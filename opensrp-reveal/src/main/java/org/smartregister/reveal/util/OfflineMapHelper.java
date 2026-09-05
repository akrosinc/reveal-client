package org.smartregister.reveal.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.turf.TurfMeasurement;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.server.FileHTTPServer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.reveal.util.Constants.Map.DOWNLOAD_MAX_ZOOM;
import static org.smartregister.reveal.util.Constants.Map.DOWNLOAD_MIN_ZOOM;


public class OfflineMapHelper {

    public static final String METADATA_JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";
    private static FileHTTPServer httpServer;
    @NonNull
    public static Pair<List<String>, Map<String, OfflineRegion>> getOfflineRegionInfo(final OfflineRegion[] offlineRegions) {
        List<String> offlineRegionNames = new ArrayList<>();
        Map<String, OfflineRegion> modelMap = new HashMap<>();

        if (offlineRegions == null) {
            return new Pair<>(offlineRegionNames, modelMap);
        }

        for (OfflineRegion offlineRegion : offlineRegions) {
            byte[] metadataBytes = offlineRegion.getMetadata();
            try {
                String jsonString = new String(metadataBytes, StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has(METADATA_JSON_FIELD_REGION_NAME)) {
                    String regionName = jsonObject.getString(METADATA_JSON_FIELD_REGION_NAME);
                    offlineRegionNames.add(regionName);
                    modelMap.put(regionName, offlineRegion);
                }
            } catch (JSONException e) {
                Timber.tag("Reveal Exception").w(e, "Error parsing offline region metadata");
            }
        }

        return new Pair<>(offlineRegionNames, modelMap);
    }
    public static void downloadMap(final Feature operationalAreaFeature, final String mapName, final Context context) {
        Runnable runnable = () -> {
            // 1. Calculate Bounding Box on DiskIO thread

            double[] bbox = TurfMeasurement.bbox(operationalAreaFeature.geometry());

            double minX = bbox[0]; // Min Longitude
            double minY = bbox[1]; // Min Latitude
            double maxX = bbox[2]; // Max Longitude
            double maxY = bbox[3]; // Max Latitude

            Timber.d("Calculated Bounding Box for %s: minX=%f, minY=%f, maxX=%f, maxY=%f", mapName, minX, minY, maxX, maxY);

            LatLngBounds latLngBounds = new LatLngBounds.Builder()
                    .include(new LatLng(maxY, maxX)) // NorthEast
                    .include(new LatLng(minY, minX)) // SouthWest
                    .build();

            // Fix 404: Correctly append the style file name to the base URL
            String baseUrl = context.getString(R.string.localhost_url, FileHTTPServer.PORT);
            String styleFileName = context.getString(R.string.reveal_offline_map_download_style);
            String mapboxStyle = baseUrl.endsWith("/") ? baseUrl + styleFileName : baseUrl + "/" + styleFileName;

            Timber.d("Using Mapbox style URL for offline download: %s", mapboxStyle);

            float minZoom = (float) DOWNLOAD_MIN_ZOOM;
            float maxZoom = (float) DOWNLOAD_MAX_ZOOM;
            float pixelRatio = context.getResources().getDisplayMetrics().density;

            OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                    mapboxStyle,
                    latLngBounds,
                    minZoom,
                    maxZoom,
                    pixelRatio
            );

            // 2. Prepare Metadata JSON
            byte[] metadata;
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(METADATA_JSON_FIELD_REGION_NAME, mapName);
                jsonObject.put("dateCreated", System.currentTimeMillis());
                metadata = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
            } catch (JSONException e) {
                Timber.e(e, "Failed to encode offline region metadata");
                metadata = new byte[0];
            }

            byte[] finalMetadata = metadata;

            // 3. Dispatch region creation to MAIN THREAD to prevent native C++ crashes
            new Handler(Looper.getMainLooper()).post(() -> {
                OfflineManager offlineManager = OfflineManager.getInstance(context);

                // Ensure tile limit is safely set high enough
                offlineManager.createOfflineRegion(
                        definition,
                        finalMetadata,
                        new OfflineManager.CreateOfflineRegionCallback() {
                            @Override
                            public void onCreate(OfflineRegion offlineRegion) {
                                Timber.d("Offline region created successfully: %s", mapName);

                                // Enable download state
                                offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

                                // Observe download progress
                                offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                                    @Override
                                    public void onStatusChanged(com.mapbox.mapboxsdk.offline.OfflineRegionStatus status) {
                                        if (status.isComplete()) {
                                            Timber.d("Download complete for region: %s", mapName);
                                        } else {
                                            double percentage = status.getRequiredResourceCount() >= 0 ?
                                                    (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) : 0.0;
                                            Timber.d("Downloading %s: %.1f%% (Completed/Required: %d/%d)",
                                                    mapName, percentage, status.getCompletedResourceCount(), status.getRequiredResourceCount());
                                        }
                                    }

                                    @Override
                                    public void onError(com.mapbox.mapboxsdk.offline.OfflineRegionError error) {
                                        Timber.e("Error downloading region %s: Reason=%s, Message=%s",
                                                mapName, error.getReason(), error.getMessage());
                                    }

                                    @Override
                                    public void mapboxTileCountLimitExceeded(long limit) {
                                        Timber.w("Tile count limit exceeded (%d) for region: %s", limit, mapName);
                                    }
                                });
                            }

                            @Override
                            public void onError(String error) {
                                Timber.e("Error creating offline region for %s: %s", mapName, error);
                            }
                        }
                );
            });
        };

        RevealApplication.getInstance().getAppExecutors().diskIO().execute(runnable);
    }
//    public static void downloadMap(final Feature operationalAreaFeature, final String mapName, final Context context) {
//        Runnable runnable = () -> {
//            // 1. Calculate Bounding Box on DiskIO thread
//            double[] bbox = TurfMeasurement.bbox(operationalAreaFeature.geometry());
//
//            double minX = bbox[0]; // Min Longitude
//            double minY = bbox[1]; // Min Latitude
//            double maxX = bbox[2]; // Max Longitude
//            double maxY = bbox[3]; // Max Latitude
//
//            LatLngBounds latLngBounds = new LatLngBounds.Builder()
//                    .include(new LatLng(maxY, maxX)) // NorthEast
//                    .include(new LatLng(minY, minX)) // SouthWest
//                    .build();
//
//            String mapboxStyle = context.getString(R.string.localhost_url, FileHTTPServer.PORT);
//            float minZoom = (float) DOWNLOAD_MIN_ZOOM;
//            float maxZoom = (float) DOWNLOAD_MAX_ZOOM;
//            float pixelRatio = context.getResources().getDisplayMetrics().density;
//
//            OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
//                    mapboxStyle,
//                    latLngBounds,
//                    minZoom,
//                    maxZoom,
//                    pixelRatio
//            );
//
//            // 2. Prepare Metadata JSON
//            byte[] metadata;
//            try {
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put(METADATA_JSON_FIELD_REGION_NAME, mapName);
//                jsonObject.put("dateCreated", System.currentTimeMillis());
//                metadata = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
//            } catch (JSONException e) {
//                Timber.e(e, "Failed to encode offline region metadata");
//                metadata = new byte[0];
//            }
//
//            byte[] finalMetadata = metadata;
//
//            // 3. Dispatch region creation to MAIN THREAD to prevent native C++ crashes
//            new Handler(Looper.getMainLooper()).post(() -> {
//                OfflineManager offlineManager = OfflineManager.getInstance(context);
//
//                // Ensure tile limit is safely set high enough
//                offlineManager.setOfflineMapboxTileCountLimit(50000);
//                offlineManager.createOfflineRegion(
//                        definition,
//                        finalMetadata,
//                        new OfflineManager.CreateOfflineRegionCallback() {
//                            @Override
//                            public void onCreate(OfflineRegion offlineRegion) {
//                                Timber.d("Offline region created: %s", mapName);
//
//                                // Enable download state
//                                offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
//
//                                // Observe download progress
//                                offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
//                                    @Override
//                                    public void onStatusChanged(com.mapbox.mapboxsdk.offline.OfflineRegionStatus status) {
//                                        if (status.isComplete()) {
//                                            Timber.d("Download complete for region: %s", mapName);
//                                        } else {
//                                            double percentage = status.getRequiredResourceCount() >= 0 ?
//                                                    (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) : 0.0;
//                                            Timber.d("Downloading %s: %.1f%%", mapName, percentage);
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onError(com.mapbox.mapboxsdk.offline.OfflineRegionError error) {
//                                        Timber.e("Error downloading region %s: %s - %s", mapName, error.getReason(), error.getMessage());
//                                    }
//
//                                    @Override
//                                    public void mapboxTileCountLimitExceeded(long limit) {
//                                        Timber.w("Tile count limit exceeded (%d) for region: %s", limit, mapName);
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onError(String error) {
//                                Timber.e("Error creating offline region: %s", error);
//                            }
//                        }
//                );
//            });
//        };
//
//        RevealApplication.getInstance().getAppExecutors().diskIO().execute(runnable);
//    }

//    public static void initializeFileHTTPServer(Context context, String digitalGlobeIdPlaceholder) {
//        try {
//            FileHTTPServer httpServer = new FileHTTPServer(context, context.getString(R.string.reveal_offline_map_download_style), digitalGlobeIdPlaceholder);
//            httpServer.start();
//        } catch (IOException e) {
//            Timber.e(e, "Failed to start local HTTP server for offline maps");
//        }
//    }
public static void initializeFileHTTPServer(Context context, String digitalGlobeIdPlaceholder) throws Exception {

    // 1. Check if the server is already running to prevent Port Collision crashes
    if (httpServer != null) {
        Timber.d("Local HTTP server is already running. Skipping startup.");
        return;
    }

    try {
        // 2. Extract the style JSON from assets to internal storage FIRST
        String styleFileName = context.getString(R.string.reveal_offline_map_download_style);
        copyAssetToInternalStorage(context, styleFileName);

        // 3. Now start the server
        httpServer = new FileHTTPServer(context, styleFileName, digitalGlobeIdPlaceholder);
        httpServer.start();
        Timber.d("Local HTTP server started successfully.");

    } catch (IOException e) {
        Timber.e(e, "Failed to start local HTTP server for offline maps");
        httpServer = null; // Reset on failure
        throw e; // Rethrow so the AsyncTask knows it failed
    }
}
    private static void copyAssetToInternalStorage(Context context, String fileName) throws IOException {
        File outFile = new File(context.getFilesDir(), fileName);

        // If the file already exists, we don't need to copy it again unless we want to overwrite it
        if (!outFile.exists()) {
            try (java.io.InputStream is = context.getAssets().open(fileName);
                 java.io.OutputStream os = new java.io.FileOutputStream(outFile)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                Timber.d("Successfully copied %s to internal storage", fileName);
            }
        }
    }
}
//package org.smartregister.reveal.util;
//
//import android.content.Context;
//import androidx.annotation.NonNull;
//import androidx.core.util.Pair;
//
//import com.mapbox.geojson.Feature;
//import com.mapbox.mapboxsdk.geometry.LatLng;
//import com.mapbox.mapboxsdk.offline.OfflineRegion;
//import com.mapbox.turf.TurfMeasurement;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.smartregister.reveal.BuildConfig;
//import org.smartregister.reveal.R;
//import org.smartregister.reveal.application.RevealApplication;
//import org.smartregister.reveal.server.FileHTTPServer;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import io.ona.kujaku.data.realm.RealmDatabase;
//import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
//import io.ona.kujaku.downloaders.MapBoxOfflineResourcesDownloader;
//import io.ona.kujaku.helpers.OfflineServiceHelper;
//import timber.log.Timber;
//
//import static io.ona.kujaku.data.MapBoxDownloadTask.MAP_NAME;
//import static org.smartregister.reveal.util.Constants.Map.DOWNLOAD_MAX_ZOOM;
//import static org.smartregister.reveal.util.Constants.Map.DOWNLOAD_MIN_ZOOM;
//
///**
// * Created by Richard Kareko on 1/30/20.
// */
//
//public class OfflineMapHelper {
//
//    @NonNull
//    public static Pair<List<String>, Map<String, OfflineRegion>> getOfflineRegionInfo (final OfflineRegion[] offlineRegions) {
//        List<String> offlineRegionNames = new ArrayList<>();
//        Map<String, OfflineRegion> modelMap = new HashMap<>();
//
//        for(int position = 0; position < offlineRegions.length; position++) {
//
//            byte[] metadataBytes = offlineRegions[position].getMetadata();
//            try {
//                JSONObject jsonObject = new JSONObject(new String(metadataBytes));
//                if (jsonObject.has(MapBoxOfflineResourcesDownloader.METADATA_JSON_FIELD_REGION_NAME)) {
//                    String regionName = jsonObject.getString(MapBoxOfflineResourcesDownloader.METADATA_JSON_FIELD_REGION_NAME);
//                    offlineRegionNames.add(regionName);
//                    modelMap.put(regionName, offlineRegions[position]);
//                }
//
//            } catch (JSONException e) {
//                Timber.tag("Reveal Exception").w(e);
//            }
//
//        }
//
//        return new Pair(offlineRegionNames, modelMap);
//    }
//
//    public static Map<String, MapBoxOfflineQueueTask> populateOfflineQueueTaskMap(RealmDatabase realmDatabase) {
//        Map<String, MapBoxOfflineQueueTask> offlineQueueTaskMap = new HashMap<>();
//
//        List<MapBoxOfflineQueueTask> offlineQueueTasks = realmDatabase.getTasks();
//
//        if (offlineQueueTasks == null){
//            return offlineQueueTaskMap;
//        }
//
//        for (MapBoxOfflineQueueTask offlineQueueTask: offlineQueueTasks) {
//
//            try {
//                if (MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD.equals(offlineQueueTask.getTaskType())
//                        && MapBoxOfflineQueueTask.TASK_STATUS_DONE == offlineQueueTask.getTaskStatus()) {
//                    offlineQueueTaskMap.put(offlineQueueTask.getTask().get(MAP_NAME).toString(), offlineQueueTask);
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return offlineQueueTaskMap;
//    }
//
//    public static void downloadMap(final Feature operationalAreaFeature, final String mapName, final Context context) {
//        Runnable runnable = new Runnable() {
//            public void run() {
//                double[] bbox = TurfMeasurement.bbox(operationalAreaFeature.geometry());
//
//                double minX = bbox[0];
//                double minY = bbox[1];
//                double maxX = bbox[2];
//                double maxY = bbox[3];
//
//                double topLeftLat = maxY;
//                double topLeftLng = minX;
//                double bottomRightLat = minY;
//                double bottomRightLng = maxX;
//                double topRightLat = maxY;
//                double topRightLng = maxX;
//                double bottomLeftLat = minY;
//                double bottomLeftLng = minX;
//
//                String mapboxStyle = context.getString(R.string.localhost_url, FileHTTPServer.PORT);
//
//                LatLng topLeftBound = new LatLng(topLeftLat, topLeftLng);
//                LatLng topRightBound = new LatLng(topRightLat, topRightLng);
//                LatLng bottomRightBound = new LatLng(bottomRightLat, bottomRightLng);
//                LatLng bottomLeftBound = new LatLng(bottomLeftLat, bottomLeftLng);
//
//                double maxZoom = DOWNLOAD_MAX_ZOOM;
//                double minZoom = DOWNLOAD_MIN_ZOOM;
//
//                OfflineServiceHelper.ZoomRange zoomRange = new OfflineServiceHelper.ZoomRange(minZoom, maxZoom);
//
//                OfflineServiceHelper.requestOfflineMapDownload(context
//                        , mapName
//                        , mapboxStyle
//                        , BuildConfig.MAPBOX_SDK_ACCESS_TOKEN
//                        , topLeftBound
//                        , topRightBound
//                        , bottomRightBound
//                        , bottomLeftBound
//                        , zoomRange
//                );
//            }
//        };
//
//        RevealApplication.getInstance().getAppExecutors().diskIO().execute(runnable);
//    }
//
//    public static void initializeFileHTTPServer(Context context, String digitalGlobeIdPlaceholder) {
//        try {
//            FileHTTPServer httpServer = new FileHTTPServer(context, context.getString(R.string.reveal_offline_map_download_style), digitalGlobeIdPlaceholder);
//            httpServer.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
