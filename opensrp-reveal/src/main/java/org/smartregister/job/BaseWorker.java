package org.smartregister.job;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Abstract base class for WorkManager workers that replace the legacy android-job
 * {@code BaseJob} subclasses. Concrete workers created in later migration tasks
 * extend this class and implement {@code doWork()}.
 *
 * <p>{@link Worker} already provides {@code getApplicationContext()} (declared
 * {@code final} on {@code ListenableWorker}); subclasses use it directly to reach
 * the application {@link Context} from within {@code doWork()}.</p>
 */
public abstract class BaseWorker extends Worker {

    public BaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }
}
