package org.smartregister.reveal.interactor;

import android.content.Context;
import androidx.core.util.Pair;

import com.mapbox.mapboxsdk.offline.OfflineRegion;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.Location;
import org.smartregister.repository.LocationRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.DownloadedOfflineMapsContract;
import org.smartregister.reveal.model.OfflineMapModel;
import org.smartregister.reveal.util.AppExecutors;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class DownloadedOfflineMapsInteractor implements DownloadedOfflineMapsContract.Interactor {

    private final AppExecutors appExecutors;
    private final LocationRepository locationRepository;
    private final DownloadedOfflineMapsContract.Presenter presenter;

    public DownloadedOfflineMapsInteractor(DownloadedOfflineMapsContract.Presenter presenter, Context context) {
        this.presenter = presenter;
        this.appExecutors = RevealApplication.getInstance().getAppExecutors();
        this.locationRepository = RevealApplication.getInstance().getLocationRepository();
    }

    @Override
    public void fetchLocationsWithOfflineMapDownloads(final Pair<List<String>, Map<String, OfflineRegion>> offlineRegionInfo) {
        Runnable runnable = () -> {
            if (offlineRegionInfo == null || offlineRegionInfo.first == null || offlineRegionInfo.first.isEmpty()) {
                appExecutors.mainThread().execute(() -> presenter.onOAsWithOfflineDownloadsFetched(new ArrayList<>()));
                return;
            }

            // Fetch Location domains from database using offline region names/IDs
            List<Location> operationalAreas = locationRepository.getLocationsByIds(offlineRegionInfo.first);

            // Populate offline map models using Native Mapbox OfflineRegion instances
            List<OfflineMapModel> offlineMapModels = populateOfflineMapModelList(operationalAreas, offlineRegionInfo.second);

            appExecutors.mainThread().execute(() -> presenter.onOAsWithOfflineDownloadsFetched(offlineMapModels));
        };

        appExecutors.diskIO().execute(runnable);
    }

    public List<OfflineMapModel> populateOfflineMapModelList(List<Location> locations, Map<String, OfflineRegion> offlineRegionMap) {
        List<OfflineMapModel> offlineMapModels = new ArrayList<>();

        if (locations == null || offlineRegionMap == null) {
            return offlineMapModels;
        }

        for (Location location : locations) {
            OfflineMapModel offlineMapModel = new OfflineMapModel();
            offlineMapModel.setLocation(location);
            offlineMapModel.setOfflineMapStatus(OfflineMapModel.OfflineMapStatus.DOWNLOADED);

            OfflineRegion offlineRegion = offlineRegionMap.get(location.getId());
            if (offlineRegion != null) {
                offlineMapModel.setOfflineRegion(offlineRegion);

                // Extract metadata (e.g. date created) stored inside native Mapbox OfflineRegion
                byte[] metadataBytes = offlineRegion.getMetadata();
                if (metadataBytes != null && metadataBytes.length > 0) {
                    try {
                        String jsonString = new String(metadataBytes, StandardCharsets.UTF_8);
                        JSONObject jsonObject = new JSONObject(jsonString);

                        if (jsonObject.has("dateCreated")) {
                            long dateMillis = jsonObject.getLong("dateCreated");
                            offlineMapModel.setDateCreated(new Date(dateMillis));
                        }
                    } catch (JSONException e) {
                        Timber.tag("Reveal Exception").w(e, "Error parsing metadata for location: %s", location.getId());
                    }
                }
            }

            offlineMapModels.add(offlineMapModel);
        }

        return offlineMapModels;
    }
}
//package org.smartregister.reveal.interactor;
//
//import android.content.Context;
//import androidx.core.util.Pair;
//
//import com.mapbox.mapboxsdk.offline.OfflineRegion;
//
//import org.smartregister.domain.Location;
//import org.smartregister.repository.LocationRepository;
//import org.smartregister.reveal.application.RevealApplication;
//import org.smartregister.reveal.contract.DownloadedOfflineMapsContract;
//import org.smartregister.reveal.model.OfflineMapModel;
//import org.smartregister.reveal.util.AppExecutors;
//import org.smartregister.reveal.util.OfflineMapHelper;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import io.ona.kujaku.data.realm.RealmDatabase;
//import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
//
//public class DownloadedOfflineMapsInteractor implements DownloadedOfflineMapsContract.Interactor {
//
//    private AppExecutors appExecutors;
//
//    private LocationRepository locationRepository;
//
//    private DownloadedOfflineMapsContract.Presenter presenter;
//
//    private RealmDatabase realmDatabase;
//
//    private Map<String, MapBoxOfflineQueueTask> offlineQueueTaskMap;
//
//    public DownloadedOfflineMapsInteractor(DownloadedOfflineMapsContract.Presenter presenter, Context context) {
//        this.presenter = presenter;
//        appExecutors = RevealApplication.getInstance().getAppExecutors();
//        locationRepository = RevealApplication.getInstance().getLocationRepository();
//        realmDatabase = RevealApplication.getInstance().getRealmDatabase(context);
//        offlineQueueTaskMap = new HashMap<>();
//    }
//
//    @Override
//    public void fetchLocationsWithOfflineMapDownloads(final Pair<List<String>, Map<String, OfflineRegion>> offlineRegionInfo) {
//
//        Runnable runnable = new Runnable() {
//            public void run() {
//                if (offlineRegionInfo == null || offlineRegionInfo.first == null) {
//                    presenter.onOAsWithOfflineDownloadsFetched(null);
//                    return;
//                }
//
//                List<Location> operationalAreas = locationRepository.getLocationsByIds(offlineRegionInfo.first);
//
//                setOfflineQueueTaskMap(OfflineMapHelper.populateOfflineQueueTaskMap(realmDatabase));
//
//                List<OfflineMapModel> offlineMapModels = populateOfflineMapModelList(operationalAreas, offlineRegionInfo.second);
//
//                appExecutors.mainThread().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        presenter.onOAsWithOfflineDownloadsFetched(offlineMapModels);
//                    }
//                });
//            }
//        };
//
//        appExecutors.diskIO().execute(runnable);
//
//    }
//
//    public List<OfflineMapModel>  populateOfflineMapModelList(List<Location> locations, Map<String, OfflineRegion> offlineRegionMap) {
//
//        List<OfflineMapModel> offlineMapModels = new ArrayList<>();
//        for (Location location: locations) {
//            OfflineMapModel offlineMapModel = new OfflineMapModel();
//            offlineMapModel.setLocation(location);
//            offlineMapModel.setOfflineMapStatus(OfflineMapModel.OfflineMapStatus.DOWNLOADED);
//            offlineMapModel.setOfflineRegion(offlineRegionMap.get(location.getId()));
//
//            if (offlineQueueTaskMap.get(location.getId()) != null) {
//                offlineMapModel.setDateCreated(offlineQueueTaskMap.get(location.getId()).getDateCreated());
//            }
//
//            offlineMapModels.add(offlineMapModel);
//        }
//
//        return offlineMapModels;
//    }
//
//    public void setOfflineQueueTaskMap(Map<String, MapBoxOfflineQueueTask> offlineQueueTaskMap) {
//        this.offlineQueueTaskMap = offlineQueueTaskMap;
//    }
//
//}
