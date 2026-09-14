package org.smartregister.sync.intent;

import android.content.Intent;

import org.smartregister.CoreLibrary;
import org.smartregister.sync.helper.HdssServiceHelper;


public class HdssSyncIntentService extends BaseSyncIntentService {

    private static final String TAG = HdssSyncIntentService.class.getCanonicalName();

    private HdssServiceHelper hdssServiceHelper;
    private boolean currentlySyncing = false;

    public HdssSyncIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!currentlySyncing) {
            currentlySyncing = true;

            this.hdssServiceHelper.syncHdssDetails();
            currentlySyncing = false;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.hdssServiceHelper = CoreLibrary.getInstance().context().hdssServiceHelper();
    }

    /**
     * Entry point for WorkManager workers. Runs the HDSS sync synchronously on the
     * caller's (worker) thread without the {@link android.app.IntentService} lifecycle
     * or a background {@code startService()}. Replicates the helper initialization
     * previously done in {@link #onCreate()} before driving {@link #onHandleIntent}.
     *
     * @param appContext the application context provided by the Worker
     */
    public void runHdssSync(@androidx.annotation.NonNull android.content.Context appContext) {
        if (getBaseContext() == null) {
            attachBaseContext(appContext);
        }
        this.hdssServiceHelper = CoreLibrary.getInstance().context().hdssServiceHelper();
        onHandleIntent(new Intent());
    }

}

