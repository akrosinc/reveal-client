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

    /**
     * Entry point for WorkManager workers. Runs the plan sync synchronously on the
     * caller's (worker) thread without the {@link android.app.IntentService} lifecycle
     * or a background {@code startService()}.
     *
     * @param appContext the application context provided by the Worker
     */
    public void runPlanSync(@androidx.annotation.NonNull android.content.Context appContext) {
        if (getBaseContext() == null) {
            attachBaseContext(appContext);
        }
        onHandleIntent(new Intent());
    }
}
