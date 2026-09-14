package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.sync.intent.PlanIntentService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code PlanIntentServiceJob}. Runs the
 * plan sync (previously performed by {@link PlanIntentService} via a background
 * {@code startService()}) directly on the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy
 * {@code PlanIntentServiceJob.TAG} value verbatim, including the non-ASCII
 * {@code 'æ'} character ({@code "PlanIntenætServiceJob"}), so existing scheduling call
 * sites resolve this worker through {@link RevealWorkerRegistry}.</p>
 */
public class PlanWorker extends BaseWorker {

    public static final String TAG = "PlanIntenætServiceJob";

    public PlanWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("PlanWorker.doWork() started");
            PlanIntentService service = new PlanIntentService();
            service.runPlanSync(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "PlanWorker failed");
            return Result.retry();
        }
    }
}
