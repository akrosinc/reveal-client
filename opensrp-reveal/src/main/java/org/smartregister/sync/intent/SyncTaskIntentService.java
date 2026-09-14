package org.smartregister.sync.intent;

import android.content.Intent;

import org.smartregister.sync.helper.TaskServiceHelper;

public class SyncTaskIntentService extends BaseSyncIntentService {
    private static final String TAG = "SyncTaskIntentService";
    private TaskServiceHelper taskServiceHelper;

    public SyncTaskIntentService() {
        super(TAG);
    }

    public SyncTaskIntentService(TaskServiceHelper taskServiceHelper) {
        super(TAG);
        this.taskServiceHelper = taskServiceHelper;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (taskServiceHelper == null) {
            taskServiceHelper = TaskServiceHelper.getInstance();
        }
        super.onHandleIntent(intent);
        taskServiceHelper.syncTasks();
    }

    /**
     * Entry point for WorkManager workers. Runs the task sync synchronously on the
     * caller's (worker) thread without the {@link android.app.IntentService} lifecycle
     * or a background {@code startService()}.
     *
     * @param appContext the application context provided by the Worker
     */
    public void runTaskSync(@androidx.annotation.NonNull android.content.Context appContext) {
        if (getBaseContext() == null) {
            attachBaseContext(appContext);
        }
        onHandleIntent(new Intent());
    }

}