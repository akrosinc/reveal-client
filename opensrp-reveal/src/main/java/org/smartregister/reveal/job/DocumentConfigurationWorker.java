package org.smartregister.reveal.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.smartregister.job.BaseWorker;
import org.smartregister.sync.intent.DocumentConfigurationIntentService;

import timber.log.Timber;

/**
 * WorkManager worker that replaces the legacy {@code DocumentConfigurationServiceJob}.
 * Runs the document configuration sync (previously performed by
 * {@link DocumentConfigurationIntentService} via a background {@code startService()})
 * directly on the worker thread.
 *
 * <p>The unique work name/{@link #TAG} matches the legacy
 * {@code DocumentConfigurationServiceJob.TAG} so existing scheduling call sites resolve
 * this worker through {@link RevealWorkerRegistry}.</p>
 */
public class DocumentConfigurationWorker extends BaseWorker {

    public static final String TAG = "DocumentConfigurationServiceJob";

    public DocumentConfigurationWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.tag("SYNC_TRACE_RVL").d("DocumentConfigurationWorker.doWork() started");
            DocumentConfigurationIntentService service = new DocumentConfigurationIntentService();
            service.runDocumentConfigurationSync(getApplicationContext());
            return Result.success();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "DocumentConfigurationWorker failed");
            return Result.retry();
        }
    }
}
