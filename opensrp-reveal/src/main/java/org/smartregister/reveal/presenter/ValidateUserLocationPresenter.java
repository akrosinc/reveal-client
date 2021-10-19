package org.smartregister.reveal.presenter;

import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.reveal.BuildConfig;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.UserLocationContract;
import org.smartregister.reveal.contract.UserLocationContract.UserLocationCallback;
import org.smartregister.reveal.contract.UserLocationContract.UserLocationView;
import org.smartregister.reveal.util.AppExecutors;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.Utils;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

import static org.smartregister.reveal.util.Constants.ADMIN_PASSWORD_REQUIRED;
import static org.smartregister.reveal.util.Constants.BUILD_COUNTRY;
import static org.smartregister.reveal.util.Constants.Preferences.GPS_ACCURACY;
import static org.smartregister.reveal.util.Constants.Preferences.ADMIN_PASSWORD_ENTERED;
import static org.smartregister.reveal.util.Constants.Preferences.EVENT_LATITUDE;
import static org.smartregister.reveal.util.Constants.Preferences.EVENT_LONGITUDE;
import static org.smartregister.reveal.util.Constants.USER_NAME;

;

/**
 * Created by samuelgithengi on 2/13/19.
 */
public class ValidateUserLocationPresenter implements UserLocationContract.UserLocationPresenter {

    private UserLocationView locationView;

    private UserLocationCallback callback;

    private long resolutionStarted;

    private AppExecutors appExecutors;
    private AllSharedPreferences sharedPreferences;
    protected ValidateUserLocationPresenter(UserLocationView locationView, UserLocationCallback callback) {
        this.locationView = locationView;
        this.callback = callback;
        appExecutors = RevealApplication.getInstance().getAppExecutors();
        sharedPreferences = RevealApplication.getInstance().getContext().allSharedPreferences();
    }

    @Override
    public void requestUserLocation() {
        locationView.requestUserLocation();
    }

    @Override
    public void onGetUserLocation(Location location) {
        locationView.hideProgressDialog();
            double offset = callback.getTargetCoordinates().distanceTo(
                    new LatLng(location.getLatitude(), location.getLongitude()));
            if (offset > Utils.getLocationBuffer()) {
                appExecutors.diskIO().execute(() -> logAdminPassRequiredEvent(location,true));
                callback.requestUserPassword();
            } else {
                appExecutors.diskIO().execute(() -> logAdminPassRequiredEvent(location,false));
                callback.onLocationValidated();
            }
    }

    @Override
    public void onGetUserLocationFailed() {
        locationView.hideProgressDialog();
        callback.requestUserPassword();
    }

    @Override
    public void waitForUserLocation() {
        resolutionStarted = SystemClock.elapsedRealtime();
        waitForUserLocation(0);
    }


    private void waitForUserLocation(long elapsedTimeInMillis) {
        Location location = locationView.getUserCurrentLocation();
        Timber.d("user location: " + location);
        if (location == null) {
            if (elapsedTimeInMillis / 1000 >= Utils.getResolveLocationTimeoutInSeconds()) {
                appExecutors.mainThread().execute(() -> onGetUserLocationFailed());
            } else {
                if (elapsedTimeInMillis == 0) {//first try running in main thread ; show progress dialog.
                    locationView.showProgressDialog(R.string.narrowing_location_title, R.string.narrowing_location_message);
                }
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        waitForUserLocation(SystemClock.elapsedRealtime() - resolutionStarted);
                    }
                }, 2000);
            }
        } else {
            appExecutors.mainThread().execute(() -> onGetUserLocation(location));

        }
    }

    private void logAdminPassRequiredEvent(Location location, boolean passwordEntered){
        sharedPreferences.savePreference(EVENT_LATITUDE,"");
        sharedPreferences.savePreference(EVENT_LONGITUDE,"");
        sharedPreferences.savePreference(ADMIN_PASSWORD_ENTERED,"");
        sharedPreferences.savePreference(GPS_ACCURACY,"");
        Bundle bundle = new Bundle();
        bundle.putString(USER_NAME, RevealApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM());
        Double latitude = location.getLatitude();
        bundle.putDouble(EVENT_LATITUDE,latitude);
        sharedPreferences.savePreference(EVENT_LATITUDE,latitude.toString());
        Double longitude = location.getLongitude();
        bundle.putDouble(EVENT_LONGITUDE,longitude);
        sharedPreferences.savePreference(EVENT_LONGITUDE,longitude.toString());
        Float accuracy = location.getAccuracy();
        bundle.putFloat(GPS_ACCURACY,accuracy);
        sharedPreferences.savePreference(GPS_ACCURACY,accuracy.toString());
        bundle.putString(BUILD_COUNTRY,BuildConfig.BUILD_COUNTRY.name());
        bundle.putBoolean(ADMIN_PASSWORD_ENTERED,passwordEntered);
        sharedPreferences.savePreference(ADMIN_PASSWORD_ENTERED,String.valueOf(passwordEntered));
        FirebaseAnalytics.getInstance(RevealApplication.getInstance().getApplicationContext()).logEvent(ADMIN_PASSWORD_REQUIRED,bundle);
    }



}
