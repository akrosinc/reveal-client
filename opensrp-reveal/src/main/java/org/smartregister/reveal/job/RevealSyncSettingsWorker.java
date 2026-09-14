package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.reveal.sync.RevealSettingsSyncIntentService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code RevealSyncSettingsServiceJob}.
 * Runs the reveal settings sync (previously performed by
 * {@link RevealSettingsSyncIntentService} via a background {@code startService()})
 * directly on the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy
 * {@code RevealSyncSettingsServiceJob.TAG} so existing scheduling call sites resolve
 * this worker through {@link RevealWorkerRegistry}.</p>
 */
public class RevealSyncSettingsWorker extends BaseWorker {

    public static final String TAG = "RevealSyncSettingsServiceJob";

    public RevealSyncSettingsWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("RevealSyncSettingsWorker.doWork() started");
            RevealSettingsSyncIntentService service = new RevealSettingsSyncIntentService();
            service.runSettingsSync(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "RevealSyncSettingsWorker failed");
            return Result.retry();
        }
    }
}
