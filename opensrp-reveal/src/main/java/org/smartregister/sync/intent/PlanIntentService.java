package org.smartregister.sync.intent;

import android.content.Intent;

import org.smartregister.sync.helper.PlanIntentServiceHelper;


public class PlanIntentService extends BaseSyncIntentService {

    private static final String TAG = "PlanIntentService";

    public PlanIntentService() { super(TAG); }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        PlanIntentServiceHelper.getInstance().syncPlans();
    }
}
