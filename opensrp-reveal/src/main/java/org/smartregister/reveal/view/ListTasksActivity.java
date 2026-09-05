package org.smartregister.reveal.view;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static org.smartregister.reveal.util.Constants.ANIMATE_TO_LOCATION_DURATION;
import static org.smartregister.reveal.util.Constants.Action.HABITAT_SURVEY;
import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE;
import static org.smartregister.reveal.util.Constants.Action.LSM_HOUSEHOLD_SURVEY;
import static org.smartregister.reveal.util.Constants.Action.MDA_ONCHOCERCIASIS_SURVEY;
import static org.smartregister.reveal.util.Constants.Action.MDA_SURVEY;
import static org.smartregister.reveal.util.Constants.Action.RCD;
import static org.smartregister.reveal.util.Constants.Action.SECONDARY_INDEX_CASE;
import static org.smartregister.reveal.util.Constants.Action.STRUCTURE_SURVEY;
import static org.smartregister.reveal.util.Constants.BusinessStatus.COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.ENROLLED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.ENROLLED_NOT_COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.MDA_COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.STRUCTURE_PART_OF_HOH;
import static org.smartregister.reveal.util.Constants.BusinessStatus.MDA_PARTIALLY_COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.MDA_REFUSED_OR_ABSENT;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_ELIGIBLE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_SPRAYED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.PARTIALLY_SPRAYED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOTENROLLED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.MONTHTHREECOMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.MONTHSIXCOMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.SPRAYED;
import static org.smartregister.reveal.util.Constants.CONFIGURATION.LOCAL_SYNC_DONE;
import static org.smartregister.reveal.util.Constants.CONFIGURATION.UPDATE_LOCATION_BUFFER_RADIUS;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STRUCTURE_ID;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.TASK_ID;
import static org.smartregister.reveal.util.Constants.Filter.FILTER_CONFIGURATION;
import static org.smartregister.reveal.util.Constants.Filter.FILTER_SORT_PARAMS;
import static org.smartregister.reveal.util.Constants.Intervention.IRS;
import static org.smartregister.reveal.util.Constants.Intervention.IRS_VERIFICATION;
import static org.smartregister.reveal.util.Constants.Intervention.LARVAL_DIPPING;
import static org.smartregister.reveal.util.Constants.Intervention.MOSQUITO_COLLECTION;
import static org.smartregister.reveal.util.Constants.Intervention.PAOT;
import static org.smartregister.reveal.util.Constants.JSON_FORM_PARAM_JSON;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_FAMILY_PROFILE;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_FILTER_TASKS;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_GET_JSON;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_TASK_LISTS;
import static org.smartregister.reveal.util.Constants.SYNC_BACK_OFF_DELAY;
import static org.smartregister.reveal.util.Constants.VERTICAL_OFFSET;
import static org.smartregister.reveal.util.FamilyConstants.Intent.START_REGISTRATION;
import static org.smartregister.reveal.util.Utils.displayDistanceScale;
import static org.smartregister.reveal.util.Utils.getDrawOperationalAreaBoundaryAndLabel;
import static org.smartregister.reveal.util.Utils.getLocationBuffer;
import static org.smartregister.reveal.util.Utils.getSatelliteStyle;
import static org.smartregister.reveal.util.Utils.getSyncEntityString;
import static org.smartregister.reveal.util.Utils.isCurrentTargetLevelStructure;
import static org.smartregister.reveal.util.Utils.isZambiaIRSLite;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.pluginscalebar.ScaleBarOptions;
import com.mapbox.pluginscalebar.ScaleBarPlugin;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.SyncEntity;
import org.smartregister.domain.SyncProgress;
import org.smartregister.domain.Task;
import org.smartregister.dto.UserAssignmentDTO;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.receiver.SyncProgressBroadcastReceiver;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.receiver.ValidateAssignmentReceiver;
import org.smartregister.reporting.view.ProgressIndicatorView;
import org.smartregister.reveal.BuildConfig;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.BaseDrawerContract;
import org.smartregister.reveal.contract.ListTaskContract;
import org.smartregister.reveal.contract.UserLocationContract.UserLocationView;
import org.smartregister.reveal.layer.LabelLayer;
import org.smartregister.reveal.model.CardDetails;
import org.smartregister.reveal.model.FamilyCardDetails;
import org.smartregister.reveal.model.FilterConfiguration;
import org.smartregister.reveal.model.IRSVerificationCardDetails;
import org.smartregister.reveal.model.MosquitoHarvestCardDetails;
import org.smartregister.reveal.model.SprayCardDetails;
import org.smartregister.reveal.model.SurveyCardDetails;
import org.smartregister.reveal.model.TaskFilterParams;
import org.smartregister.reveal.presenter.ListTaskPresenter;
import org.smartregister.reveal.repository.RevealMappingHelper;
import org.smartregister.reveal.template.FormRecyclerFormActivity;
import org.smartregister.reveal.test.GDRSActivity;
import org.smartregister.reveal.util.AlertDialogUtils;
import org.smartregister.reveal.util.CardDetailsUtil;
import org.smartregister.reveal.util.Constants.Action;
import org.smartregister.reveal.util.Constants.Properties;
import org.smartregister.reveal.util.Constants.TaskRegister;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import org.smartregister.reveal.util.RevealMapHelper;
import org.smartregister.util.Constants;
import org.smartregister.util.NetworkUtils;
import org.smartregister.util.SyncUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import timber.log.Timber;

import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;

import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.turf.TurfTransformation;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.*;

/**
 * Created by samuelgithengi on 11/20/18.
 */
//public class ListTasksActivity extends BaseMapActivity implements ListTaskContract.ListTaskView,
//        View.OnClickListener, SyncStatusBroadcastReceiver.SyncStatusListener, UserLocationView, OnLocationComponentInitializedCallback, SyncProgressBroadcastReceiver.SyncProgressListener, ValidateAssignmentReceiver.UserAssignmentListener, OnMapReadyCallback {
public class ListTasksActivity extends BaseMapActivity implements ListTaskContract.ListTaskView,
        View.OnClickListener, SyncStatusBroadcastReceiver.SyncStatusListener, UserLocationView,
        SyncProgressBroadcastReceiver.SyncProgressListener, ValidateAssignmentReceiver.UserAssignmentListener {
    private ListTaskPresenter listTaskPresenter;

    private GeoJsonSource geoJsonSource;

    private GeoJsonSource selectedGeoJsonSource;

    private ProgressDialog progressDialog;
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1001;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 1002;
    private MapboxMap mMapboxMap;

    private CardView sprayCardView;

    private TextView tvReason;

    private PreferencesUtil preferencesUtil;

    private CardView mosquitoCollectionCardView;
    private CardView larvalBreedingCardView;
    private CardView potentialAreaOfTransmissionCardView;
    private CardView indicatorsCardView;
    private CardView irsVerificationCardView;

    private RefreshGeowidgetReceiver refreshGeowidgetReceiver = new RefreshGeowidgetReceiver();

    private SyncProgressBroadcastReceiver syncProgressBroadcastReceiver = new SyncProgressBroadcastReceiver(this);

    private boolean hasRequestedLocation;

    private boolean startedToastShown;

    private boolean completedToastShown;

    private BaseDrawerContract.View drawerView;

    private RevealJsonFormUtils jsonFormUtils;

    private LabelLayer rcdLabelLayer;

    private LabelLayer indexCaseLabelLayer;

    private LabelLayer secondaryIndexCaseLabelLayer;

    private LabelLayer householdLabelLayer;

    private RevealMapHelper revealMapHelper;

    private ImageButton myLocationButton;

    private ImageButton layerSwitcherFab;

    private ImageButton filterTasksFab;

    private FrameLayout filterCountLayout;

    private TextView filterCountTextView;

    private EditText searchView;

    private CardDetailsUtil cardDetailsUtil = new CardDetailsUtil();

    private boolean formOpening;

    private Bundle savedInstanceState = new Bundle();

    private ImageButton addStructureButton;
    private static final String TAG = "MapInitDebug";
    private FeatureCollection pendingFeatureCollection;
    private Feature pendingOperationalArea;
    private List<Feature> pendingAdjacentAreas;
    private boolean pendingIsChangeMapPosition;
    private List<Feature> pendingParentLocations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, (BuildConfig.MAPBOX_SDK_ACCESS_TOKEN));
        this.preferencesUtil = PreferencesUtil.getInstance();
        this.savedInstanceState = savedInstanceState;
        if (getCountry() == Country.THAILAND || getCountry() == Country.THAILAND_EN) {
            setContentView(R.layout.thailand_activity_list_tasks);
        } else {
            setContentView(R.layout.activity_list_tasks);
        }

        jsonFormUtils = new RevealJsonFormUtils();
        drawerView = new DrawerMenuView(this);

        revealMapHelper = new RevealMapHelper();

        listTaskPresenter = new ListTaskPresenter(this, drawerView.getPresenter());

        initializeProgressIndicatorViews();

//        kujakuMapView = findViewById(R.id.kujakuMapView);
        mapView = findViewById(R.id.mapView);
        myLocationButton = findViewById(R.id.ib_mapview_focusOnMyLocationIcon);

        layerSwitcherFab = findViewById(R.id.fab_mapview_layerSwitcher);

//        if (StringUtils.isNotBlank(PreferencesUtil.getInstance().getCurrentPlanTargetLevel())) {
//            initializeMapView(savedInstanceState);
//        }
        if (StringUtils.isNotBlank(PreferencesUtil.getInstance().getCurrentPlanTargetLevel())) {
            Log.d(TAG, "Target level valid. Calling initializeMapView.");
            initializeMapView(savedInstanceState);
        } else {
            Log.d(TAG, "Target level is BLANK. MapView NOT initialized.");
        }

        drawerView.initializeDrawerLayout();
        initializeProgressDialog();

        addStructureButton = findViewById(R.id.btn_add_structure);
        addStructureButton.setOnClickListener(this);
        findViewById(R.id.drawerMenu).setOnClickListener(this);

        initializeCardViews();

        initializeToolbar();
    }

    @NonNull
    private Country getCountry() {
        return PreferencesUtil.getInstance().getBuildCountry();
    }

    private void initializeCardViews() {
        sprayCardView = findViewById(R.id.spray_card_view);
        sprayCardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //intercept clicks and interaction of map below card view
                return true;
            }
        });

        mosquitoCollectionCardView = findViewById(R.id.mosquito_collection_card_view);

        larvalBreedingCardView = findViewById(R.id.larval_breeding_card_view);

        potentialAreaOfTransmissionCardView = findViewById(R.id.potential_area_of_transmission_card_view);

        irsVerificationCardView = findViewById(R.id.irs_verification_card_view);

        findViewById(R.id.btn_add_structure).setOnClickListener(this);

        findViewById(R.id.btn_collapse_spray_card_view).setOnClickListener(this);

        tvReason = findViewById(R.id.reason);

        findViewById(R.id.change_household_status).setOnClickListener(this);
        findViewById(R.id.change_lsm_household_status).setOnClickListener(this);
        findViewById(R.id.change_habitat_status).setOnClickListener(this);
        findViewById(R.id.change_spray_status).setOnClickListener(this);
        findViewById(R.id.change_oncho_status).setOnClickListener(this);
        findViewById(R.id.change_structure_survey_status).setOnClickListener(this);
        findViewById(R.id.change_gdrs_index_status).setOnClickListener(this);
        findViewById(R.id.change_gdrs_secondary_index_status).setOnClickListener(this);
        findViewById(R.id.change_gdrs_rcd_status).setOnClickListener(this);

        findViewById(R.id.btn_undo_spray).setOnClickListener(this);
        findViewById(R.id.btn_undo_structure_status).setOnClickListener(this);

        findViewById(R.id.register_family).setOnClickListener(this);

        findViewById(R.id.task_register).setOnClickListener(this);

        findViewById(R.id.btn_collapse_mosquito_collection_card_view).setOnClickListener(this);

        findViewById(R.id.btn_record_mosquito_collection).setOnClickListener(this);

        findViewById(R.id.btn_undo_mosquito_collection).setOnClickListener(this);

        findViewById(R.id.btn_collapse_larval_breeding_card_view).setOnClickListener(this);

        findViewById(R.id.btn_record_larval_dipping).setOnClickListener(this);

        findViewById(R.id.btn_undo_larval_dipping).setOnClickListener(this);

        findViewById(R.id.btn_collapse_paot_card_view).setOnClickListener(this);

        findViewById(R.id.btn_edit_paot_details).setOnClickListener(this);

        findViewById(R.id.btn_undo_paot_details).setOnClickListener(this);

        findViewById(R.id.btn_collapse_irs_verification_card_view).setOnClickListener(this);

        indicatorsCardView = findViewById(R.id.indicators_card_view);
        indicatorsCardView.setOnClickListener(this);

        findViewById(R.id.btn_collapse_indicators_card_view).setOnClickListener(this);

        findViewById(R.id.register_family).setOnClickListener(this);

        if (!org.smartregister.reveal.util.Utils.isCurrentTargetLevelStructure()) {
            findViewById(R.id.btn_add_structure).setVisibility(View.GONE);
        }
        if (Country.SENEGAL.equals(getCountry()) || Country.SENEGAL.equals(getCountry())) {
            sprayCardView.findViewById(R.id.btn_undo_spray).setVisibility(View.GONE);
        }
    }

    @Override
    public void closeCardView(int id) {
        if (id == R.id.btn_collapse_spray_card_view) {
            setViewVisibility(sprayCardView, false);
        } else if (id == R.id.btn_collapse_mosquito_collection_card_view) {
            setViewVisibility(mosquitoCollectionCardView, false);
        } else if (id == R.id.btn_collapse_larval_breeding_card_view) {
            setViewVisibility(larvalBreedingCardView, false);
        } else if (id == R.id.btn_collapse_paot_card_view) {
            setViewVisibility(potentialAreaOfTransmissionCardView, false);
        } else if (id == R.id.btn_collapse_indicators_card_view) {
            setViewVisibility(indicatorsCardView, false);
        } else if (id == R.id.btn_collapse_irs_verification_card_view) {
            setViewVisibility(irsVerificationCardView, false);
        }
    }

    @Override
    public void closeAllCardViews() {
        setViewVisibility(sprayCardView, false);
        setViewVisibility(mosquitoCollectionCardView, false);
        setViewVisibility(larvalBreedingCardView, false);
        setViewVisibility(potentialAreaOfTransmissionCardView, false);
        setViewVisibility(indicatorsCardView, false);
        setViewVisibility(irsVerificationCardView, false);
    }

    private void setViewVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

private void initializeMapView(Bundle savedInstanceState) {
    Log.d("MapboxDebug", "--- Initializing Native MapView ---");

    if (mapView == null) {
        Log.d("MapboxDebug", "mapView is NULL!");
        return;
    }

    try {
        mapView.onCreate(savedInstanceState);
        Log.d("MapboxDebug", "mapView.onCreate executed successfully");
    } catch (Exception e) {
        Log.e("MapboxDebug", "CRITICAL: Error in mapView.onCreate", e);
    }

    // Single OnMapReadyCallback registered here
    mapView.getMapAsync(new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull MapboxMap mapboxMap) {
            Log.d("MapboxDebug", "MapboxMap instance is ready!");
            mMapboxMap = mapboxMap;

            String satelliteStyle = getSatelliteStyle(getContext());
            Log.d("MapboxDebug", "Attempting to load Satellite Style URI: " + satelliteStyle);

            if (StringUtils.isBlank(satelliteStyle)) {
                Log.e("MapboxDebug", "ERROR: Satellite Style URI is NULL or EMPTY!");
                return;
            }

            Style.Builder builder = new Style.Builder().fromUri(satelliteStyle);
            mapboxMap.setStyle(builder, new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    Log.d("MapboxDebug", "SUCCESS: Mapbox Style fully loaded!");

                    // 1. UI & Controls
                    enableCompass(mapboxMap);

                    // 2. Resolve Data Sources
                    geoJsonSource = style.getSourceAs(getString(R.string.reveal_datasource_name));
                    selectedGeoJsonSource = style.getSourceAs(getString(R.string.selected_datasource_name));

                    // 3. Custom Map Layers & Satellite Basemap (Using your refactored helper)
                    RevealMapHelper.addCustomLayers(style, ListTasksActivity.this);
                    RevealMapHelper.addBaseLayers(mapView, style, ListTasksActivity.this); // 👈 This injects satellite tiles

                    // 4. Country UI Configs
                    if (getBuildCountry() != Country.ZAMBIA
                            && getBuildCountry() != Country.SENEGAL
                            && getBuildCountry() != Country.SENEGAL_EN
                            && getBuildCountry() != Country.NIGERIA) {
                        if (layerSwitcherFab != null) {
                            layerSwitcherFab.setVisibility(View.GONE);
                        }
                    }

                    // 5. Plugins & Location Component
                    initializeScaleBarPlugin(mapboxMap);
                    enableLocationComponent(style);

                    if (pendingFeatureCollection != null) {
                        FeatureCollection fc = pendingFeatureCollection;
                        Feature oa = pendingOperationalArea;
                        List<Feature> adj = pendingAdjacentAreas;
                        boolean isChangePos = pendingIsChangeMapPosition;
                        List<Feature> parents = pendingParentLocations;

                        pendingFeatureCollection = null;
                        pendingOperationalArea = null;
                        pendingAdjacentAreas = null;
                        pendingParentLocations = null;

                        setGeoJsonSourceWithParents(fc, oa, adj, isChangePos, parents);
                    } else {
                        listTaskPresenter.onMapReady();
                    }
                }
            });


            // Zoom Preferences
            mapboxMap.setMinZoomPreference(3);
            mapboxMap.setMaxZoomPreference(21);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .zoom(16)
                    .build();
            mapboxMap.setCameraPosition(cameraPosition);

            // Gesture Listeners
            mapboxMap.addOnMapClickListener(point -> {
                listTaskPresenter.onMapClicked(mapboxMap, point, false);
                return false;
            });

            mapboxMap.addOnMapLongClickListener(point -> {
                listTaskPresenter.onMapClicked(mapboxMap, point, true);
                return false;
            });

            positionMyLocationAndLayerSwitcher();
        }
    });
}
    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this) && mMapboxMap != null) {
            LocationComponent locationComponent = mMapboxMap.getLocationComponent();

            // Activate LocationComponent with default options
            LocationComponentActivationOptions options = LocationComponentActivationOptions
                    .builder(this, loadedMapStyle)
                    .build();

            locationComponent.activateLocationComponent(options);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        }
    }


    protected void enableCompass(MapboxMap mapboxMap) {
        UiSettings uiSettings = mapboxMap.getUiSettings();

        uiSettings.setCompassGravity(Gravity.START | Gravity.TOP);
        uiSettings.setCompassMargins(getResources().getDimensionPixelSize(R.dimen.compass_left_margin),
                getResources().getDimensionPixelSize(R.dimen.compass_top_margin), 0, 0);
        uiSettings.setCompassFadeFacingNorth(false);
        uiSettings.setCompassEnabled(true);
    }


protected void initializeScaleBarPlugin(MapboxMap mapboxMap) {
    if (displayDistanceScale() && mapView != null) {
        ScaleBarPlugin scaleBarPlugin = new ScaleBarPlugin(mapView, mapboxMap);
        // Create a ScaleBarOptions object to use custom styling
        ScaleBarOptions scaleBarOptions = new ScaleBarOptions(getContext());
        scaleBarOptions.setTextColor(R.color.distance_scale_text);
        scaleBarOptions.setTextSize(R.dimen.distance_scale_text_size);

        scaleBarPlugin.create(scaleBarOptions);
    }
}
    private void positionMyLocationAndLayerSwitcher(FrameLayout.LayoutParams myLocationButtonParams, int bottomMargin) {

        if (myLocationButton != null) {
            myLocationButtonParams.gravity = Gravity.BOTTOM | Gravity.END;
            myLocationButtonParams.bottomMargin = bottomMargin;
            myLocationButtonParams.topMargin = 0;
            myLocationButton.setLayoutParams(myLocationButtonParams);
        }

    }

//    public void positionMyLocationAndLayerSwitcher() {
//        FrameLayout.LayoutParams myLocationButtonParams = (FrameLayout.LayoutParams) myLocationButton.getLayoutParams();
//        if (!List.of(Country.MALI, Country.ZAMBIA, Country.NAMIBIA, Country.SENEGAL, Country.RWANDA,
//                        Country.SENEGAL_EN, Country.RWANDA_EN, Country.NIGERIA, Country.GDRS, Country.NIH)
//                .contains(getBuildCountry())) {
//            positionMyLocationAndLayerSwitcher(myLocationButtonParams, myLocationButtonParams.topMargin);
//        } else {
//            int progressHeight = getResources().getDimensionPixelSize(R.dimen.progress_height);
//
//            int bottomMargin = (org.smartregister.reveal.util.Utils.getInterventionLabel() == R.string.irs || org.smartregister.reveal.util.Utils.getInterventionLabel() == R.string.mda || org.smartregister.reveal.util.Utils.getInterventionLabel() == R.string.survey_coverage) ? progressHeight + 40 : 40;
//            positionMyLocationAndLayerSwitcher(myLocationButtonParams, bottomMargin);
//
//            if (layerSwitcherFab != null) {
//                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layerSwitcherFab.getLayoutParams();
//                //position the layer selector above location button and with similar bottom margin
//                if (org.smartregister.reveal.util.Utils.getInterventionLabel() == R.string.irs)
//                    params.bottomMargin = myLocationButton.getMeasuredHeight() + progressHeight + 80;
//                else
//                    params.bottomMargin = myLocationButton.getMeasuredHeight() + bottomMargin + 40;
//                //Make the layer selector is same size as my location button
//                params.height = myLocationButton.getMeasuredHeight();
//                params.width = myLocationButton.getMeasuredWidth();
//                params.rightMargin = getResources().getDimensionPixelOffset(R.dimen.my_location_btn_margin);
//                layerSwitcherFab.setScaleType(FloatingActionButton.ScaleType.CENTER);
//                layerSwitcherFab.setLayoutParams(params);
//            }
//        }
//    }
public void positionMyLocationAndLayerSwitcher() {
    // Guard against null reference if the view hasn't been inflated yet
    if (myLocationButton == null) {
        myLocationButton = findViewById(R.id.ib_mapview_focusOnMyLocationIcon);
        if (myLocationButton == null || myLocationButton.getLayoutParams() == null) {
            return;
        }
    }

    FrameLayout.LayoutParams myLocationButtonParams = (FrameLayout.LayoutParams) myLocationButton.getLayoutParams();
    if (!List.of(Country.MALI, Country.ZAMBIA, Country.NAMIBIA, Country.SENEGAL, Country.RWANDA,
                    Country.SENEGAL_EN, Country.RWANDA_EN, Country.NIGERIA, Country.GDRS, Country.NIH)
            .contains(getBuildCountry())) {
        positionMyLocationAndLayerSwitcher(myLocationButtonParams, myLocationButtonParams.topMargin);
    } else {
        int progressHeight = getResources().getDimensionPixelSize(R.dimen.progress_height);

        int bottomMargin = (org.smartregister.reveal.util.Utils.getInterventionLabel() == R.string.irs || org.smartregister.reveal.util.Utils.getInterventionLabel() == R.string.mda || org.smartregister.reveal.util.Utils.getInterventionLabel() == R.string.survey_coverage) ? progressHeight + 40 : 40;
        positionMyLocationAndLayerSwitcher(myLocationButtonParams, bottomMargin);

        if (layerSwitcherFab != null && layerSwitcherFab.getLayoutParams() != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layerSwitcherFab.getLayoutParams();
            //position the layer selector above location button and with similar bottom margin
            if (org.smartregister.reveal.util.Utils.getInterventionLabel() == R.string.irs)
                params.bottomMargin = myLocationButton.getMeasuredHeight() + progressHeight + 80;
            else
                params.bottomMargin = myLocationButton.getMeasuredHeight() + bottomMargin + 40;
            //Make the layer selector is same size as my location button
            params.height = myLocationButton.getMeasuredHeight();
            params.width = myLocationButton.getMeasuredWidth();
            params.rightMargin = getResources().getDimensionPixelOffset(R.dimen.my_location_btn_margin);
            layerSwitcherFab.setScaleType(FloatingActionButton.ScaleType.CENTER);
            layerSwitcherFab.setLayoutParams(params);
        }
    }
}

    private void initializeProgressIndicatorViews() {
        LinearLayout progressIndicatorsGroupView = findViewById(R.id.progressIndicatorsGroupView);
        progressIndicatorsGroupView.setBackgroundColor(this.getResources().getColor(R.color.transluscent_white));
        progressIndicatorsGroupView.setOnClickListener(this);
    }

    private void initializeToolbar() {
        searchView = findViewById(R.id.edt_search);
        searchView.setSingleLine();
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {//do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                listTaskPresenter.searchTasks(s.toString().trim());
            }
        });
        filterTasksFab = findViewById(R.id.filter_tasks_fab);
        filterCountLayout = findViewById(R.id.filter_tasks_count_layout);
        filterCountTextView = findViewById(R.id.filter_tasks_count);

        filterTasksFab.setOnClickListener(this);
        filterCountLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_add_structure) {
            listTaskPresenter.onAddStructureClicked(revealMapHelper.isMyLocationComponentActive(this, myLocationButton));
        } else if (v.getId() == R.id.change_spray_status) {
            listTaskPresenter.onChangeInterventionStatus(IRS);
        } else if (v.getId() == R.id.change_structure_survey_status) {
            listTaskPresenter.onChangeInterventionStatus(STRUCTURE_SURVEY);
        } else if (v.getId() == R.id.change_household_status) {
            listTaskPresenter.onChangeInterventionStatus(MDA_SURVEY);
        } else if (v.getId() == R.id.change_gdrs_rcd_status) {
            listTaskPresenter.onChangeInterventionStatus(RCD);
        } else if (v.getId() == R.id.change_gdrs_index_status) {
            listTaskPresenter.onChangeInterventionStatus(INDEX_CASE);
        } else if (v.getId() == R.id.change_gdrs_secondary_index_status) {
            listTaskPresenter.onChangeInterventionStatus(SECONDARY_INDEX_CASE);
        } else if (v.getId() == R.id.change_habitat_status) {
            listTaskPresenter.onChangeInterventionStatus(HABITAT_SURVEY);
        } else if (v.getId() == R.id.change_lsm_household_status) {
            listTaskPresenter.onChangeInterventionStatus(LSM_HOUSEHOLD_SURVEY);
        } else if (v.getId() == R.id.change_oncho_status) {
            listTaskPresenter.onChangeInterventionStatus(MDA_ONCHOCERCIASIS_SURVEY);
        } else if (v.getId() == R.id.btn_undo_spray) {
            if (isZambiaIRSLite()) {
                displayResetInterventionTaskDialog(IRS_VERIFICATION);
            } else {
                displayResetInterventionTaskDialog(IRS);
            }
        } else if (v.getId() == R.id.btn_undo_structure_status) {
            displayResetInterventionTaskDialog(STRUCTURE_SURVEY);
        } else if (v.getId() == R.id.btn_record_mosquito_collection) {
            listTaskPresenter.onChangeInterventionStatus(MOSQUITO_COLLECTION);
        } else if (v.getId() == R.id.btn_undo_mosquito_collection) {
            displayResetInterventionTaskDialog(MOSQUITO_COLLECTION);
        } else if (v.getId() == R.id.btn_record_larval_dipping) {
            listTaskPresenter.onChangeInterventionStatus(LARVAL_DIPPING);
        } else if (v.getId() == R.id.btn_undo_larval_dipping) {
            displayResetInterventionTaskDialog(LARVAL_DIPPING);
        } else if (v.getId() == R.id.btn_edit_paot_details) {
            listTaskPresenter.onChangeInterventionStatus(PAOT);
        } else if (v.getId() == R.id.btn_undo_paot_details) {
            displayResetInterventionTaskDialog(PAOT);
        } else if (v.getId() == R.id.btn_collapse_spray_card_view) {
            setViewVisibility(tvReason, false);
            closeCardView(v.getId());
        } else if (v.getId() == R.id.register_family) {
            registerFamily();
            closeCardView(R.id.btn_collapse_spray_card_view);
        } else if (v.getId() == R.id.btn_collapse_mosquito_collection_card_view
                || v.getId() == R.id.btn_collapse_larval_breeding_card_view
                || v.getId() == R.id.btn_collapse_paot_card_view
                || v.getId() == R.id.btn_collapse_indicators_card_view
                || v.getId() == R.id.btn_collapse_irs_verification_card_view) {
            closeCardView(v.getId());
        } else if (v.getId() == R.id.task_register) {
            listTaskPresenter.onOpenTaskRegisterClicked();
        } else if (v.getId() == R.id.drawerMenu) {
            drawerView.openDrawerLayout();
        } else if (v.getId() == R.id.progressIndicatorsGroupView) {
            openIndicatorsCardView();
        } else if (v.getId() == R.id.filter_tasks_fab || v.getId() == R.id.filter_tasks_count_layout) {
            listTaskPresenter.onFilterTasksClicked();
        }
    }

    @Override
    public void openFilterTaskActivity(TaskFilterParams filterParams) {
        Intent intent = new Intent(getContext(), FilterTasksActivity.class);
        intent.putExtra(FILTER_SORT_PARAMS, filterParams);
        FilterConfiguration.FilterConfigurationBuilder builder = FilterConfiguration.builder();
        if (getCountry().equals(Country.NAMIBIA)) {
            builder.taskCodeLayoutEnabled(false)
                    .interventionTypeLayoutEnabled(false)
                    .businessStatusList(Arrays.asList(NOT_VISITED, NOT_SPRAYED, PARTIALLY_SPRAYED, SPRAYED))
                    .sortOptions(R.array.task_sort_options_namibia);
        } else if (getCountry().equals(Country.UW)) {
            builder.taskCodeLayoutEnabled(false)
                    .interventionTypeLayoutEnabled(false)
                    .businessStatusList(Arrays.asList(NOT_VISITED, MONTHTHREECOMPLETE
                            , MONTHSIXCOMPLETE, ENROLLED, NOT_ELIGIBLE, ENROLLED_NOT_COMPLETE, NOTENROLLED))
                    .sortOptions(R.array.task_sort_options_uw);
        } else if (getCountry().equals(Country.VL_ZM)) {
            builder.taskCodeLayoutEnabled(false)
                    .interventionTypeLayoutEnabled(false)
                    .businessStatusList(Arrays.asList(NOT_VISITED, NOT_ELIGIBLE, COMPLETE))
                    .sortOptions(R.array.task_sort_options_uw);
        } else if (getCountry().equals(Country.MALI)) {
            builder.taskCodeLayoutEnabled(false)
                    .interventionTypeLayoutEnabled(false)
                    .businessStatusList(Arrays.asList(NOT_VISITED, NOT_ELIGIBLE, COMPLETE, STRUCTURE_PART_OF_HOH, MDA_REFUSED_OR_ABSENT, MDA_PARTIALLY_COMPLETE, MDA_COMPLETE))
                    .sortOptions(R.array.task_sort_options_mali);
        }
        intent.putExtra(FILTER_CONFIGURATION, builder.build());
        startActivityForResult(intent, REQUEST_CODE_FILTER_TASKS);
    }

    private void openIndicatorsCardView() {
        setViewVisibility(indicatorsCardView, true);
    }

    @Override
    public void openTaskRegister(TaskFilterParams filterParams) {
        Intent intent = new Intent(this, TaskRegisterActivity.class);
        intent.putExtra(TaskRegister.INTERVENTION_TYPE, getString(listTaskPresenter.getInterventionLabel()));
        if (getUserCurrentLocation() != null) {
            intent.putExtra(TaskRegister.LAST_USER_LOCATION, getUserCurrentLocation());
        }
        if (filterParams != null) {
            filterParams.setSearchPhrase(searchView.getText().toString());
            intent.putExtra(FILTER_SORT_PARAMS, filterParams);
        } else if (StringUtils.isNotBlank(searchView.getText())) {
            intent.putExtra(FILTER_SORT_PARAMS, TaskFilterParams.builder().searchPhrase(searchView.getText().toString()).build());
        }
        startActivityForResult(intent, REQUEST_CODE_TASK_LISTS);
    }


    @Override
    public void openStructureProfile(CommonPersonObjectClient family) {

        Intent intent = new Intent(getActivity(), Utils.metadata().profileActivity);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, family.getCaseId());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_HEAD, Utils.getValue(family.getColumnmaps(), DBConstants.KEY.FAMILY_HEAD, false));
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.PRIMARY_CAREGIVER, Utils.getValue(family.getColumnmaps(), DBConstants.KEY.PRIMARY_CAREGIVER, false));
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_NAME, Utils.getValue(family.getColumnmaps(), DBConstants.KEY.FIRST_NAME, false));
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.GO_TO_DUE_PAGE, false);


        intent.putExtra(Properties.LOCATION_UUID, listTaskPresenter.getSelectedFeature().id());
        intent.putExtra(Properties.TASK_IDENTIFIER, listTaskPresenter.getSelectedFeature().getStringProperty(Properties.TASK_IDENTIFIER));
        intent.putExtra(Properties.TASK_BUSINESS_STATUS, listTaskPresenter.getSelectedFeature().getStringProperty(Properties.TASK_BUSINESS_STATUS));
        intent.putExtra(Properties.TASK_STATUS, listTaskPresenter.getSelectedFeature().getStringProperty(Properties.TASK_STATUS));

        startActivityForResult(intent, REQUEST_CODE_FAMILY_PROFILE);
    }


    @Override
    public void registerFamily() {
        clearSelectedFeature();
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        intent.putExtra(START_REGISTRATION, true);
        Feature feature = listTaskPresenter.getSelectedFeature();
        intent.putExtra(Properties.LOCATION_UUID, feature.id());
        intent.putExtra(Properties.TASK_IDENTIFIER, feature.getStringProperty(Properties.TASK_IDENTIFIER));
        intent.putExtra(Properties.TASK_BUSINESS_STATUS, feature.getStringProperty(Properties.TASK_BUSINESS_STATUS));
        intent.putExtra(Properties.TASK_STATUS, feature.getStringProperty(Properties.TASK_STATUS));
        if (feature.hasProperty(Properties.STRUCTURE_NAME))
            intent.putExtra(Properties.STRUCTURE_NAME, feature.getStringProperty(Properties.STRUCTURE_NAME));
        startActivity(intent);

    }

    @Override
    public void openRCD() {
        clearSelectedFeature();
        Intent intent = new Intent(this, GDRSActivity.class);
        Feature feature = listTaskPresenter.getSelectedFeature();

        intent.putExtra(Properties.LOCATION_UUID, feature.id());
        intent.putExtra(Properties.TASK_IDENTIFIER, feature.getStringProperty(Properties.TASK_IDENTIFIER));
        intent.putExtra(Properties.TASK_BUSINESS_STATUS, feature.getStringProperty(Properties.TASK_BUSINESS_STATUS));
        intent.putExtra(Properties.TASK_CODE, feature.getStringProperty(Properties.TASK_CODE));
        startActivity(intent);
    }

    public void openFormByTemplate(String formTemplate){
        Timber.tag("TestFrag").i("ListTaskActivity openFormByTemplate feature clicked");

//        if (formTemplate == "Form-List-Form"){
            openTemplate();
//        }
    }


    /**
     * Opens {@link org.smartregister.reveal.template.GDRSFormRecyclerActivity} for the
     * selected feature — the template-based replacement for {@link #openRCD()}.
     *
     * <p>The parent form name is resolved from the task code so the correct
     * JSON form is embedded at the top of the template screen.
     */
    public void openGDRSTemplate() {
        clearSelectedFeature();
        Feature feature = listTaskPresenter.getSelectedFeature();

        Intent intent = new Intent(this,
                org.smartregister.reveal.template.GDRSFormRecyclerActivity.class);
        intent.putExtra(
                org.smartregister.reveal.template.GDRSFormRecyclerActivity.EXTRA_PARENT_FORM_NAME,
//            feature.getStringProperty(Properties.FORM_FOR_TASK));
            "gdrs_index_case.json");
        intent.putExtra(Properties.LOCATION_UUID,
                feature.id());
        intent.putExtra(Properties.TASK_IDENTIFIER,
                feature.getStringProperty(Properties.TASK_IDENTIFIER));
        intent.putExtra(Properties.TASK_CODE,
                feature.getStringProperty(Properties.TASK_CODE));

        startActivity(intent);
    }

    public void openTemplate() {
        clearSelectedFeature();
        Feature feature = listTaskPresenter.getSelectedFeature();

        Intent intent = new Intent(this, FormRecyclerFormActivity.class);
        String upperForm = feature.getStringProperty(Properties.FORM_FOR_TASK);
        Timber.tag("TestFrag").i("ListTasksActivity openTemplate form %s",upperForm);
        intent.putExtra(FormRecyclerFormActivity.EXTRA_FORM_NAME, "json.form/"+ upperForm);

        intent.putExtra(FormRecyclerFormActivity.EXTRA_PARENT_TASK_ID, feature.getStringProperty(Properties.TASK_IDENTIFIER));
        intent.putExtra(FormRecyclerFormActivity.EXTRA_LOCATION_UUID, feature.id());

        if (upperForm.equals("sp_zm_enrolment_formA.json")){
            intent.putExtra(FormRecyclerFormActivity.EXTRA_CHILD_TASK_CODE, "Enrolment Structure");
            intent.putExtra(FormRecyclerFormActivity.EXTRA_GATE_FIELD_KEYS, "respondent_consent_enrolment:yes|respondent_consent_destruction:yes");

            intent.putExtra(FormRecyclerFormActivity.EXTRA_CHILD_FORM_NAME, "json.form/sp_zm_enrolment_formB.json");
            intent.putExtra(FormRecyclerFormActivity.EXTRA_RECYCLER_GATE_KEYS, "respondent_consent_enrolment:yes");
        } else {
            intent.putExtra(FormRecyclerFormActivity.EXTRA_CHILD_TASK_CODE, "Coverage Structure");
            intent.putExtra(FormRecyclerFormActivity.EXTRA_GATE_FIELD_KEYS, "permission_enter:yes_all,yes_some,no");

            intent.putExtra(FormRecyclerFormActivity.EXTRA_CHILD_FORM_NAME, "json.form/sp_zm_coverage_formB.json");
            intent.putExtra(FormRecyclerFormActivity.EXTRA_RECYCLER_GATE_KEYS, "permission_enter:yes_all,yes_some");
        }

        intent.putExtra(FormRecyclerFormActivity.EXTRA_BUSINESS_STATUS_FIELD, "business_status");
        intent.putExtra(FormRecyclerFormActivity.EXTRA_RECYCLER_HEADER, "Structures");
        intent.putExtra(FormRecyclerFormActivity.EXTRA_COUNT_LABEL,
            "14.1. How many separate structures are there in this household? "
                + "Do not include any livestock shelters or storage areas.");

        intent.putExtra(FormRecyclerFormActivity.EXTRA_EMPTY_MESSAGE,
            "No structures generated yet. Enter the number above and tap Generate.");
        intent.putExtra(FormRecyclerFormActivity.EXTRA_COUNT_HINT, "enter number");

        startActivity(intent);
    }



    @Override
    public void setGeoJsonSourceWithParents(@NonNull FeatureCollection featureCollection,
                                            Feature operationalArea,
                                            List<Feature> adjacentOperationalAreas,
                                            boolean isChangeMapPosition,
                                            List<Feature> parentLocations) {

        // 1. Toggle button visibility
        if (StringUtils.isNotBlank(PreferencesUtil.getInstance().getCurrentPlanTargetLevel()) && isChangeMapPosition) {
            if (addStructureButton != null) {
                addStructureButton.setVisibility(org.smartregister.reveal.util.Utils.isCurrentTargetLevelStructure() ? View.VISIBLE : View.GONE);
            }
        }

        // 2. GUARD: If map or source is null, cache for post-style initialization
        if (geoJsonSource == null || mMapboxMap == null) {
            Log.w("MapboxDebug", "Map or GeoJsonSource not ready yet! Caching data for flush after style load...");
            this.pendingFeatureCollection = featureCollection;
            this.pendingOperationalArea = operationalArea;
            this.pendingAdjacentAreas = adjacentOperationalAreas;
            this.pendingIsChangeMapPosition = isChangeMapPosition;
            this.pendingParentLocations = parentLocations;
            return;
        }

        // Clear pending cache when actively rendering
        this.pendingFeatureCollection = null;

        // 3. Update GeoJSON Source
        geoJsonSource.setGeoJson(featureCollection);

        // 4. Update style layers inside getStyle callback
        mMapboxMap.getStyle(style -> {
            if (!style.isFullyLoaded()) return;

            if (operationalArea != null) {
                CameraPosition cameraPosition = mMapboxMap.getCameraForGeometry(operationalArea.geometry());
                if (listTaskPresenter.getInterventionLabel() == R.string.focus_investigation) {
                    Feature indexCase = revealMapHelper.getIndexCase(featureCollection);
                    if (indexCase != null) {
                        Location center = new RevealMappingHelper().getCenter(indexCase.geometry().toJson());
                        double currentZoom = mMapboxMap.getCameraPosition().zoom;
                        cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(center.getLatitude(), center.getLongitude()))
                                .zoom(currentZoom)
                                .build();
                    }
                }

                boolean isInitialLoad = style.getLayer("operational-area-layer-fill") == null;
                if (cameraPosition != null && (isInitialLoad || isChangeMapPosition)) {
                    mMapboxMap.setCameraPosition(cameraPosition);
                }

                // GDRS Plan Label Layers
                if ("TRUE".equals(this.preferencesUtil.isGdrsPlan())) {
                    if (rcdLabelLayer != null) rcdLabelLayer.removeLayerOnMap(mMapboxMap);
                    rcdLabelLayer = createRcdLabelLayer(featureCollection);
                    if (rcdLabelLayer != null) rcdLabelLayer.addLayerToMap(mMapboxMap);

                    if (indexCaseLabelLayer != null) indexCaseLabelLayer.removeLayerOnMap(mMapboxMap);
                    indexCaseLabelLayer = createIndexCaseLabelLayer(featureCollection);
                    if (indexCaseLabelLayer != null) indexCaseLabelLayer.addLayerToMap(mMapboxMap);

                    if (secondaryIndexCaseLabelLayer != null) secondaryIndexCaseLabelLayer.removeLayerOnMap(mMapboxMap);
                    secondaryIndexCaseLabelLayer = createSecondaryIndexCaseLabelLayer(featureCollection);
                    if (secondaryIndexCaseLabelLayer != null) secondaryIndexCaseLabelLayer.addLayerToMap(mMapboxMap);

                    if (householdLabelLayer != null) householdLabelLayer.removeLayerOnMap(mMapboxMap);
                    householdLabelLayer = createHouseholdLabelLayer(featureCollection);
                    if (householdLabelLayer != null) householdLabelLayer.addLayerToMap(mMapboxMap);
                }

                // Operational Area Boundaries
                if (getDrawOperationalAreaBoundaryAndLabel()) {
                    if (operationalArea.geometry() instanceof MultiPolygon) {
                        FeatureCollection featureCollectionOperationalArea = revealMapHelper.splitMultiPolygonsToSingleFeatureCollection(operationalArea);
                        drawBoundaryFromFeatureCollection(style, featureCollectionOperationalArea);
                    } else {
                        drawBoundary(style, operationalArea);
                    }

                    if (parentLocations != null && !parentLocations.isEmpty()) {
                        Map<String, List<Feature>> featureCollectionMapFromMultiPolygonList = revealMapHelper.getFeatureCollectionMapFromMultiPolygonList(parentLocations);
                        Set<String> levels = featureCollectionMapFromMultiPolygonList.keySet();

                        int[] colorResIds = {
                                R.color.parent_boundary_grey_1,
                                R.color.parent_boundary_grey_2,
                                R.color.parent_boundary_grey_3,
                                R.color.parent_boundary_grey_4,
                                R.color.parent_boundary_grey_5,
                                R.color.parent_boundary_grey_6
                        };

                        int counter = 0;
                        for (String level : levels) {
                            String layerBelow = style.getLayer("operational-area-layer-fill") != null ? "operational-area-layer-fill" : null;
                            List<Feature> parentLevelFeatures = featureCollectionMapFromMultiPolygonList.get(level);
                            boolean showLabel = true;

                            if (parentLevelFeatures != null && !parentLevelFeatures.isEmpty()) {
                                FeatureCollection parentLevelFeatureCollection = FeatureCollection.fromFeatures(parentLevelFeatures);

                                if (operationalArea.properties() != null && operationalArea.properties().has("geographicLevel")) {
                                    String geographicLevelOperationalArea = operationalArea.properties().get("geographicLevel").getAsString();

                                    if (parentLevelFeatures.get(0) != null && parentLevelFeatures.get(0).properties() != null
                                            && parentLevelFeatures.get(0).properties().has("geographicLevel")) {

                                        String geographicLevelParent = parentLevelFeatures.get(0).properties().get("geographicLevel").getAsString();
                                        if (geographicLevelParent.equals(geographicLevelOperationalArea) || "village".equals(geographicLevelParent)) {
                                            showLabel = false;
                                        }
                                    }
                                }

                                drawParentBoundaryFromFeatureCollectionWithColor(
                                        style,
                                        parentLevelFeatureCollection,
                                        "parent-source-" + level,
                                        "parent-layer-" + level,
                                        layerBelow,
                                        colorResIds[counter % colorResIds.length],
                                        showLabel
                                );
                            }
                            counter++;
                        }
                    }

                    if (adjacentOperationalAreas != null && !adjacentOperationalAreas.isEmpty()) {
                        drawNeighbouringBoundary(style, adjacentOperationalAreas);
                    }
                }

                // IRS Lite OA Boundaries
                if (!org.smartregister.reveal.util.Utils.isCurrentTargetLevelStructure()) {
                    Map<String, String> featureToLayerMapping = new HashMap<>();
                    for (Feature feature : featureCollection.features()) {
                        drawIRSLiteOABoundaryLayer(style, feature);
                        if (feature.id() != null) {
                            featureToLayerMapping.put("irs-lite-layer-" + feature.id(), feature.id());
                        }
                    }
                }

                // Focus investigation layers
                if (listTaskPresenter.getInterventionLabel() == R.string.focus_investigation && revealMapHelper.getIndexCaseLineLayer() == null) {
                    revealMapHelper.addIndexCaseLayers(mMapboxMap, getContext(), featureCollection);
                } else {
                    revealMapHelper.updateIndexCaseLayers(mMapboxMap, featureCollection, this);
                }

                mMapboxMap.addOnCameraIdleListener(this::logZoomLevel);
            }
        });
    }

private void logZoomLevel() {
        double currentZoom = mMapboxMap.getCameraPosition().zoom;
//        Timber.tag("batching").d("Current zoom level: %f", currentZoom);
    }



    private LabelLayer createRcdLabelLayer(FeatureCollection structures) {
        return new LabelLayer.Builder(structures)
                .setLabelProperty("rcdCountKey")
                .setLabelColorInt(Color.YELLOW)
                .setTextOffSetTuple(new Float[]{0.5f,0f})
                .build();
    }

    private LabelLayer createIndexCaseLabelLayer(FeatureCollection structures) {
        return new LabelLayer.Builder(structures)
                .setLabelProperty("indexCaseCountKey")
                .setLabelColorInt(Color.CYAN)
                .setTextOffSetTuple(new Float[]{-0.5f,0f})
                .build();
    }

    private LabelLayer createSecondaryIndexCaseLabelLayer(FeatureCollection structures) {
        return new LabelLayer.Builder(structures)
                .setLabelProperty("secondaryIndexCaseCountKey")
                .setLabelColorInt(Color.BLUE)
                .setTextOffSetTuple(new Float[]{-0.5f,1.0f})
                .build();
    }


    private LabelLayer createHouseholdLabelLayer(FeatureCollection structures) {
        return new LabelLayer.Builder(structures)
                .setLabelProperty("isAHousehold")
                .setLabelColorInt(Color.BLACK)
                .build();
    }



    @Override
    public void displayNotification(int title, int message, Object... formatArgs) {
        AlertDialogUtils.displayNotification(this, title, message, formatArgs);
    }

    @Override
    public void displayNotification(String message) {
        AlertDialogUtils.displayNotification(this, message);
    }

    @Override
    public void openCardView(CardDetails cardDetails) {
        if (cardDetails instanceof SprayCardDetails) {
            cardDetailsUtil.populateSprayCardTextViews((SprayCardDetails) cardDetails, this);
            sprayCardView.setVisibility(View.VISIBLE);
        } else if (cardDetails instanceof MosquitoHarvestCardDetails) {
            cardDetailsUtil.populateAndOpenMosquitoHarvestCard((MosquitoHarvestCardDetails) cardDetails, this);
        } else if (cardDetails instanceof IRSVerificationCardDetails) {
            cardDetailsUtil.populateAndOpenIRSVerificationCard((IRSVerificationCardDetails) cardDetails, this);
        } else if (cardDetails instanceof FamilyCardDetails) {
            cardDetailsUtil.populateFamilyCard((FamilyCardDetails) cardDetails, this);
            sprayCardView.setVisibility(View.VISIBLE);
        } else if (cardDetails instanceof SurveyCardDetails) {
            cardDetailsUtil.populateSurveyCard((SurveyCardDetails) cardDetails, this);
            sprayCardView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void startJsonForm(JSONObject form) {
        if (!formOpening) {
            jsonFormUtils.startJsonForm(form, this);
            formOpening = true;
        }
    }

    @Override
    public void displaySelectedFeature(Feature feature, LatLng point) {
        displaySelectedFeature(feature, point, mMapboxMap.getCameraPosition().zoom);
    }


@Override
public void displaySelectedFeature(Feature feature, LatLng clickedPoint, double zoomlevel) {
    adjustFocusPoint(clickedPoint);

    if (mMapboxMap != null) {
        mMapboxMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(clickedPoint, zoomlevel),
                ANIMATE_TO_LOCATION_DURATION
        );
    }

    if (selectedGeoJsonSource != null && org.smartregister.reveal.util.Utils.isCurrentTargetLevelStructure()) {
        selectedGeoJsonSource.setGeoJson(FeatureCollection.fromFeature(feature));
    }
}

    private void adjustFocusPoint(LatLng point) {
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL || screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            point.setLatitude(point.getLatitude() + VERTICAL_OFFSET);
        }
    }

//    @Override
//    public void clearSelectedFeature() {
//        if (selectedGeoJsonSource != null) {
//            try {
//                selectedGeoJsonSource.setGeoJson(new com.cocoahero.android.geojson.FeatureCollection().toJSON().toString());
//            } catch (JSONException e) {
//                Timber.tag("Reveal Exception").w(e, "Error clearing selected feature");
//            }
//        }
//    }
@Override
public void clearSelectedFeature() {
    if (selectedGeoJsonSource != null) {
        // Fixed: Use native Mapbox FeatureCollection with an empty list instead of Cocoahero
        selectedGeoJsonSource.setGeoJson(
                com.mapbox.geojson.FeatureCollection.fromFeatures(new java.util.ArrayList<>()).toJson()
        );
    }
}

    @Override
    public void displayToast(@StringRes int resourceId) {
        Toast.makeText(this, resourceId, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " hasRequestedLocation=" + hasRequestedLocation);

        if (requestCode == REQUEST_CODE_GET_JSON && resultCode == RESULT_OK && data.hasExtra(JSON_FORM_PARAM_JSON)) {
            String json = data.getStringExtra(JSON_FORM_PARAM_JSON);
            Timber.tag("WriteValue").d("ListTasksActivity onActivityResult json %s",json);
            listTaskPresenter.saveJsonForm(json);
        } else if (requestCode == Constants.RequestCode.LOCATION_SETTINGS && hasRequestedLocation) {
            Log.d(TAG, "LOCATION_SETTINGS branch reached, resultCode=" + resultCode);

            if (resultCode == RESULT_OK) {
                listTaskPresenter.getLocationPresenter().waitForUserLocation();
            } else if (resultCode == RESULT_CANCELED) {
                listTaskPresenter.getLocationPresenter().onGetUserLocationFailed();
            }
            hasRequestedLocation = false;
        } else if (requestCode == REQUEST_CODE_FAMILY_PROFILE && resultCode == RESULT_OK && data.hasExtra(STRUCTURE_ID)) {
            String structureId = data.getStringExtra(STRUCTURE_ID);
            Task task = (Task) data.getSerializableExtra(TASK_ID);
            listTaskPresenter.resetFeatureTasks(structureId, task);
        } else if (requestCode == REQUEST_CODE_FILTER_TASKS && resultCode == RESULT_OK && data.hasExtra(FILTER_SORT_PARAMS)) {
            TaskFilterParams filterParams = (TaskFilterParams) data.getSerializableExtra(FILTER_SORT_PARAMS);
            listTaskPresenter.filterTasks(filterParams);
        } else if (requestCode == REQUEST_CODE_TASK_LISTS && resultCode == RESULT_OK && data.hasExtra(FILTER_SORT_PARAMS)) {
            TaskFilterParams filterParams = (TaskFilterParams) data.getSerializableExtra(FILTER_SORT_PARAMS);
            listTaskPresenter.setTaskFilterParams(filterParams);
        }
    }

//    private void initializeProgressDialog() {
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setCancelable(false);
//        progressDialog.setTitle(R.string.fetching_structures_title);
//        progressDialog.setMessage(getString(R.string.fetching_structures_message));
//    }

    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(R.string.fetching_structures_title);
        progressDialog.setMessage(getString(R.string.fetching_structures_message));
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
    }
    @Override
    public void updateProgressDialog(int percentage) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setProgress(percentage);
        }
    }
    @Override
    public void showProgressDialog(@StringRes int title, @StringRes int message) {
        showProgressDialog(title, message, new Object[0]);
    }

    @Override
    public void showProgressDialog(@StringRes int title, @StringRes int message, Object... formatArgs) {
        if (progressDialog != null) {
            progressDialog.setTitle(title);
            if (formatArgs.length == 0) {
                progressDialog.setMessage(getString(message));
            } else {
                progressDialog.setMessage(getString(message, formatArgs));
            }
            progressDialog.show();
        }
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


public Location getUserCurrentLocation() {
    if (mMapboxMap == null) {
        return null;
    }
    LocationComponent locationComponent = mMapboxMap.getLocationComponent();
    if (locationComponent != null && locationComponent.isLocationComponentActivated()) {
        return locationComponent.getLastKnownLocation();
    }
    return null;
}


    @Override
    public void requestUserLocation() {
        hasRequestedLocation = true;
        Log.d(TAG, "requestUserLocation called, alreadyGranted=" + PermissionsManager.areLocationPermissionsGranted(this));

        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_CODE_LOCATION_PERMISSION
            );
            return;
        }

        // 2. Enable location component if permissions are granted
        if (mMapboxMap != null) {
            mMapboxMap.getStyle(style -> {
                LocationComponent locationComponent = mMapboxMap.getLocationComponent();
                if (locationComponent != null) {
                    if (!locationComponent.isLocationComponentActivated()) {
                        LocationComponentActivationOptions options = LocationComponentActivationOptions
                                .builder(this, style)
                                .useDefaultLocationEngine(true)
                                .build();
                        locationComponent.activateLocationComponent(options);
                    }

                    try {
                        locationComponent.setLocationComponentEnabled(true);
                        locationComponent.setCameraMode(CameraMode.TRACKING);
                        locationComponent.setRenderMode(RenderMode.COMPASS);
                    } catch (SecurityException e) {
                        Log.e(TAG, "SecurityException enabling location component", e);
                    }
                }
            });
        }
    }
    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onDestroy() {
        listTaskPresenter = null;
        super.onDestroy();
    }

    @Override
    public void onSyncStart() {
        if (SyncStatusBroadcastReceiver.getInstance().isSyncing() && !startedToastShown) {
            displayToast(R.string.sync_started);
            startedToastShown = true;
            completedToastShown = false;
        }
        toggleProgressBarView(true);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted by user.");
                requestUserLocation();
            } else {
                Log.w(TAG, "Location permission denied by user.");
                Toast.makeText(this, R.string.location_service_disabled, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage permission granted by user.");
                // whatever storage-dependent action was waiting, run it here
            } else {
                Log.w(TAG, "Storage permission denied by user.");
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        if (FetchStatus.fetched.equals(fetchStatus)) {
            return;
        }
        if (startedToastShown && completedToastShown) return;
        //To cover against consecutive sync starts firing, turn the flag off with delay
        new Handler().postDelayed(() -> startedToastShown = false, SYNC_BACK_OFF_DELAY);
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable();
        if (fetchStatus.equals(FetchStatus.fetchedFailed) && isNetworkAvailable) {
            displayToast(org.smartregister.reveal.R.string.sync_failed);
        } else if (fetchStatus.equals(FetchStatus.nothingFetched)) {
            displayToast(org.smartregister.reveal.R.string.sync_complete);
        } else if (fetchStatus.equals(FetchStatus.noConnection)) {
            displayToast(org.smartregister.reveal.R.string.sync_failed_no_internet);
        } else if (fetchStatus.equals(FetchStatus.fetchedFailed) && !isNetworkAvailable) {
            displayToast(org.smartregister.reveal.R.string.sync_failed_no_internet);
        }
        completedToastShown = true;
    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        onSyncInProgress(fetchStatus);
        //Check sync status and Update UI to show sync status
        drawerView.checkSynced();
        // revert to sync status view
        toggleProgressBarView(false);
    }

    @Override
    public void onResume() {

        super.onResume();
        if (mapView != null) {
            mapView.onResume(); // 👈 Resumes Mapbox OpenGL rendering surface
        }
        formOpening = false;

        SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(this);
        Log.d("SYNC_TRACE_RVL", "LISTENER_REGISTERED t=" + System.currentTimeMillis());
        ValidateAssignmentReceiver.getInstance().addListener(this);
        IntentFilter filter = new IntentFilter(Action.STRUCTURE_TASK_SYNCED);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(refreshGeowidgetReceiver, filter);
        IntentFilter syncProgressFilter = new IntentFilter(AllConstants.SyncProgressConstants.ACTION_SYNC_PROGRESS);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(syncProgressBroadcastReceiver, syncProgressFilter);
        drawerView.onResume();
        listTaskPresenter.onResume();

        if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
            toggleProgressBarView(true);
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause(); // 👈 Pauses rendering safely
        }
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
        ValidateAssignmentReceiver.getInstance().removeLister(this);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(refreshGeowidgetReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(syncProgressBroadcastReceiver);
        RevealApplication.getInstance().setMyLocationComponentEnabled(revealMapHelper.isMyLocationComponentActive(this, myLocationButton));
        super.onPause();
    }

    @Override
    public void onDrawerClosed() {
        listTaskPresenter.onDrawerClosed();
    }

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public RevealJsonFormUtils getJsonFormUtils() {
        return jsonFormUtils;
    }

//    @Override
//    public void focusOnUserLocation(boolean focusOnUserLocation) {
//        kujakuMapView.focusOnUserLocation(focusOnUserLocation, RenderMode.COMPASS);
//    }
@Override
public void focusOnUserLocation(boolean focusOnUserLocation) {
    if (mMapboxMap != null) {
        mMapboxMap.getStyle(style -> {
            LocationComponent locationComponent = mMapboxMap.getLocationComponent();
            if (locationComponent.isLocationComponentActivated()) {
                if (focusOnUserLocation) {
                    locationComponent.setCameraMode(CameraMode.TRACKING_COMPASS);
                    locationComponent.setRenderMode(RenderMode.COMPASS);
                } else {
                    locationComponent.setCameraMode(CameraMode.NONE);
                }
            }
        });
    }
}

    @Override
    public boolean isMyLocationComponentActive() {
        return revealMapHelper.isMyLocationComponentActive(this, myLocationButton);
    }

    @Override
    public void displayMarkStructureInactiveDialog() {
        AlertDialogUtils.displayNotificationWithCallback(this, R.string.mark_location_inactive,
                R.string.confirm_mark_location_inactive, R.string.confirm, R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == BUTTON_POSITIVE)
                            listTaskPresenter.onMarkStructureInactiveConfirmed();
                        dialog.dismiss();
                    }
                });
    }

    @Override
    public void displayEditCDDTaskCompleteDialog() {
        AlertDialogUtils.displayNotificationWithCallback(this, R.string.edit_cdd_task_complete_status,
                R.string.confirm_edit_cdd_task_complete_status, R.string.complete, R.string.incomplete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == BUTTON_POSITIVE) {
                            listTaskPresenter.onEditCDDTaskCompleteStatusConfirmed(true);
                        } else {
                            listTaskPresenter.onEditCDDTaskCompleteStatusConfirmed(false);
                        }
                        dialog.dismiss();
                    }
                });
    }

    @Override
    public void setNumberOfFilters(int numberOfFilters) {
        if (numberOfFilters > 0) {
            filterTasksFab.setVisibility(View.GONE);
            filterCountLayout.setVisibility(View.VISIBLE);
            filterCountTextView.setText(String.valueOf(numberOfFilters));
        } else {
            filterTasksFab.setVisibility(View.VISIBLE);
            filterCountLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public void setSearchPhrase(String searchPhrase) {
        searchView.setText(searchPhrase);
    }

    @Override
    public void toggleProgressBarView(boolean syncing) {
        drawerView.toggleProgressBarView(syncing);
    }

    @Override
    public void setOperationalArea(String operationalArea) {
        drawerView.setOperationalArea(operationalArea);
    }

    @Override
    public void onSyncProgress(SyncProgress syncProgress) {

        int progress = syncProgress.getPercentageSynced();
        String entity = getSyncEntityString(syncProgress.getSyncEntity());

        Timber.tag("SecureStore").i("entity %s progress %s",entity,progress);
//        if (syncProgress.getSyncEntity().equals(SyncEntity.LOCATIONS)) {
//            ProgressBar syncProgressBar = findViewById(R.id.location_sync_progress_bar);
//            TextView syncProgressBarLabel = findViewById(R.id.location_sync_progress_bar_label);
//            String labelText = String.format(getResources().getString(R.string.progressBarLabel), entity, progress);
//            syncProgressBar.setProgress(progress);
//            syncProgressBarLabel.setText(labelText);
//            if (progress == 100) {
//                PreferencesUtil.getInstance().setAllLocationsSynced(true);
//            } else {
//                PreferencesUtil.getInstance().setAllLocationsSynced(false);
//            }
//
//        }
        if (syncProgress.getSyncEntity().equals(SyncEntity.LOCATIONS)) {
            ProgressBar syncProgressBar = findViewById(R.id.location_sync_progress_bar);
            TextView syncProgressBarLabel = findViewById(R.id.location_sync_progress_bar_label);
            String labelText = String.format(getResources().getString(R.string.progressBarLabel), entity, progress);
            syncProgressBar.setProgress(progress);
            syncProgressBarLabel.setText(labelText);

            updateProgressDialog(progress); // 👈 add this line

            if (progress == 100) {
                PreferencesUtil.getInstance().setAllLocationsSynced(true);
            } else {
                PreferencesUtil.getInstance().setAllLocationsSynced(false);
            }
        }
        else if (syncProgress.getSyncEntity().equals(SyncEntity.TASKS)) {
            ProgressBar syncProgressBar = findViewById(R.id.task_sync_progress_bar);
            TextView syncProgressBarLabel = findViewById(R.id.task_sync_progress_bar_label);
            String labelText = String.format(getResources().getString(R.string.progressBarLabel), entity, progress);
            syncProgressBar.setProgress(progress);
            syncProgressBarLabel.setText(labelText);
            if (progress == 100) {
                PreferencesUtil.getInstance().setAllTasksSynced(true);
            } else {
                PreferencesUtil.getInstance().setAllTasksSynced(false);
            }
        } else if (syncProgress.getSyncEntity().equals(SyncEntity.PLANS)) {
            ProgressBar syncProgressBar = findViewById(R.id.plan_sync_progress_bar);
            TextView syncProgressBarLabel = findViewById(R.id.plan_sync_progress_bar_label);
            String labelText = String.format(getResources().getString(R.string.progressBarLabel), entity, progress);
            syncProgressBar.setProgress(progress);
            syncProgressBarLabel.setText(labelText);
            if (progress == 100) {
                PreferencesUtil.getInstance().setAllPlansSynced(true);
            } else {
                PreferencesUtil.getInstance().setAllPlansSynced(false);
            }
        } else if (syncProgress.getSyncEntity().equals(SyncEntity.EVENTS)) {
            ProgressBar syncProgressBar = findViewById(R.id.event_sync_progress_bar);
            TextView syncProgressBarLabel = findViewById(R.id.event_sync_progress_bar_label);
            String labelText = String.format(getResources().getString(R.string.progressBarLabel), entity, progress);
            syncProgressBar.setProgress(progress);
            syncProgressBarLabel.setText(labelText);
            if (progress == 100) {
                PreferencesUtil.getInstance().setAllEventsSynced(true);
            } else {
                PreferencesUtil.getInstance().setAllEventsSynced(false);
            }
        } else if(syncProgress.getSyncEntity().equals(SyncEntity.HDSS)
            || syncProgress.getSyncEntity().equals(SyncEntity.HDSS_OFFLINE)
            || syncProgress.getSyncEntity().equals(SyncEntity.HDSS_FILE)){
            ProgressBar syncProgressBar = findViewById(R.id.hdss_sync_progress_bar);
            TextView syncProgressBarLabel = findViewById(R.id.hdss_sync_progress_bar_label);

            String prefix = "";
            switch (syncProgress.getSyncEntity()){
                case HDSS:
                    prefix = "";
                    break;
                case HDSS_OFFLINE:
                    prefix = "Processing Data Offline: ";
                    break;
                case HDSS_FILE:
                    prefix = "Fetching Bulk Data: ";
                    break;
                default:
                    prefix = "";
            }
            String labelText =
                prefix.concat(String.format(getResources().getString(R.string.progressBarLabel), entity, progress));

            syncProgressBar.setProgress(progress);
            syncProgressBarLabel.setText(labelText);
            if (progress == 100) {
                PreferencesUtil.getInstance().setAllHdssSynced(true);
            } else {
                PreferencesUtil.getInstance().setAllHdssSynced(false);
            }
        }

        int totalSyncProgress = SyncUtils.getTotalSyncProgress();
        updateTotalSyncProgressSection(totalSyncProgress);
    }

    @Override
    public void onUserAssignmentRevoked(UserAssignmentDTO userAssignmentDTO) {
        drawerView.onResume();
    }


private class RefreshGeowidgetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        boolean localSyncDone;
        if (extras != null && extras.getBoolean(UPDATE_LOCATION_BUFFER_RADIUS)) {
            float bufferRadius = getLocationBuffer(isCurrentTargetLevelStructure());

            Location currentLocation = getLastKnownLocation();
            if (mMapboxMap != null && currentLocation != null) {
                mMapboxMap.getStyle(style -> {
                    LatLng userLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    updateLocationBufferCircle(style, userLatLng, bufferRadius);
                });
            }
        }
        localSyncDone = extras != null && extras.getBoolean(LOCAL_SYNC_DONE);
        listTaskPresenter.refreshStructures(localSyncDone);
    }
}
    @Nullable
    private Location getLastKnownLocation() {
        if (mMapboxMap != null) {
            LocationComponent locationComponent = mMapboxMap.getLocationComponent();
            if (locationComponent != null && locationComponent.isLocationComponentActivated()) {
                return locationComponent.getLastKnownLocation();
            }
        }
        return null;
    }
    private void updateLocationBufferCircle(@NonNull Style style, LatLng center, float radiusInMeters) {
        // 1. Generate a circular polygon using Mapbox Turf
        Point centerPoint = Point.fromLngLat(center.getLongitude(), center.getLatitude());
        Polygon circlePolygon = TurfTransformation.circle(centerPoint, radiusInMeters, "meters");

        FeatureCollection featureCollection = FeatureCollection.fromFeature(Feature.fromGeometry(circlePolygon));

        // 2. Add or update GeoJsonSource
        GeoJsonSource source = style.getSourceAs("location-buffer-source");
        if (source == null) {
            source = new GeoJsonSource("location-buffer-source", featureCollection);
            style.addSource(source);

            // Fill Layer for buffer
            FillLayer fillLayer = new FillLayer("location-buffer-fill", "location-buffer-source");
            fillLayer.setProperties(
                    PropertyFactory.fillColor(ContextCompat.getColor(getContext(), R.color.colorAccent)),
                    PropertyFactory.fillOpacity(0.2f)
            );
            style.addLayer(fillLayer);

            // Line Layer for buffer boundary
            LineLayer lineLayer = new LineLayer("location-buffer-line", "location-buffer-source");
            lineLayer.setProperties(
                    PropertyFactory.lineColor(ContextCompat.getColor(getContext(), R.color.colorAccent)),
                    PropertyFactory.lineWidth(1.5f)
            );
            style.addLayer(lineLayer);
        } else {
            source.setGeoJson(featureCollection);
        }
    }

    protected Country getBuildCountry() {
        return getCountry();
    }


    public void displayResetInterventionTaskDialog(String interventionType) {
        AlertDialogUtils.displayNotificationWithCallback(this, R.string.undo_task_title,
                R.string.undo_task_msg, R.string.confirm, R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == BUTTON_POSITIVE)
                            listTaskPresenter.onUndoInterventionStatus(interventionType);
                        dialog.dismiss();
                    }
                });
    }

    private void updateTotalSyncProgressSection(final int totalSyncProgress) {
        ProgressIndicatorView overallSyncProgressView = findViewById(R.id.overall_sync_progress_view);
        overallSyncProgressView.setTitle(String.format("Sync Progress  %d%%", totalSyncProgress));
        overallSyncProgressView.setProgress(totalSyncProgress);
        PreferencesUtil.getInstance().setCurrentTotalSyncProgress(String.valueOf(totalSyncProgress));
    }

    //methods added by moid
    private void addOrUpdateBoundaryLayers(
            @NonNull Style style,
            @NonNull FeatureCollection featureCollection,
            @NonNull String sourceId,
            @NonNull String layerIdPrefix,
            int boundaryColor,
            float boundaryWidth,
            String labelProperty,
            String layerBelow
    ) {
        // 1. Source creation or update
        GeoJsonSource source = style.getSourceAs(sourceId);
        if (source == null) {
            source = new GeoJsonSource(sourceId, featureCollection);
            style.addSource(source);
        } else {
            source.setGeoJson(featureCollection);
        }

        String fillLayerId = layerIdPrefix + "-fill";
        String lineLayerId = layerIdPrefix + "-line";
        String symbolLayerId = layerIdPrefix + "-symbol";

        // 2. Fill Layer (Semi-transparent background for polygon interaction)
        if (style.getLayer(fillLayerId) == null) {
            FillLayer fillLayer = new FillLayer(fillLayerId, sourceId);
            fillLayer.setProperties(
                    fillColor(boundaryColor),
                    fillOpacity(0.0f)
            );
            if (layerBelow != null && style.getLayer(layerBelow) != null) {
                style.addLayerBelow(fillLayer, layerBelow);
            } else {
                style.addLayer(fillLayer);
            }
        }

        // 3. Line Layer (Boundary outline)
        if (style.getLayer(lineLayerId) == null) {
            LineLayer lineLayer = new LineLayer(lineLayerId, sourceId);
            lineLayer.setProperties(
                    lineColor(boundaryColor),
                    lineWidth(boundaryWidth)
            );
            if (layerBelow != null && style.getLayer(layerBelow) != null) {
                style.addLayerBelow(lineLayer, layerBelow);
            } else {
                style.addLayer(lineLayer);
            }
        }

        // 4. Symbol Layer (Optional text label)
        if (labelProperty != null && style.getLayer(symbolLayerId) == null) {
            SymbolLayer symbolLayer = new SymbolLayer(symbolLayerId, sourceId);
            symbolLayer.setProperties(
                    textField("{" + labelProperty + "}"),
                    textColor(Color.WHITE),
                    textSize(12f),
                    textIgnorePlacement(true),
                    textAllowOverlap(true)
            );
            style.addLayer(symbolLayer);
        }
    }
    private void drawBoundaryFromFeatureCollection(Style style, FeatureCollection operationalArea) {
        addOrUpdateBoundaryLayers(
                style,
                operationalArea,
                "operational-area-source",
                "operational-area-layer",
                Color.YELLOW,
                getResources().getDimension(R.dimen.operational_area_boundary_width_thick),
                org.smartregister.reveal.util.Constants.Map.NAME_PROPERTY,
                null
        );
    }

    private void drawBoundary(Style style, Feature operationalArea) {
        drawBoundaryFromFeatureCollection(style, FeatureCollection.fromFeature(operationalArea));
    }



    private void drawParentBoundaryFromFeatureCollectionWithColor(Style style, FeatureCollection operationalArea, String sourceId, String layerIdPrefix, String layerBelow, int boundaryColorRes, boolean showLabel) {
        addOrUpdateBoundaryLayers(
                style,
                operationalArea,
                sourceId,
                layerIdPrefix,
                getResources().getColor(boundaryColorRes, null),
                getResources().getDimension(R.dimen.operational_area_boundary_width),
                showLabel ? org.smartregister.reveal.util.Constants.Map.NAME_PROPERTY : null,
                layerBelow
        );
    }

    private void drawNeighbouringBoundary(Style style, List<Feature> operationalArea) {
        addOrUpdateBoundaryLayers(
                style,
                FeatureCollection.fromFeatures(operationalArea),
                "neighbouring-area-source",
                "neighbouring-area-layer",
                Color.WHITE,
                getResources().getDimension(R.dimen.adjacent_operational_area_boundary_width),
                org.smartregister.reveal.util.Constants.Map.NAME_PROPERTY,
                null
        );
    }

    private void drawIRSLiteOABoundaryLayer(Style style, Feature operationalArea) {
        addOrUpdateBoundaryLayers(
                style,
                FeatureCollection.fromFeature(operationalArea),
                "irs-lite-source-" + operationalArea.id(),
                "irs-lite-layer-" + operationalArea.id(),
                Color.WHITE,
                getResources().getDimension(R.dimen.irs_lite_operational_area_boundary_width),
                null,
                null
        );
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d(TAG, "onStart: location permission not granted, requesting.");
            listTaskPresenter.getLocationPresenter().requestUserLocation();
        }
    }


    private void requestStoragePermission() {
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "requestStoragePermission called, alreadyGranted=" + granted);

        if (!granted) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_STORAGE_PERMISSION
            );
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }



}
