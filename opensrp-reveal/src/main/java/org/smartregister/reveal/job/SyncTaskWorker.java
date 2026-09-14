package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.sync.intent.SyncTaskIntentService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code SyncTaskServiceJob}. Runs the task
 * sync (previously performed by {@link SyncTaskIntentService} via a background
 * {@code startService()}) directly on the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy {@code SyncTaskServiceJob.TAG}
 * so existing scheduling call sites resolve this worker through
 * {@link RevealWorkerRegistry}.</p>
 */
public class SyncTaskWorker extends BaseWorker {

    public static final String TAG = "SyncTaskServiceJob";

    public SyncTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("SyncTaskWorker.doWork() started");
            SyncTaskIntentService service = new SyncTaskIntentService();
            service.runTaskSync(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "SyncTaskWorker failed");
            return Result.retry();
        }
    }
}
