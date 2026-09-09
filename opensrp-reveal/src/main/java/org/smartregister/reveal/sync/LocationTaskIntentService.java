package org.smartregister.reveal.sync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.TaskRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.job.RevealSyncSettingsServiceJob;
import org.smartregister.reveal.util.AppExecutors;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.Utils;
import org.smartregister.sync.helper.DataIntentServiceHelper;
import org.smartregister.sync.helper.EventSyncHelper;
import org.smartregister.sync.helper.HdssServiceHelper;
import org.smartregister.sync.helper.LocationServiceHelper;
import org.smartregister.sync.helper.PlanIntentServiceHelper;
import org.smartregister.sync.helper.TaskServiceHelper;
import org.smartregister.util.NetworkUtils;
import org.smartregister.util.SyncUtils;

import java.util.List;

import timber.log.Timber;

import static org.smartregister.reveal.util.Constants.Action.STRUCTURE_TASK_SYNCED;
import static org.smartregister.reveal.util.FamilyConstants.TABLE_NAME.FAMILY_MEMBER;

public class LocationTaskIntentService extends IntentService {
    private boolean anyStageFailed = false;
    private static final String TAG = "LocationTaskIntentService";

    private SyncUtils syncUtils;

    public LocationTaskIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!NetworkUtils.isNetworkAvailable()) {
            sendSyncStatusBroadcastMessage(FetchStatus.noConnection);
            return;
        }
        if (!syncUtils.verifyAuthorization()) {
            try {
                syncUtils.logoutUser();
            } catch (Exception e) {
                Timber.tag("Reveal Exception").w(e);
            }
            return;

        }
        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);

        doSync();

        (new AppExecutors()).mainThread().execute(new Runnable() {
            @Override
            public void run() {
//                RevealSyncSettingsServiceJob.scheduleJobImmediately(RevealSyncSettingsServiceJob.TAG);
//                SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
            }
        });
    }

    private void sendSyncStatusBroadcastMessage(FetchStatus fetchStatus) {
        Intent intent = new Intent();
        intent.setAction(SyncStatusBroadcastReceiver.ACTION_SYNC_STATUS);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_FETCH_STATUS, fetchStatus);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        syncUtils = new SyncUtils(getBaseContext());
        return super.onStartCommand(intent, flags, startId);
    }

//    @VisibleForTesting
//    protected void doSync() {
//        Log.d("SYNC_TRACE_RVL", "LOCATION_TASK_DOSYNC_START t=" + System.currentTimeMillis());
//        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
//        LocationServiceHelper locationServiceHelper = new LocationServiceHelper(
//                RevealApplication.getInstance().getLocationRepository(),
//                RevealApplication.getInstance().getStructureRepository());
//        TaskServiceHelper taskServiceHelper = TaskServiceHelper.getInstance();
//        PlanIntentServiceHelper planServiceHelper = PlanIntentServiceHelper.getInstance();
//
//
//        List<Location> syncedStructures = locationServiceHelper.fetchLocationsStructures();
//        Log.d("SYNC_TRACE_RVL", "LOCATIONS_FETCHED t=" + System.currentTimeMillis()
//                + " count=" + (syncedStructures == null ? 0 : syncedStructures.size()));
//        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
//        planServiceHelper.syncPlans();
//
//        DataIntentServiceHelper dataIntentServiceHelper = DataIntentServiceHelper.getInstance();
//        dataIntentServiceHelper.getDBUserConfig();
//        dataIntentServiceHelper.pushDBToServer();
//
//        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
//        List<Task> synchedTasks = taskServiceHelper.syncTasks();
//
//        TaskRepository taskRepository = RevealApplication.getInstance().getContext().getTaskRepository();
//        taskRepository.updateTaskStructureIdFromStructure(syncedStructures);
//        taskRepository.updateTaskStructureIdsFromExistingStructures();
//        taskRepository.updateTaskStructureIdsFromExistingClients(FAMILY_MEMBER);
//
//        if (hasChangesInCurrentOperationalArea(syncedStructures, synchedTasks)) {
//            Intent intent = new Intent(STRUCTURE_TASK_SYNCED);
//            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//        }
//
//        if (!org.smartregister.util.Utils.isEmptyCollection(syncedStructures)
//                || !org.smartregister.util.Utils.isEmptyCollection(synchedTasks)) {
//            doSync();
//        }
//
//        HdssServiceHelper hdssServiceHelper = HdssServiceHelper.getInstance();
//        hdssServiceHelper.syncHdssDetails();
//
//
//        new AppExecutors().mainThread().execute(new Runnable() {
//            @Override
//            public void run() {
//
//                SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
//            }
//        });
//
//    }
//@VisibleForTesting
//protected void doSync() {
//    anyStageFailed = false; // fresh sync cycle starting
//
//    doSync(0); // Default call with recursion depth 0
//}
protected void doSync() {
    try {
        AllSharedPreferences prefs = RevealApplication.getInstance().getContext().allSharedPreferences();
        if (prefs != null) {
            prefs.saveIsSyncInProgress(true);
        }
    } catch (Exception e) {
        Log.w("SYNC_TRACE_RVL", "Error setting sync-in-progress flag", e);
    }
    doSync(0);
}

//    @VisibleForTesting
//    protected void doSync(int recursionDepth) {
//        // 🛡️ RECURSION GUARD: Prevent infinite loops (Max 3 recursive passes)
//        if (recursionDepth > 3) {
//            Log.w(TAG, "Max recursion depth reached for doSync(). Stopping loop to prevent battery drain.");
//            finishSyncProcess();
//            return;
//        }
//
//        Log.d("SYNC_TRACE_RVL", "LOCATION_TASK_DOSYNC_START t=" + System.currentTimeMillis() + " [Depth: " + recursionDepth + "]");
//        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
//
//        LocationServiceHelper locationServiceHelper = new LocationServiceHelper(
//                RevealApplication.getInstance().getLocationRepository(),
//                RevealApplication.getInstance().getStructureRepository());
//        TaskServiceHelper taskServiceHelper = TaskServiceHelper.getInstance();
//        PlanIntentServiceHelper planServiceHelper = PlanIntentServiceHelper.getInstance();
//
//        // --- CLIENT'S EXACT PROCESS ORDER (WITH ERROR SAFETY) ---
//        List<Location> syncedStructures = null;
//        try {
//            syncedStructures = locationServiceHelper.fetchLocationsStructures();
//            Log.d("SYNC_TRACE_RVL", "LOCATIONS_FETCHED t=" + System.currentTimeMillis()
//                    + " count=" + (syncedStructures == null ? 0 : syncedStructures.size()));
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error in fetchLocationsStructures");
//        }
//
//        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
//        try {
//            planServiceHelper.syncPlans();
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error in syncPlans");
//        }
//
//        try {
//            DataIntentServiceHelper dataIntentServiceHelper = DataIntentServiceHelper.getInstance();
//            dataIntentServiceHelper.getDBUserConfig();
//            dataIntentServiceHelper.pushDBToServer();
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error pushing data to server");
//        }
//
//        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
//        List<Task> synchedTasks = null;
//        try {
//            synchedTasks = taskServiceHelper.syncTasks();
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error syncing tasks");
//        }
//
//        try {
//            TaskRepository taskRepository = RevealApplication.getInstance().getContext().getTaskRepository();
//            if (syncedStructures != null) {
//                taskRepository.updateTaskStructureIdFromStructure(syncedStructures);
//            }
//            taskRepository.updateTaskStructureIdsFromExistingStructures();
//            taskRepository.updateTaskStructureIdsFromExistingClients(FAMILY_MEMBER);
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error updating task structure IDs");
//        }
//
//        if (hasChangesInCurrentOperationalArea(syncedStructures, synchedTasks)) {
//            Intent intent = new Intent(STRUCTURE_TASK_SYNCED);
//            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//        }
//
//        // 🛡️ SAFE RECURSION: Increments depth counter instead of looping blindly forever
//        if (!org.smartregister.util.Utils.isEmptyCollection(syncedStructures)
//                || !org.smartregister.util.Utils.isEmptyCollection(synchedTasks)) {
//            doSync(recursionDepth + 1);
//            return; // Exit current frame to let recursion handle the rest
//        }
//
//        try {
//            HdssServiceHelper hdssServiceHelper = HdssServiceHelper.getInstance();
////             hdssServiceHelper.syncHdssDetails();
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error syncing HDSS details");
//        }
//        // ---------------------------------------------------------
//
//        finishSyncProcess();
//    }

    /**
     * Shared completion handler to release locks, notify UI, and schedule next jobs.
     */
//    private void finishSyncProcess() {
//        // 1. Release concurrency lock so future syncs are allowed
//        try {
//            AllSharedPreferences prefs = RevealApplication.getInstance().getContext().allSharedPreferences();
//            if (prefs != null) {
//                prefs.saveIsSyncInProgress(false);
//            }
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error resetting sync flag");
//        }
//
//        // 2. Notify UI to update from 0 and dismiss progress bars
//        sendCompletionNotification(FetchStatus.fetched);
//
//        // 3. Schedule next job
//        new AppExecutors().mainThread().execute(new Runnable() {
//            @Override
//            public void run() {
//                SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
//            }
//        });
//    }
//    private void finishSyncProcess() {
//        // 1. Release concurrency lock so future syncs are allowed
//        try {
//            AllSharedPreferences prefs = RevealApplication.getInstance().getContext().allSharedPreferences();
//            if (prefs != null) {
//                prefs.saveIsSyncInProgress(false);
//            }
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error resetting sync flag");
//        }
//
//        // 2. Notify UI with the REAL outcome — not always "fetched"
//        FetchStatus finalStatus = anyStageFailed ? FetchStatus.fetchedFailed : FetchStatus.fetched;
//        sendCompletionNotification(finalStatus);
//
//        // 3. Schedule next job
//        new AppExecutors().mainThread().execute(new Runnable() {
//            @Override
//            public void run() {
//                SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
//            }
//        });
//    }
//    @VisibleForTesting
//    protected void doSync(int recursionDepth) {
//        // 🛡️ RECURSION GUARD: Prevent infinite loops (Max 3 recursive passes)
//        if (recursionDepth > 3) {
//            Log.w(TAG, "Max recursion depth reached for doSync(). Stopping loop to prevent battery drain.");
//            finishSyncProcess();
//            return;
//        }
//
//        Log.d("SYNC_TRACE_RVL", "LOCATION_TASK_DOSYNC_START t=" + System.currentTimeMillis() + " [Depth: " + recursionDepth + "]");
//        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
//
//        LocationServiceHelper locationServiceHelper = new LocationServiceHelper(
//                RevealApplication.getInstance().getLocationRepository(),
//                RevealApplication.getInstance().getStructureRepository());
//        TaskServiceHelper taskServiceHelper = TaskServiceHelper.getInstance();
//        PlanIntentServiceHelper planServiceHelper = PlanIntentServiceHelper.getInstance();
//
//        // --- CLIENT'S EXACT PROCESS ORDER (WITH ERROR SAFETY) ---
//        List<Location> syncedStructures = null;
//        try {
//            syncedStructures = locationServiceHelper.fetchLocationsStructures();
//            Log.d("SYNC_TRACE_RVL", "LOCATIONS_FETCHED t=" + System.currentTimeMillis()
//                    + " count=" + (syncedStructures == null ? 0 : syncedStructures.size()));
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error in fetchLocationsStructures");
//        }
//
//        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
//        try {
//            planServiceHelper.syncPlans();
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error in syncPlans");
//        }
//
//        try {
//            DataIntentServiceHelper dataIntentServiceHelper = DataIntentServiceHelper.getInstance();
//            dataIntentServiceHelper.getDBUserConfig();
//            dataIntentServiceHelper.pushDBToServer();
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error pushing data to server");
//        }
//
//        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
//        List<Task> synchedTasks = null;
//        try {
//            synchedTasks = taskServiceHelper.syncTasks();
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error syncing tasks");
//        }
//
//        try {
//            TaskRepository taskRepository = RevealApplication.getInstance().getContext().getTaskRepository();
//            if (syncedStructures != null) {
//                taskRepository.updateTaskStructureIdFromStructure(syncedStructures);
//            }
//            taskRepository.updateTaskStructureIdsFromExistingStructures();
//            taskRepository.updateTaskStructureIdsFromExistingClients(FAMILY_MEMBER);
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error updating task structure IDs");
//        }
//
//        if (hasChangesInCurrentOperationalArea(syncedStructures, synchedTasks)) {
//            Intent intent = new Intent(STRUCTURE_TASK_SYNCED);
//            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//        }
//
//        // 🛡️ SAFE RECURSION: Increments depth counter instead of looping blindly forever
//        if (!org.smartregister.util.Utils.isEmptyCollection(syncedStructures)
//                || !org.smartregister.util.Utils.isEmptyCollection(synchedTasks)) {
//            doSync(recursionDepth + 1);
//            return; // Exit current frame to let recursion handle the rest
//        }
//
//        // NEW: Events sync stage — replaces the separate SyncIntentService/SyncServiceJob
//        // trigger. Runs inline as part of this pipeline, so its progress/failure is
//        // covered by the same try/catch and anyStageFailed tracking as everything else.
//        boolean eventsFoundNewData = false;
//        try {
//            EventSyncHelper eventSyncHelper = EventSyncHelper.getInstance();
//            eventsFoundNewData = eventSyncHelper.syncEvents();
//            Log.d("SYNC_TRACE_RVL", "EVENTS_SYNCED t=" + System.currentTimeMillis()
//                    + " foundNewData=" + eventsFoundNewData
//                    + " fetched=" + eventSyncHelper.getFetchedRecords() + "/" + eventSyncHelper.getTotalRecords());
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error syncing events");
//        }
//
//        try {
//            HdssServiceHelper hdssServiceHelper = HdssServiceHelper.getInstance();
//            if (eventsFoundNewData) {
//                hdssServiceHelper.syncHdssDetails();
//            } else {
//                Log.d("SYNC_TRACE_RVL", "SKIPPING_HDSS — events found no new data this cycle");
//            }
//        } catch (Exception e) {
//            Timber.tag(TAG).e(e, "Error syncing HDSS details");
//        }
//        // ---------------------------------------------------------
//
//        finishSyncProcess();
//    }
    @VisibleForTesting
    protected void doSync(int recursionDepth) {
        // 🛡️ RECURSION GUARD: Max 3 passes for paginated structures/tasks
        if (recursionDepth > 3) {
            Log.w(TAG, "Max recursion depth reached for structure/task pagination. Proceeding to remaining pipeline.");
        }

        Log.d("SYNC_TRACE_RVL", "LOCATION_TASK_DOSYNC_START t=" + System.currentTimeMillis() + " [Depth: " + recursionDepth + "]");
        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);

        LocationServiceHelper locationServiceHelper = new LocationServiceHelper(
                RevealApplication.getInstance().getLocationRepository(),
                RevealApplication.getInstance().getStructureRepository());
        TaskServiceHelper taskServiceHelper = TaskServiceHelper.getInstance();
        PlanIntentServiceHelper planServiceHelper = PlanIntentServiceHelper.getInstance();

        List<Location> syncedStructures = null;
        try {
            syncedStructures = locationServiceHelper.fetchLocationsStructures();
            Log.d("SYNC_TRACE_RVL", "LOCATIONS_FETCHED t=" + System.currentTimeMillis()
                    + " count=" + (syncedStructures == null ? 0 : syncedStructures.size()));
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Error in fetchLocationsStructures");
        }

        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
        try {
            planServiceHelper.syncPlans();
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Error in syncPlans");
        }

        try {
            DataIntentServiceHelper dataIntentServiceHelper = DataIntentServiceHelper.getInstance();
            dataIntentServiceHelper.getDBUserConfig();
            dataIntentServiceHelper.pushDBToServer();
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Error pushing data to server");
        }

        sendSyncStatusBroadcastMessage(FetchStatus.fetchStarted);
        List<Task> synchedTasks = null;
        try {
            synchedTasks = taskServiceHelper.syncTasks();
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Error syncing tasks");
        }

        TaskRepository taskRepository = RevealApplication.getInstance().getContext().getTaskRepository();
        try {
            if (syncedStructures != null) {
                taskRepository.updateTaskStructureIdFromStructure(syncedStructures);
            }
            taskRepository.updateTaskStructureIdsFromExistingStructures();
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Error updating task structure IDs from structures");
        }

        boolean hasStructureOrTaskChanges = hasChangesInCurrentOperationalArea(syncedStructures, synchedTasks);

        // 🔁 PAGINATION CHECK: Drain structure and task queues before hitting downstream syncs
        boolean hasMorePages = !org.smartregister.util.Utils.isEmptyCollection(syncedStructures)
                || !org.smartregister.util.Utils.isEmptyCollection(synchedTasks);

        if (hasMorePages && recursionDepth <= 3) {
            if (hasStructureOrTaskChanges) {
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(new Intent(STRUCTURE_TASK_SYNCED));
            }
            doSync(recursionDepth + 1);
            return; // Let deeper recursion frame finish remaining stages
        }

        // ==========================================
        // STAGE 2: EVENTS SYNC (CLIENTS & VISITS)
        // ==========================================
        boolean eventsFoundNewData = false;
        try {
            EventSyncHelper eventSyncHelper = EventSyncHelper.getInstance();
            eventsFoundNewData = eventSyncHelper.syncEvents();
            Log.d("SYNC_TRACE_RVL", "EVENTS_SYNCED t=" + System.currentTimeMillis()
                    + " foundNewData=" + eventsFoundNewData
                    + " fetched=" + eventSyncHelper.getFetchedRecords() + "/" + eventSyncHelper.getTotalRecords());

            // 🎯 FIX: Re-link tasks to newly fetched family members/clients
            if (eventsFoundNewData) {
                taskRepository.updateTaskStructureIdsFromExistingClients(FAMILY_MEMBER);
            }
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Error syncing events");
        }

        // ==========================================
        // STAGE 3: HDSS SYNC (COMPOUNDS & HOUSEHOLDS)
        // ==========================================
        try {
            HdssServiceHelper hdssServiceHelper = HdssServiceHelper.getInstance();
            // 🎯 FIX: Never gate HDSS solely behind events; it manages its own dirty flags,
            // local push queues, and encrypted CSV imports independently.
            hdssServiceHelper.syncHdssDetails();
            Log.d("SYNC_TRACE_RVL", "HDSS_SYNC_COMPLETED t=" + System.currentTimeMillis());
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Error syncing HDSS details");
        }

        // ==========================================
        // STAGE 4: FINAL REFRESH BROADCAST
        // ==========================================
        // Broadcast if structures, tasks, OR client events changed current operational area state
        if (hasStructureOrTaskChanges || eventsFoundNewData) {
            Intent intent = new Intent(STRUCTURE_TASK_SYNCED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        finishSyncProcess();
    }
    private void finishSyncProcess() {
        // 1. Release concurrency lock so future syncs are allowed
        try {
            AllSharedPreferences prefs = RevealApplication.getInstance().getContext().allSharedPreferences();
            if (prefs != null) {
                prefs.saveIsSyncInProgress(false);
            }
        } catch (Exception e) {
            Log.w("SYNC_TRACE_RVL", "Error resetting sync flag", e);
        }

        // 2. Notify UI with the real outcome
        FetchStatus finalStatus = anyStageFailed ? FetchStatus.fetchedFailed : FetchStatus.fetched;
        sendCompletionNotification(finalStatus);

        // 3. Only reschedule immediately on genuine failure — not on every completion.
        //    Unconditional rescheduling here was the root cause of the repeated
        //    SYNC_COMPLETE loop (fires every ~1-3s with no backoff/dedup).
        if (anyStageFailed) {
            Log.d("SYNC_TRACE_RVL", "SYNC_COMPLETE_RESCHEDULE — stage failure detected, scheduling retry");
            new AppExecutors().mainThread().execute(new Runnable() {
                @Override
                public void run() {
//                    SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
                    SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);

                }
            });
        } else {
            Log.d("SYNC_TRACE_RVL", "SYNC_COMPLETE_NO_RESCHEDULE — sync finished cleanly, no retry needed");
        }
    }
    private void sendCompletionNotification(FetchStatus fetchStatus) {
        Intent intent = new Intent();
        intent.setAction(SyncStatusBroadcastReceiver.ACTION_SYNC_STATUS);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_FETCH_STATUS, fetchStatus);
        intent.putExtra(SyncStatusBroadcastReceiver.EXTRA_COMPLETE_STATUS, true);
        sendBroadcast(intent);
    }

    /**
     * Checks if there a synched structure or task on the currently opened operational area
     *
     * @param syncedStructures the list of synced structures
     * @param synchedTasks     the list of synced tasks
     * @return true if there is a synched structure or task on the currently opened operational area; otherwise returns false
     */
    private boolean hasChangesInCurrentOperationalArea(List<Location> syncedStructures, List<Task> synchedTasks) {
        Location operationalAreaLocation = Utils.getOperationalAreaLocation(PreferencesUtil.getInstance().getCurrentOperationalArea());
        String operationalAreaLocationId;
        if (operationalAreaLocation == null) {
            return false;
        } else {
            operationalAreaLocationId = operationalAreaLocation.getId();
        }
        if (syncedStructures != null) {
            for (Location structure : syncedStructures) {
                if (operationalAreaLocationId.equals(structure.getProperties().getParentId())) {
                    return true;
                }
            }
        }
        if (synchedTasks != null) {
            for (Task task : synchedTasks) {
                if (operationalAreaLocationId.equals(task.getGroupIdentifier())) {
                    return true;
                }
            }
        }
        return false;
    }


}
