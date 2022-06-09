package org.smartregister.multitenant.check;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.smartregister.exception.PreResetAppOperationException;
import org.smartregister.view.activity.DrishtiApplication;


public interface PreResetAppCheck {

    @WorkerThread
    boolean isCheckOk(@NonNull DrishtiApplication drishtiApplication);

    @WorkerThread
    void performPreResetAppOperations(@NonNull DrishtiApplication drishtiApplication) throws PreResetAppOperationException;

    @NonNull
    String getUniqueName();
}
