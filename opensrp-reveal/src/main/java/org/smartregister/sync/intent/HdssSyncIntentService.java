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

}

