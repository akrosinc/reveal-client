package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.reveal.sync.RevealSyncIntentService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code SyncServiceJob}. Runs the
 * reveal sync (previously performed by {@link RevealSyncIntentService} via a
 * background {@code startService()}) directly on the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy
 * {@code SyncServiceJob.TAG} so existing scheduling call sites resolve this worker
 * through {@link RevealWorkerRegistry}.</p>
 */
public class SyncWorker extends BaseWorker {

    public static final String TAG = "SyncServiceJob";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("SyncWorker.doWork() started");
            RevealSyncIntentService service = new RevealSyncIntentService();
            service.runSync(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "SyncWorker failed");
            return Result.retry();
        }
    }
}
