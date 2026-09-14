package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.service.ImageUploadSyncService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code ImageUploadServiceJob}. Uploads
 * unsynced profile images (previously performed by {@link ImageUploadSyncService} via a
 * background {@code startService()}) directly on the worker thread.
 *
 * <p>{@link ImageUploadSyncService} extends the plain {@code android.app.IntentService}
 * (not {@code BaseSyncIntentService}); its work is driven through the
 * {@code runImageUpload(Context)} runner added for this migration.</p>
 *
 * <p>The unique work name/{@link #TAG} matches the legacy
 * {@code ImageUploadServiceJob.TAG} so existing scheduling call sites resolve this
 * worker through {@link RevealWorkerRegistry}.</p>
 */
public class ImageUploadWorker extends BaseWorker {

    public static final String TAG = "ImageUploadServiceJob";

    public ImageUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("ImageUploadWorker.doWork() started");
            ImageUploadSyncService service = new ImageUploadSyncService();
            service.runImageUpload(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "ImageUploadWorker failed");
            return Result.retry();
        }
    }
}
