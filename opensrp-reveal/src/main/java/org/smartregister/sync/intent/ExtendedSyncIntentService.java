package org.smartregister.sync.intent;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import org.smartregister.CoreLibrary;
import org.smartregister.reveal.job.RevealWorkScheduler;
import org.smartregister.reveal.job.ValidateSyncDataWorker;
import org.smartregister.service.ActionService;
import org.smartregister.util.NetworkUtils;

import timber.log.Timber;


public class ExtendedSyncIntentService extends BaseSyncIntentService {

    private ActionService actionService = CoreLibrary.getInstance().context().actionService();

    public ExtendedSyncIntentService() {
        super("ExtendedSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        try {
            super.onHandleIntent(workIntent);
            if (NetworkUtils.isNetworkAvailable()) {
                if (!CoreLibrary.getInstance().getSyncConfiguration().disableActionService()) {
                    actionService.fetchNewActions();
                }
                startSyncValidation();
            }

        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e);
        }
    }

    /**
     * Entry point for WorkManager workers. Runs the extended sync (fetch new actions
     * then trigger validation) synchronously on the caller's (worker) thread without
     * the {@link android.app.IntentService} lifecycle or a background
     * {@code startService()}.
     *
     * @param appContext the application context provided by the Worker
     */
    public void runExtendedSync(@NonNull Context appContext) {
        if (getBaseContext() == null) {
            attachBaseContext(appContext);
        }
        onHandleIntent(new Intent());
    }

    private void startSyncValidation() {
        RevealWorkScheduler.scheduleJobImmediately(ValidateSyncDataWorker.TAG);
    }
}
