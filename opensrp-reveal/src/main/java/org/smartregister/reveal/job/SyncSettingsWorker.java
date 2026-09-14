package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.sync.intent.SettingsSyncIntentService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code SyncSettingsServiceJob}. Runs the
 * settings sync (previously performed by {@link SettingsSyncIntentService} via a
 * background {@code startService()}) directly on the worker thread, reusing the
 * {@code runSettingsSync(Context)} runner added in the earlier settings migration.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy
 * {@code SyncSettingsServiceJob.TAG} so existing scheduling call sites resolve this
 * worker through {@link RevealWorkerRegistry}.</p>
 */
public class SyncSettingsWorker extends BaseWorker {

    public static final String TAG = "SyncSettingsServiceJob";

    public SyncSettingsWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("SyncSettingsWorker.doWork() started");
            SettingsSyncIntentService service = new SettingsSyncIntentService();
            service.runSettingsSync(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "SyncSettingsWorker failed");
            return Result.retry();
        }
    }
}
