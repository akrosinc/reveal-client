package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.reveal.sync.LocationTaskIntentService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code LocationTaskServiceJob}. Runs the
 * location/task sync (previously performed by {@link LocationTaskIntentService} via a
 * background {@code startService()}) directly on the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy
 * {@code LocationTaskServiceJob.TAG} so existing scheduling call sites resolve this
 * worker through {@link RevealWorkerRegistry}.</p>
 */
public class LocationTaskWorker extends BaseWorker {

    public static final String TAG = "LocationTaskServiceJob";

    public LocationTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("LocationTaskWorker.doWork() started");
            LocationTaskIntentService service = new LocationTaskIntentService();
            service.runLocationTaskSync(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "LocationTaskWorker failed");
            return Result.retry();
        }
    }
}
