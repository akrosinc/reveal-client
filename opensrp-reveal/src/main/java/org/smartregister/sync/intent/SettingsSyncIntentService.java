package org.smartregister.sync.intent;

import android.content.Intent;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.smartregister.AllConstants;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.reveal.job.RevealWorkScheduler;
import org.smartregister.reveal.job.SyncWorker;
import org.smartregister.sync.helper.SyncSettingsServiceHelper;

import timber.log.Timber;

import static org.smartregister.util.Log.logError;


public class SettingsSyncIntentService extends BaseSyncIntentService {

    private static final String TAG = SettingsSyncIntentService.class.getCanonicalName();

    protected SyncSettingsServiceHelper syncSettingsServiceHelper;

    public static final String EVENT_SYNC_COMPLETE = "event_sync_complete";

    public SettingsSyncIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean isSuccessfulSync = processSettings(intent);
        if (isSuccessfulSync) {
            RevealWorkScheduler.scheduleJobImmediately(SyncWorker.TAG);
        }
    }

    protected boolean processSettings(Intent intent) {
        Timber.d("In Settings Sync Intent Service...");
        boolean isSuccessfulSync = true;
        if (intent != null) {
            try {
                super.onHandleIntent(intent);
                int count = syncSettingsServiceHelper.processIntent();
                if (count > 0) {
                    intent.putExtra(AllConstants.INTENT_KEY.SYNC_TOTAL_RECORDS, count);
                }
            } catch (JSONException e) {
                isSuccessfulSync = false;
                logError(TAG + " Error fetching client settings");
            }
        }
        return isSuccessfulSync;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initSyncSettingsServiceHelper();
    }

    /**
     * Initializes {@link #syncSettingsServiceHelper} the same way {@link #onCreate()}
     * does. Exposed so WorkManager workers can prepare the service without the
     * {@link android.app.IntentService} lifecycle.
     */
    protected void initSyncSettingsServiceHelper() {
        Context context = CoreLibrary.getInstance().context();
        syncSettingsServiceHelper = new SyncSettingsServiceHelper(context.configuration().dristhiBaseURL(), context.getHttpAgent());
    }

    /**
     * Entry point for WorkManager workers. Runs the settings sync synchronously on the
     * caller's (worker) thread without the {@link android.app.IntentService} lifecycle
     * or a background {@code startService()}. Initializes the sync helper the same way
     * {@link #onCreate()} did, then drives the work through {@link #onHandleIntent}
     * (allowing subclasses to layer additional processing).
     *
     * @param appContext the application context provided by the Worker
     */
    public void runSettingsSync(@NonNull android.content.Context appContext) {
        if (getBaseContext() == null) {
            attachBaseContext(appContext);
        }
        initSyncSettingsServiceHelper();
        onHandleIntent(new Intent());
    }

}

