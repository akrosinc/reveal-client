package org.smartregister.reveal.view;

import android.content.Context;
import android.os.Bundle;

import com.mapbox.mapboxsdk.maps.MapView;

import org.smartregister.util.LangUtils;
import org.smartregister.view.activity.MultiLanguageActivity;

/**
 * Created by samuelgithengi on 11/20/18.
 */
public abstract class BaseMapActivity extends MultiLanguageActivity {

    protected MapView mapView;

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        LangUtils.setLanguage(base);
        super.attachBaseContext(base);
    }

}
//package org.smartregister.reveal.view;
//
//import android.content.Context;
//import android.os.Bundle;
//import androidx.annotation.NonNull;
//import org.smartregister.reveal.util.Country;
//import org.smartregister.reveal.util.PreferencesUtil;
//import org.smartregister.util.LangUtils;
//import org.smartregister.view.activity.MultiLanguageActivity;
//
///**
// * Created by samuelgithengi on 11/20/18.
// */
//public abstract class BaseMapActivity extends MultiLanguageActivity {
//
//    protected RevealMapView kujakuMapView;
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (kujakuMapView != null) {
//            kujakuMapView.onStart();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (kujakuMapView != null)
//            kujakuMapView.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (kujakuMapView != null)
//            kujakuMapView.onPause();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (kujakuMapView != null)
//            kujakuMapView.onStop();
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (kujakuMapView != null)
//            kujakuMapView.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        if (kujakuMapView != null)
//            kujakuMapView.onLowMemory();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (kujakuMapView != null)
//            kujakuMapView.onDestroy();
//    }
//
//    @Override
//    protected void attachBaseContext(Context base) {
//        LangUtils.setLanguage(base);
//        super.attachBaseContext(base);
//    }
//
//}
