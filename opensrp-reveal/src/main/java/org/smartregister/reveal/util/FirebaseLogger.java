package org.smartregister.reveal.util;

import static org.smartregister.reveal.util.Constants.USER_NAME;

import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.smartregister.domain.Response;
import org.smartregister.reveal.application.RevealApplication;

public class FirebaseLogger {

    public static final String REQUEST = "request";

    public static final String RESPONSE = "response";

    public static final String API_RESPONSE_FAILURE = "api_response_failure";

    public static void logApiFailures(final String request, final Response resp) {
        Bundle bundle = new Bundle();
        bundle.putString(USER_NAME, RevealApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM());
        bundle.putString(REQUEST,request);
        bundle.putString(RESPONSE,resp.status().displayValue());
        FirebaseAnalytics.getInstance(RevealApplication.getInstance().getApplicationContext())
                                                                     .logEvent(API_RESPONSE_FAILURE,bundle);
    }
}
