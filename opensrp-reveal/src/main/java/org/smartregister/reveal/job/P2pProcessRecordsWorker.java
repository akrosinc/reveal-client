package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.sync.intent.P2pProcessRecordsService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code P2pServiceJob}. Processes pending
 * peer-to-peer records (previously performed by {@link P2pProcessRecordsService} via a
 * background {@code startService()}) directly on the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy {@code P2pServiceJob.TAG}
 * value ({@code "P2PServiceJob"}) so existing scheduling call sites resolve this worker
 * through {@link RevealWorkerRegistry}.</p>
 */
public class P2pProcessRecordsWorker extends BaseWorker {

    public static final String TAG = "P2PServiceJob";

    public P2pProcessRecordsWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("P2pProcessRecordsWorker.doWork() started");
            P2pProcessRecordsService service = new P2pProcessRecordsService();
            service.runProcessRecords(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "P2pProcessRecordsWorker failed");
            return Result.retry();
        }
    }
}
