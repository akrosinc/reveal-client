package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.sync.intent.ExtendedSyncIntentService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code ExtendedSyncServiceJob}. Runs
 * the extended sync (fetch new actions then trigger validation) previously performed
 * by {@link ExtendedSyncIntentService} via a background {@code startService()}
 * directly on the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy
 * {@code ExtendedSyncServiceJob.TAG} so existing scheduling call sites resolve this
 * worker through {@link RevealWorkerRegistry}.</p>
 */
public class ExtendedSyncWorker extends BaseWorker {

    public static final String TAG = "ExtendedSyncServiceJob";

    public ExtendedSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("ExtendedSyncWorker.doWork() started");
            ExtendedSyncIntentService service = new ExtendedSyncIntentService();
            service.runExtendedSync(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "ExtendedSyncWorker failed");
            return Result.retry();
        }
    }
}
