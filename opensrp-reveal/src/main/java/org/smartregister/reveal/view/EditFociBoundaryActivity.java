package org.smartregister.reveal.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.smartregister.domain.Location;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.EditFociboundaryContract;
import org.smartregister.reveal.presenter.EditFociBoundaryPresenter;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.EditBoundaryState;
import org.smartregister.reveal.util.RevealMapHelper;
import org.smartregister.reveal.util.Utils;
import org.smartregister.sync.helper.LocationServiceHelper;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static org.smartregister.reveal.util.Utils.getCoordsFromGeometry;
import static org.smartregister.reveal.util.Utils.getSatelliteStyle;

/**
 * Created by Richard Kareko on 5/13/20.
 */

public class EditFociBoundaryActivity extends BaseMapActivity implements EditFociboundaryContract.View, View.OnClickListener {

    private static final String TAG = EditFociBoundaryActivity.class.getName();

    private static final String BOUNDARY_SOURCE_ID = "foci-boundary-source";
    private static final String BOUNDARY_FILL_LAYER_ID = "foci-boundary-fill-layer";
    private static final String BOUNDARY_LINE_LAYER_ID = "foci-boundary-line-layer";
    private static final String BOUNDARY_SYMBOL_LAYER_ID = "foci-boundary-symbol-layer";

    private Toolbar toolbar;
    private Button deleteBtn;
    private Button savePointBtn;
    private Button saveBoundaryBtn;
    private Button cancelBtn;

    private RevealApplication revealApplication = RevealApplication.getInstance();
    private EditFociBoundaryPresenter presenter;

    private CircleManager circleManager;
    private Circle selectedCircle;
    private final List<LatLng> boundaryPoints = new ArrayList<>();
    private Feature currentOperationalAreaFeature;
    private MapboxMap mMapboxMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new EditFociBoundaryPresenter(this);

        setContentView(R.layout.activity_edit_foci_boundary_map_view);
        mapView = findViewById(R.id.kmv_drawingBoundaries_mapView);
        mapView.onCreate(savedInstanceState);

        setUpToolbar();
        setUpViews();

        toggleButtons(EditBoundaryState.START);

        String featureCollection = revealApplication.getFeatureCollection().toJson();
        currentOperationalAreaFeature = revealApplication.getOperationalArea();

        displaySnackBar(R.string.tap_point_msg);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mMapboxMap = mapboxMap;
                String satelliteStyle = getSatelliteStyle(getContext());
                Style.Builder builder = new Style.Builder().fromUri(satelliteStyle);

                mapboxMap.setStyle(builder, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableNativeLocationComponent(style);

                        GeoJsonSource geoJsonSource = style.getSourceAs(getString(R.string.reveal_datasource_name));
                        if (geoJsonSource != null && StringUtils.isNotBlank(featureCollection)) {
                            geoJsonSource.setGeoJson(featureCollection);
                        }

                        RevealMapHelper.addCustomLayers(style, EditFociBoundaryActivity.this);

                        drawBoundaryLayer(style);
                        initializeDrawingManager(style);
                    }
                });

                mapboxMap.getUiSettings().setRotateGesturesEnabled(false);

                if (currentOperationalAreaFeature != null) {
                    CameraPosition cameraPosition = mapboxMap.getCameraForGeometry(currentOperationalAreaFeature.geometry());
                    if (cameraPosition != null) {
                        mapboxMap.setCameraPosition(cameraPosition);
                    }
                }
            }
        });
    }

    private void enableNativeLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this) && mMapboxMap != null) {
            LocationComponent locationComponent = mMapboxMap.getLocationComponent();
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(true)
                            .build();

            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            locationComponent.applyStyle(getApplicationContext(), R.style.LocationComponentStyling);
        }
    }

    private void drawBoundaryLayer(@NonNull Style style) {
        if (currentOperationalAreaFeature == null) return;

        FeatureCollection featureCollection = FeatureCollection.fromFeature(currentOperationalAreaFeature);
        GeoJsonSource source = style.getSourceAs(BOUNDARY_SOURCE_ID);
        if (source == null) {
            source = new GeoJsonSource(BOUNDARY_SOURCE_ID, featureCollection);
            style.addSource(source);

            FillLayer fillLayer = new FillLayer(BOUNDARY_FILL_LAYER_ID, BOUNDARY_SOURCE_ID);
            fillLayer.setProperties(
                    fillColor(Color.WHITE),
                    fillOpacity(0.15f)
            );
            style.addLayer(fillLayer);

            LineLayer lineLayer = new LineLayer(BOUNDARY_LINE_LAYER_ID, BOUNDARY_SOURCE_ID);
            lineLayer.setProperties(
                    lineColor(Color.WHITE),
                    lineWidth(getResources().getDimension(R.dimen.operational_area_boundary_width))
            );
            style.addLayer(lineLayer);

            SymbolLayer symbolLayer = new SymbolLayer(BOUNDARY_SYMBOL_LAYER_ID, BOUNDARY_SOURCE_ID);
            symbolLayer.setProperties(
                    textField("{" + Constants.Map.NAME_PROPERTY + "}"),
                    textColor(Color.WHITE),
                    textSize(getResources().getDimension(R.dimen.operational_area_boundary_text_size)),
                    textIgnorePlacement(true),
                    textAllowOverlap(true)
            );
            style.addLayer(symbolLayer);
        } else {
            source.setGeoJson(featureCollection);
        }
    }

    private void initializeDrawingManager(@NonNull Style style) {
        if (mMapboxMap == null) return;

        circleManager = new CircleManager(mapView, mMapboxMap, style);
        circleManager.addClickListener(circle -> {
            selectedCircle = circle;
            Toast.makeText(EditFociBoundaryActivity.this, getString(R.string.circle_clicked), Toast.LENGTH_LONG).show();
            deleteBtn.setEnabled(selectedCircle != null);
            presenter.onEditPoint();
            return true;
        });

        mMapboxMap.addOnMapClickListener(latLng -> {
            if (savePointBtn.getVisibility() == View.VISIBLE) {
                addPointToBoundary(latLng);
                Toast.makeText(EditFociBoundaryActivity.this, getString(R.string.circle_not_clicked), Toast.LENGTH_LONG).show();
                deleteBtn.setEnabled(false);
            }
            return true;
        });

        // Initialize points from current feature polygon
        if (currentOperationalAreaFeature != null && currentOperationalAreaFeature.geometry() instanceof Polygon) {
            Polygon polygon = (Polygon) currentOperationalAreaFeature.geometry();
            List<Point> coordinates = polygon.coordinates().get(0);
            for (Point point : coordinates) {
                addPointToBoundary(new LatLng(point.latitude(), point.longitude()));
            }
        }
    }

    private void addPointToBoundary(LatLng latLng) {
        boundaryPoints.add(latLng);

        int colorInt = ContextCompat.getColor(this, R.color.colorAccent);
        String colorHex = String.format("#%06X", (0xFFFFFF & colorInt));

        CircleOptions circleOptions = new CircleOptions()
                .withLatLng(latLng)
                .withCircleColor(colorHex)
                .withCircleRadius(6f)
                .withDraggable(true);

        circleManager.create(circleOptions);
        updateBoundaryGeometry();
    }

    private void updateBoundaryGeometry() {
        if (boundaryPoints.size() < 3 || mMapboxMap == null) return;

        List<Point> points = new ArrayList<>();
        for (LatLng latLng : boundaryPoints) {
            points.add(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
        }
        // Close polygon loop
        points.add(Point.fromLngLat(boundaryPoints.get(0).getLongitude(), boundaryPoints.get(0).getLatitude()));

        List<List<Point>> coordinates = new ArrayList<>();
        coordinates.add(points);
        Polygon updatedPolygon = Polygon.fromLngLats(coordinates);

        currentOperationalAreaFeature = Feature.fromGeometry(updatedPolygon, currentOperationalAreaFeature.properties());

        if (mMapboxMap.getStyle() != null) {
            GeoJsonSource source = mMapboxMap.getStyle().getSourceAs(BOUNDARY_SOURCE_ID);
            if (source != null) {
                source.setGeoJson(FeatureCollection.fromFeature(currentOperationalAreaFeature));
            }
        }
    }

    protected void setUpToolbar() {
        toolbar = findViewById(R.id.edit_boundary_toolbar);
        toolbar.setTitle(R.string.edit_boundary);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    protected void setUpViews() {
        deleteBtn = findViewById(R.id.btn_drawingBoundaries_delete);
        deleteBtn.setOnClickListener(this);

        savePointBtn = findViewById(R.id.btn_drawingBoundaries_save_point);
        savePointBtn.setOnClickListener(this);

        saveBoundaryBtn = findViewById(R.id.btn_drawingBoundaries_save);
        saveBoundaryBtn.setOnClickListener(this);

        cancelBtn = findViewById(R.id.btn_drawingBoundaries_cancel);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void toggleButtons(EditBoundaryState state) {
        switch (state) {
            case EDITTING:
                cancelBtn.setVisibility(View.GONE);
                saveBoundaryBtn.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.VISIBLE);
                savePointBtn.setVisibility(View.VISIBLE);
                break;
            case START:
            case FINISHED:
            default:
                cancelBtn.setVisibility(View.VISIBLE);
                saveBoundaryBtn.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.GONE);
                savePointBtn.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void exitEditBoundaryActivity() {
        RevealApplication.getInstance().setRefreshMapOnEventSaved(true);
        finish();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void deletePoint(View view) {
        if (selectedCircle != null && circleManager != null) {
            boundaryPoints.remove(selectedCircle.getLatLng());
            circleManager.delete(selectedCircle);
            selectedCircle = null;
            updateBoundaryGeometry();
            view.setEnabled(false);
        }
    }

    @Override
    public void displaySnackBar(int message) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.drawingBoundaries_map_section), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void setToolbarTitle(int title) {
        toolbar.setTitle(title);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_drawingBoundaries_delete) {
            presenter.onDeletePoint(view);
        } else if (id == R.id.btn_drawingBoundaries_save_point) {
            savePoint();
        } else if (id == R.id.btn_drawingBoundaries_save) {
            saveBoundary();
        } else if (id == R.id.btn_drawingBoundaries_cancel) {
            presenter.onCancelEditBoundaryChanges();
        }
    }

    protected void savePoint() {
        toggleButtons(EditBoundaryState.FINISHED);
        deleteBtn.setEnabled(false);
    }

    protected void saveBoundary() {
        if (currentOperationalAreaFeature == null) return;

        Geometry updatedGeometry = currentOperationalAreaFeature.geometry();
        if (updatedGeometry instanceof MultiPolygon) {
            exitEditBoundaryActivity();
            return;
        }

        JSONArray updatedCoords = getCoordsFromGeometry(updatedGeometry, revealApplication.getOperationalArea().geometry());

        Location operationalAreaLocation = LocationServiceHelper.locationGson.fromJson(revealApplication.getOperationalArea().toJson(), Location.class);
        JsonArray updatedCoordsJsonArray = LocationServiceHelper.locationGson.fromJson(updatedCoords.toString(), JsonArray.class);
        operationalAreaLocation.getGeometry().setCoordinates(updatedCoordsJsonArray);

        Location dbLocation = Utils.getLocationById(operationalAreaLocation.getId());
        if (dbLocation != null && dbLocation.getLocationTags() != null) {
            operationalAreaLocation.setLocationTags(dbLocation.getLocationTags());
        }
        presenter.onSaveEditedBoundary(operationalAreaLocation);
    }
}
//package org.smartregister.reveal.view;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.widget.Toolbar;
//
//import com.google.android.material.snackbar.Snackbar;
//import com.google.gson.JsonArray;
//import com.mapbox.android.core.permissions.PermissionsManager;
//import com.mapbox.geojson.FeatureCollection;
//import com.mapbox.geojson.Geometry;
//import com.mapbox.geojson.MultiPolygon;
//import com.mapbox.mapboxsdk.camera.CameraPosition;
//import com.mapbox.mapboxsdk.geometry.LatLng;
//import com.mapbox.mapboxsdk.location.LocationComponent;
//import com.mapbox.mapboxsdk.location.modes.RenderMode;
//import com.mapbox.mapboxsdk.maps.MapboxMap;
//import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
//import com.mapbox.mapboxsdk.maps.Style;
//import com.mapbox.mapboxsdk.plugins.annotation.Circle;
//import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
//
//import org.apache.commons.lang3.StringUtils;
//import org.json.JSONArray;
//import org.smartregister.domain.Location;
//import org.smartregister.reveal.R;
//import org.smartregister.reveal.application.RevealApplication;
//import org.smartregister.reveal.contract.EditFociboundaryContract;
//import org.smartregister.reveal.presenter.EditFociBoundaryPresenter;
//import org.smartregister.reveal.util.Constants;
//import org.smartregister.reveal.util.EditBoundaryState;
//import org.smartregister.reveal.util.RevealMapHelper;
//import org.smartregister.reveal.util.Utils;
//import org.smartregister.sync.helper.LocationServiceHelper;
//
//import io.ona.kujaku.callbacks.OnLocationComponentInitializedCallback;
//import io.ona.kujaku.layers.FillBoundaryLayer;
//import io.ona.kujaku.layers.KujakuLayer;
//import io.ona.kujaku.listeners.OnDrawingCircleClickListener;
//import io.ona.kujaku.listeners.OnKujakuLayerLongClickListener;
//import io.ona.kujaku.manager.DrawingManager;
//
//import static org.smartregister.reveal.util.Utils.getCoordsFromGeometry;
//import static org.smartregister.reveal.util.Utils.getLocationBuffer;
//import static org.smartregister.reveal.util.Utils.getPixelsPerDPI;
//import static org.smartregister.reveal.util.Utils.getSatelliteStyle;
//import static org.smartregister.reveal.util.Utils.isCurrentTargetLevelStructure;
//
///**
// * Created by Richard Kareko on 5/13/20.
// */
//
//public class EditFociBoundaryActivity extends BaseMapActivity implements EditFociboundaryContract.View, OnLocationComponentInitializedCallback, View.OnClickListener {
//
//    private static final String TAG = EditFociBoundaryActivity.class.getName();
//
//    private DrawingManager drawingManager;
//
//    private Toolbar toolbar;
//    private Button deleteBtn ;
//    private Button savePointBtn;
//    private Button saveBoundaryBtn;
//    private Button cancelBtn;
//
//    private RevealApplication revealApplication = RevealApplication.getInstance();
//    private boolean locationComponentActive = false;
//    private FillBoundaryLayer boundaryLayer;
//    private EditFociBoundaryPresenter presenter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        presenter = new EditFociBoundaryPresenter(this);
//
//        setContentView(R.layout.activity_edit_foci_boundary_map_view);
//        kujakuMapView = findViewById(R.id.kmv_drawingBoundaries_mapView);
//        kujakuMapView.onCreate(savedInstanceState);
//
//        kujakuMapView.setDisableMyLocationOnMapMove(true);
//        kujakuMapView.getMapboxLocationComponentWrapper().setOnLocationComponentInitializedCallback(this);
//
//        setUpToolbar();
//
//        buildBoundaryLayer();
//
//        setUpViews();
//
//        toggleButtons(EditBoundaryState.START);
//
//        String featureCollection = revealApplication.getFeatureCollection().toJson();
//        String finalFeatureCollection = featureCollection;
//        com.mapbox.geojson.Feature finalOperationalAreaFeature = revealApplication.getOperationalArea();
//        boolean finalLocationComponentActive = locationComponentActive;
//
//        displaySnackBar(R.string.tap_point_msg);
//
//        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
//            @Override
//            public void onMapReady(@NonNull MapboxMap mapboxMap) {
//                String satelliteStyle = getSatelliteStyle(getContext());
//                Style.Builder builder = new Style.Builder().fromUri(satelliteStyle);
//                mapboxMap.setStyle(builder,  new Style.OnStyleLoaded() {
//                    @Override
//                    public void onStyleLoaded(@NonNull Style style) {
//
//                        GeoJsonSource geoJsonSource = style.getSourceAs(getString(R.string.reveal_datasource_name));
//
//                        if (geoJsonSource != null && StringUtils.isNotBlank(finalFeatureCollection)) {
//                            geoJsonSource.setGeoJson(finalFeatureCollection);
//                        }
//
//                        RevealMapHelper.addCustomLayers(style, EditFociBoundaryActivity.this);
//
//                        kujakuMapView.setMapboxMap(mapboxMap);
//
//                        RevealMapHelper.addBaseLayers(kujakuMapView, style, EditFociBoundaryActivity.this);
//
//                        initializeDrawingManager(mapboxMap, style);
//
//                        //jump straight into edit mode
//                        enableDrawingMode(mapboxMap);
//                    }
//                }); //end of set style
//
//                mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
//
//                kujakuMapView.setMapboxMap(mapboxMap);
//                float bufferRadius = getLocationBuffer(isCurrentTargetLevelStructure()) / getPixelsPerDPI(getResources());
//                kujakuMapView.setLocationBufferRadius(bufferRadius);
//
//
//                if (finalOperationalAreaFeature != null && !finalLocationComponentActive) {
//                    CameraPosition cameraPosition = mapboxMap.getCameraForGeometry(finalOperationalAreaFeature.geometry());
//                    if (cameraPosition != null) {
//                        mapboxMap.setCameraPosition(cameraPosition);
//                    }
//                } else {
//                    kujakuMapView.focusOnUserLocation(true, bufferRadius, RenderMode.COMPASS);
//                }
//
//            }
//        });
//    }
//
//    private void initializeDrawingManager(@NonNull MapboxMap mapboxMap, @NonNull Style style) {
//        drawingManager = new DrawingManager(kujakuMapView, mapboxMap, style);
//
//        drawingManager.addOnDrawingCircleClickListener(new OnDrawingCircleClickListener() {
//            @Override
//            public void onCircleClick(@NonNull Circle circle) {
//                Toast.makeText(EditFociBoundaryActivity.this,
//                        getString(R.string.circle_clicked), Toast.LENGTH_LONG).show();
//                deleteBtn.setEnabled(drawingManager.getCurrentKujakuCircle() != null);
//                presenter.onEditPoint();
//            }
//
//            @Override
//            public void onCircleNotClick(@NonNull LatLng latLng) {
//                Toast.makeText(EditFociBoundaryActivity.this,
//                        getString(R.string.circle_not_clicked), Toast.LENGTH_LONG).show();
//                deleteBtn.setEnabled(false);
//            }
//        });
//
//        drawingManager.addOnKujakuLayerLongClickListener(new OnKujakuLayerLongClickListener() {
//            @Override
//            public void onKujakuLayerLongClick(@NonNull KujakuLayer kujakuLayer) {
//
//                if (drawingManager.isDrawingEnabled()) {
//                    savePointBtn.setText(R.string.save_point);
//                }
//            }
//        });
//    }
//
//    protected void setUpToolbar() {
//        toolbar = this.findViewById(R.id.edit_boundary_toolbar);
//        toolbar.setTitle(R.string.edit_boundary);
//        this.setSupportActionBar(toolbar);
//        this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//    }
//
//    protected void setUpViews() {
//        this.deleteBtn = findViewById(R.id.btn_drawingBoundaries_delete);
//        this.deleteBtn.setOnClickListener(this);
//
//        this.savePointBtn = findViewById(R.id.btn_drawingBoundaries_save_point);
//        this.savePointBtn.setOnClickListener(this);
//
//        this.saveBoundaryBtn = findViewById(R.id.btn_drawingBoundaries_save);
//        this.saveBoundaryBtn.setOnClickListener(this);
//
//        cancelBtn = findViewById(R.id.btn_drawingBoundaries_cancel);
//        this.cancelBtn.setOnClickListener(this);
//    }
//
//    private void buildBoundaryLayer() {
//        if (revealApplication.getOperationalArea() != null) {
//            FillBoundaryLayer.Builder boundaryBuilder = new FillBoundaryLayer.Builder(FeatureCollection.fromFeature(revealApplication.getOperationalArea()))
//                    .setLabelProperty(Constants.Map.NAME_PROPERTY)
//                    .setLabelTextSize(getResources().getDimension(R.dimen.operational_area_boundary_text_size))
//                    .setLabelColorInt(Color.WHITE)
//                    .setBoundaryColor(Color.WHITE)
//                    .setBoundaryWidth(getResources().getDimension(R.dimen.operational_area_boundary_width));
//            boundaryLayer = boundaryBuilder.build();
//        }
//    }
//
//    protected void enableDrawingMode(MapboxMap mapboxMap) {
//        boundaryLayer.disableLayerOnMap(mapboxMap);
//        if (drawingManager != null) {
//            if (!drawingManager.isDrawingEnabled()) {
//                if (drawingManager.editBoundary( boundaryLayer)) {
//                    savePointBtn.setText(R.string.save_point);
//                }
//            } else {
//                drawingManager.stopDrawingAndDisplayLayer();
//            }
//        } else {
//            Log.e(TAG, "Drawing manager instance is null");
//        }
//
//        deleteBtn.setEnabled(false);
//    }
//
//    @Override
//    public void toggleButtons(EditBoundaryState state) {
//        switch (state) {
//            case EDITTING:
//                cancelBtn.setVisibility(View.GONE);
//                saveBoundaryBtn.setVisibility(View.GONE);
//                deleteBtn.setVisibility(View.VISIBLE);
//                savePointBtn.setVisibility(View.VISIBLE);
//                break;
//            case START:
//            case FINISHED:
//            default:
//                cancelBtn.setVisibility(View.VISIBLE);
//                saveBoundaryBtn.setVisibility(View.VISIBLE);
//                deleteBtn.setVisibility(View.GONE);
//                savePointBtn.setVisibility(View.GONE);
//                break;
//        }
//    }
//
//    @Override
//    public void exitEditBoundaryActivity() {
//        if (drawingManager != null && drawingManager.isDrawingEnabled()) {
//            drawingManager.stopDrawingAndDisplayLayer();
//        }
//        RevealApplication.getInstance().setRefreshMapOnEventSaved(true);
//        finish();
//    }
//
//    @Override
//    public Context getContext() {
//        return this;
//    }
//
//    @Override
//    public void deletePoint(View view) {
//        if (drawingManager != null) {
//            drawingManager.deleteDrawingCurrentCircle();
//            view.setEnabled(false);
//        }
//    }
//
//    @Override
//    public void displaySnackBar(int message) {
//        Snackbar snackbar = Snackbar.make(findViewById(R.id.drawingBoundaries_map_section), message, Snackbar.LENGTH_LONG);
//        snackbar.show();
//    }
//
//    @Override
//    public void setToolbarTitle(int title) {
//        toolbar.setTitle(title);
//    }
//
//    @Override
//    public void onLocationComponentInitialized() {
//        if (PermissionsManager.areLocationPermissionsGranted(this)) {
//            LocationComponent locationComponent = kujakuMapView.getMapboxLocationComponentWrapper()
//                    .getLocationComponent();
//            locationComponent.applyStyle(getApplicationContext(), R.style.LocationComponentStyling);
//        }
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.btn_drawingBoundaries_delete:
//                presenter.onDeletePoint(view);
//                break;
//            case R.id.btn_drawingBoundaries_save_point:
//                savePoint();
//                break;
//            case R.id.btn_drawingBoundaries_save:
//                saveBoundary();
//                break;
//            case R.id.btn_drawingBoundaries_cancel:
//                presenter.onCancelEditBoundaryChanges();
//                break;
//            default:
//                break;
//
//        }
//    }
//
//    protected void savePoint() {
//        if (drawingManager != null) {
//            if (drawingManager.isDrawingEnabled()) {
//                kujakuMapView.addLayer(boundaryLayer);
//                drawingManager.stopDrawingAndDisplayLayer();
//                toggleButtons(EditBoundaryState.FINISHED);
//            }
//        } else {
//            Log.e(TAG, "Drawing manager instance is null");
//        }
//
//        deleteBtn.setEnabled(false);
//    }
//
//    protected void saveBoundary() {
//        Geometry updatedGeometry = boundaryLayer.getFeatureCollection().features().get(0).geometry();
//        if ( updatedGeometry instanceof  MultiPolygon) {
//            // boundary has not been edited
//            exitEditBoundaryActivity();
//            return;
//        }
//
//        JSONArray updatedCoords = getCoordsFromGeometry(updatedGeometry, revealApplication.getOperationalArea().geometry());
//
//        Location operationalAreaLocation = LocationServiceHelper.locationGson.fromJson(revealApplication.getOperationalArea().toJson(), Location.class);
//        JsonArray updatedCoordsJsonArray = LocationServiceHelper.locationGson.fromJson(updatedCoords.toString(), JsonArray.class);
//        operationalAreaLocation.getGeometry().setCoordinates(updatedCoordsJsonArray);
//
//        //update location tags
//        Location dbLocation = Utils.getLocationById(operationalAreaLocation.getId());
//        if (dbLocation != null && dbLocation.getLocationTags() != null) {
//            operationalAreaLocation.setLocationTags(dbLocation.getLocationTags());
//        }
//        presenter.onSaveEditedBoundary(operationalAreaLocation);
//    }
//}
