package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.sync.intent.HdssSyncIntentService;

import timber.log.Timber;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.sync.intent.HdssSyncIntentService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code HdssServiceJob}. Runs the HDSS
 * sync (previously performed by {@link HdssSyncIntentService} via a background
 * {@code startService()}) directly on the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy {@code HdssServiceJob.TAG}
 * so existing scheduling call sites resolve this worker through
 * {@link RevealWorkerRegistry}.</p>
 */
public class HdssWorker extends BaseWorker {

    public static final String TAG = "HdssServiceJob";

    public HdssWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("HdssWorker.doWork() started");
            HdssSyncIntentService service = new HdssSyncIntentService();
            service.runHdssSync(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "HdssWorker failed");
            return Result.retry();
        }
    }
}

/**
 * WorkManager worker that replaces the legacy {@code HdssServiceJob}. Runs the HDSS
 * sync (previously performed by {@link HdssSyncIntentService} via a background
 * {@code startService()}) directly on the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy {@code HdssServiceJob.TAG}
 * so existing scheduling call sites resolve this worker through
 * {@link RevealWorkerRegistry}.</p>
 */
//public class HdssWorker extends BaseWorker {
//
//    public static final String TAG = "HdssServiceJob";
//
//    public HdssWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
//        super(context, workerParameters);
//    }
//
//    @NonNull
//    @Override
//    public Result doWork() {
//        try {
//            Timber.tag("SYNC_TRACE_RVL").d("HdssWorker.doWork() started");
//            HdssSyncIntentService service = new HdssSyncIntentService();
//            service.runHdssSync(getApplicationContext());
//            return Result.success();
//        } catch (Exception e) {
//            Timber.tag("Reveal Exception").w(e, "HdssWorker failed");
//            return Result.retry();
//        }
//    }
//}
