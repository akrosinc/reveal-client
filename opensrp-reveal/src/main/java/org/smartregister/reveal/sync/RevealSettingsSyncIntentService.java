package org.smartregister.reveal.sync;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.smartregister.AllConstants;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.sync.intent.SettingsSyncIntentService;

import static org.smartregister.reveal.util.Constants.Action.STRUCTURE_TASK_SYNCED;
import static org.smartregister.reveal.util.Constants.CONFIGURATION.UPDATE_LOCATION_BUFFER_RADIUS;

/**
 * @author Vincent Karuri
 */
public class RevealSettingsSyncIntentService extends SettingsSyncIntentService {
    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        Bundle data = intent.getExtras();
        if (data != null && data.getInt(AllConstants.INTENT_KEY.SYNC_TOTAL_RECORDS, 0) > 0) {
            RevealApplication.getInstance().processServerConfigs();
            // broadcast sync event
            Intent refreshGeoWidgetIntent = new Intent(STRUCTURE_TASK_SYNCED);
            refreshGeoWidgetIntent.putExtra(UPDATE_LOCATION_BUFFER_RADIUS, true);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(refreshGeoWidgetIntent);
        }
    }

    /**
     * Entry point for WorkManager workers. Runs the reveal settings sync synchronously
     * on the caller's (worker) thread without the {@link android.app.IntentService}
     * lifecycle or a background {@code startService()}.
     *
     * <p>The base {@link SettingsSyncIntentService#runSettingsSync(Context)} drives the
     * work through {@link #onHandleIntent(Intent)}, so the reveal-specific
     * {@code SYNC_TOTAL_RECORDS} post-processing (config processing + geo-widget
     * broadcast) still runs: the parent's {@code processSettings(intent)} mutates the
     * shared {@link Intent} to add the record count, which this subclass then reads.</p>
     *
     * @param appContext the application context provided by the Worker
     */
    @Override
    public void runSettingsSync(@NonNull Context appContext) {
        super.runSettingsSync(appContext);
    }
}
