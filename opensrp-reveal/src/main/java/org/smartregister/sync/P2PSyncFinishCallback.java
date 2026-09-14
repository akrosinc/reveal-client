package org.smartregister.sync;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.smartregister.reveal.job.RevealWorkScheduler;
import org.smartregister.reveal.job.P2pProcessRecordsWorker;
import org.smartregister.p2p.callback.SyncFinishedCallback;

import java.util.HashMap;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 10/05/2019
 */

public class P2PSyncFinishCallback implements SyncFinishedCallback {

    @Override
    public void onSuccess(@NonNull HashMap<String, Integer> hashMap) {
        scheduleProcessJob();
    }

    @Override
    public void onFailure(@NonNull Exception e, @Nullable HashMap<String, Integer> hashMap) {
        scheduleProcessJob();
    }

    private void scheduleProcessJob(){
        RevealWorkScheduler.scheduleJobImmediately(P2pProcessRecordsWorker.TAG);
    }
}
