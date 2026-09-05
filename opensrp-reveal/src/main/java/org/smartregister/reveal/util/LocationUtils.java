package org.smartregister.reveal.util;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;

import timber.log.Timber;

/**
 * Created by samuelgithengi on 3/20/19.
 */
public class LocationUtils {

    public static final int LOCATION_SETTINGS_REQUEST_CODE = 9001;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    private LocationEngine locationEngine;
    private LocationEngineRequest locationEngineRequest;
    private Location lastLocation;

    public LocationUtils(Context context) {
        this.locationEngine = LocationEngineProvider.getBestLocationEngine(context.getApplicationContext());
        this.locationEngineRequest = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build();
    }
    public void setLastKnownLocation(Location location) {
        this.lastLocation = location;
    }


    @SuppressWarnings("MissingPermission")
    public void requestLocationUpdates(LocationEngineCallback<LocationEngineResult> locationCallback) {
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates(locationEngineRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @SuppressWarnings("MissingPermission")
    public Location getLastLocation() {
        if (locationEngine != null) {
            locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    if (result != null && result.getLastLocation() != null) {
                        lastLocation = result.getLastLocation();
                    }
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    Timber.tag("Reveal Exception").e(exception, "Failed to get last location");
                }
            });
        }
        return lastLocation;
    }

    /**
     * Stop the location engine and unregister from receiving updates
     */
    public void stopLocationClient(LocationEngineCallback<LocationEngineResult> locationCallback) {
        if (locationEngine != null && locationCallback != null) {
            locationEngine.removeLocationUpdates(locationCallback);
        }
    }

    public void checkLocationSettingsAndStartLocationServices(Context context, LocationEngineCallback<LocationEngineResult> locationCallback) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(DEFAULT_INTERVAL_IN_MILLISECONDS);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .setAlwaysShow(true);

            LocationServices.getSettingsClient(activity)
                    .checkLocationSettings(builder.build())
                    .addOnCompleteListener(task -> {
                        try {
                            task.getResult(ApiException.class);
                            // Settings are satisfied
                            Timber.i("All location settings are satisfied.");
                            requestLocationUpdates(locationCallback);
                        } catch (ApiException exception) {
                            switch (exception.getStatusCode()) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        ResolvableApiException resolvable = (ResolvableApiException) exception;
                                        resolvable.startResolutionForResult(activity, LOCATION_SETTINGS_REQUEST_CODE);
                                    } catch (IntentSender.SendIntentException e) {
                                        Timber.i("PendingIntent unable to execute request.");
                                    } catch (ClassCastException e) {
                                        Timber.tag("Reveal Exception").w(e, "Unable to cast to ResolvableApiException");
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    Timber.tag("Reveal Exception").w("Location settings are inadequate, and cannot be fixed here. Dialog cannot be created.");
                                    break;
                                default:
                                    Timber.tag("Reveal Exception").w("Unknown status code returned after checking location settings");
                                    break;
                            }
                        }
                    });
        } else {
            Timber.tag("Reveal Exception").w("Context is not an Activity and can therefore not start location resolution dialog");
        }
    }

    /**
     * Clear location client
     */
    public void destroy(LocationEngineCallback<LocationEngineResult> locationCallback) {
        stopLocationClient(locationCallback);
        locationEngine = null;
    }
}
//package org.smartregister.reveal.util;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.IntentSender;
//import android.location.Location;
//
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.LocationSettingsResult;
//import com.google.android.gms.location.LocationSettingsStatusCodes;
//
//
//import io.ona.kujaku.interfaces.ILocationClient;
//import io.ona.kujaku.listeners.BaseLocationListener;
//import io.ona.kujaku.location.clients.GoogleLocationClient;
//import io.ona.kujaku.utils.Constants;
//import io.ona.kujaku.utils.LocationSettingsHelper;
//import timber.log.Timber;
//
///**e
// * Created by samuelgithengi on 3/20/19.
// */
//public class LocationUtils {
//
//    private ILocationClient locationClient;
//
//    public LocationUtils(Context context) {
//        locationClient = new GoogleLocationClient(context);
//    }
//
//    public LocationUtils(ILocationClient locationClient) {
//        this.locationClient = locationClient;
//    }
//
//    public void requestLocationUpdates(BaseLocationListener locationListener) {
//        locationClient.requestLocationUpdates(locationListener);
//    }
//
//    public Location getLastLocation() {
//        return locationClient.getLastLocation();
//    }
//
//    /**
//     * Stop the location client and unregister from receiving updates
//     */
//    public void stopLocationClient() {
//        if (locationClient != null) {
//            locationClient.close();
//        }
//    }
//
//
//    public void checkLocationSettingsAndStartLocationServices(Context context, BaseLocationListener locationListener) {
//        if (context instanceof Activity) {
//            Activity activity = (Activity) context;
//
//            LocationSettingsHelper.checkLocationEnabled(activity, new ResultCallback<LocationSettingsResult>() {
//                @Override
//                public void onResult(LocationSettingsResult result) {
//                    final Status status = result.getStatus();
//
//                    switch (status.getStatusCode()) {
//                        case LocationSettingsStatusCodes.SUCCESS:
//                            Timber.i("All location settings are satisfied.");
//                            requestLocationUpdates(locationListener);
//                            break;
//                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                            Timber.i("Location settings are not satisfied. Show the user a dialog to upgrade location settings");
//
//                            try {
//                                status.startResolutionForResult(activity, Constants.RequestCode.LOCATION_SETTINGS);
//                            } catch (IntentSender.SendIntentException e) {
//                                Timber.i("PendingIntent unable to execute request.");
//                            }
//                            break;
//                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                            Timber.tag("Reveal Exception").w("Location settings are inadequate, and cannot be fixed here. Dialog cannot be created.");
//                            break;
//
//                        default:
//                            Timber.tag("Reveal Exception").w("Unknown status code returned after checking location settings");
//                            break;
//                    }
//                }
//            });
//        } else {
//            Timber.tag("Reveal Exception").w("KujakuMapView is not started in an Activity and can therefore not start location services");
//        }
//    }
//
//    /**
//     * Clear location client
//     */
//    public void destroy() {
//        stopLocationClient();
//        locationClient = null;
//    }
//
//
//}
