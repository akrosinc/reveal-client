package org.smartregister.reveal.view;

import static org.smartregister.reveal.util.Constants.MY_LOCATION_ZOOM_LEVEL;
import static org.smartregister.reveal.util.Constants.Map.MAX_SELECT_ZOOM_LEVEL;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.rengwuxian.materialedittext.validation.METValidator;

import org.smartregister.reveal.layer.LabelLayer;
import org.smartregister.reveal.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Native Mapbox implementation replacing KujakuMapView.
 * Created by samuelgithengi on 12/13/18.
 */
public class RevealMapView extends MapView {

    private List<METValidator> validators;
    private MapboxMap mapboxMap;

    public RevealMapView(@NonNull Context context) {
        super(context);
        initMap();
    }

    public RevealMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initMap();
    }

    public RevealMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMap();
    }

    public RevealMapView(@NonNull Context context, @Nullable MapboxMapOptions options) {
        super(context, options);
        initMap();
    }

    /**
     * Automatically fetch and cache the MapboxMap instance asynchronously.
     */
    private void initMap() {
        getMapAsync(map -> {
            this.mapboxMap = map;
        });
    }

    /**
     * Native replacement for Kujaku's centerMap method.
     */
    public void centerMap(@NonNull LatLng point, int animateToNewTargetDuration, double newZoom) {
        if (mapboxMap == null) return;

        double targetZoom;
        if (!Utils.isCurrentTargetLevelStructure()) {
            targetZoom = MAX_SELECT_ZOOM_LEVEL;
        } else {
            targetZoom = Math.max(newZoom, MY_LOCATION_ZOOM_LEVEL);
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(point)
                .zoom(targetZoom)
                .build();

        if (animateToNewTargetDuration > 0) {
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), animateToNewTargetDuration);
        } else {
            mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    /**
     * Convenience method to support custom LabelLayers natively on the map.
     */
    public void addLayer(@NonNull LabelLayer labelLayer) {
        if (mapboxMap != null) {
            labelLayer.addLayerToMap(mapboxMap);
        }
    }

    public void addValidator(METValidator validator) {
        if (validators == null) {
            this.validators = new ArrayList<>();
        }
        this.validators.add(validator);
    }

    public List<METValidator> getValidators() {
        return validators;
    }

    public MapboxMap getMapboxMap() {
        return mapboxMap;
    }

    public void setMapboxMap(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
    }

    public Double getMapboxMapZoom() {
        if (mapboxMap != null) {
            return mapboxMap.getCameraPosition().zoom;
        }
        return null;
    }

    public CameraPosition getCameraPosition() {
        if (mapboxMap != null) {
            return mapboxMap.getCameraPosition();
        }
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
//package org.smartregister.reveal.view;
//
//import static org.smartregister.reveal.util.Constants.MY_LOCATION_ZOOM_LEVEL;
//import static org.smartregister.reveal.util.Constants.Map.MAX_SELECT_ZOOM_LEVEL;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.util.AttributeSet;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.mapbox.mapboxsdk.camera.CameraPosition;
//import com.mapbox.mapboxsdk.geometry.LatLng;
//import com.mapbox.mapboxsdk.maps.MapboxMap;
//import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
//import com.rengwuxian.materialedittext.validation.METValidator;
//
//import org.smartregister.reveal.util.Utils;
//
//import java.util.ArrayList;
//import java.util.List;
//import io.ona.kujaku.views.KujakuMapView;
//
///**
// * Created by samuelgithengi on 12/13/18.
// */
//public class RevealMapView extends KujakuMapView {
//
//    private List<METValidator> validators;
//
//    private MapboxMap mapboxMap;
//
//    public RevealMapView(@NonNull Context context) {
//        super(context);
//    }
//
//    public RevealMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public RevealMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    public RevealMapView(@NonNull Context context, @Nullable MapboxMapOptions options) {
//        super(context, options);
//    }
//
//    @Override
//    public void centerMap(@NonNull LatLng point, int animateToNewTargetDuration, double newZoom) {
//        if (!Utils.isCurrentTargetLevelStructure()) {
//            super.centerMap(point, animateToNewTargetDuration, MAX_SELECT_ZOOM_LEVEL);
//        } else {
//            super.centerMap(point, animateToNewTargetDuration, newZoom > MY_LOCATION_ZOOM_LEVEL ? newZoom : MY_LOCATION_ZOOM_LEVEL);
//        }
//
//    }
//
//
//    public void addValidator(METValidator validator) {
//        if (validators == null) {
//            this.validators = new ArrayList<>();
//        }
//        this.validators.add(validator);
//    }
//
//    public List<METValidator> getValidators() {
//        return validators;
//    }
//
//    public MapboxMap getMapboxMap() {
//        return mapboxMap;
//    }
//
//    public void setMapboxMap(MapboxMap mapboxMap) {
//        this.mapboxMap = mapboxMap;
//    }
//
//
//    public Double getMapboxMapZoom() {
//        if (mapboxMap != null)
//            return mapboxMap.getCameraPosition().zoom;
//        else
//            return null;
//    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
////        if (savedInstanceState != null) {
//////            TelemetryDefinition telemetry = Mapbox.getTelemetry();
//////            if (telemetry != null) {
//////                telemetry.onAppUserTurnstileEvent();
//////            }
////        } else if (savedInstanceState.getBoolean(MapboxConstants.STATE_HAS_SAVED_STATE)) {
////            this.savedInstanceState = savedInstanceState;
////        }
//    }
//
//
//    public CameraPosition getCameraPosition() {
//        if (mapboxMap != null)
//            return mapboxMap.getCameraPosition();
//        else
//            return null;
//    }
//}
