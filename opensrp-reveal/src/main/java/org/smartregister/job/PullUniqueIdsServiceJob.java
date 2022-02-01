package org.smartregister.job;

import android.content.Intent;

import androidx.annotation.NonNull;

import org.smartregister.AllConstants;
import org.smartregister.sync.intent.PullUniqueIdsIntentService;

/**
 * Created by ndegwamartin on 06/09/2018.
 */
public class PullUniqueIdsServiceJob extends BaseJob {

    public static final String TAG = "PullUniqueIdsServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), PullUniqueIdsIntentService.class);
        getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
