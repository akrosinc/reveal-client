package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.sync.intent.ValidateIntentService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code ValidateSyncDataServiceJob}.
 * Runs the client/event validation sync previously performed by
 * {@link ValidateIntentService} via a background {@code startService()} directly on
 * the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy
 * {@code ValidateSyncDataServiceJob.TAG} so existing scheduling call sites resolve
 * this worker through {@link RevealWorkerRegistry}.</p>
 */
public class ValidateSyncDataWorker extends BaseWorker {

    public static final String TAG = "ValidateSyncDataServiceJob";

    public ValidateSyncDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("ValidateSyncDataWorker.doWork() started");
            ValidateIntentService service = new ValidateIntentService();
            service.runValidation(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "ValidateSyncDataWorker failed");
            return Result.retry();
        }
    }
}
