package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.sync.intent.LocationIntentService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code LocationStructureServiceJob}. Runs
 * the location/structure sync (previously performed by {@link LocationIntentService} via
 * a background {@code startService()}) directly on the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy
 * {@code LocationStructureServiceJob.TAG} so existing scheduling call sites resolve this
 * worker through {@link RevealWorkerRegistry}.</p>
 */
public class LocationStructureWorker extends BaseWorker {

    public static final String TAG = "LocationStructureServiceJob";

    public LocationStructureWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("LocationStructureWorker.doWork() started");
            LocationIntentService service = new LocationIntentService();
            service.runLocationStructureSync(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "LocationStructureWorker failed");
            return Result.retry();
        }
    }
}
