package org.smartregister.reveal.job;

import androidx.work.ListenableWorker;

/**
 * Maps a legacy job tag to the WorkManager {@link ListenableWorker} class that
 * should be scheduled for it. Replaces the tag&rarr;Job factory role that
 * {@code RevealJobCreator} played for android-job.
 *
 * <p>During migration this registry is intentionally empty. Later tasks (3&ndash;5)
 * populate the switch as each concrete {@code XxxWorker} is created, at which point
 * the {@code BaseJob} shim will resolve real worker classes and schedule them via
 * {@link RevealWorkScheduler}.</p>
 */
public class RevealWorkerRegistry {

    private RevealWorkerRegistry() {
        // Utility class, no instances.
    }

    /**
     * Returns the worker class registered for the given tag, or {@code null} when no
     * worker has been migrated for that tag yet.
     *
     * @param tag the legacy job tag (also used as the WorkManager unique work name)
     * @return the worker class, or {@code null} if not yet registered
     */
    public static Class<? extends ListenableWorker> workerForTag(String tag) {
        if (tag == null) {
            return null;
        }
        switch (tag) {
            case SyncWorker.TAG:
                return SyncWorker.class;
            case ExtendedSyncWorker.TAG:
                return ExtendedSyncWorker.class;
            case ValidateSyncDataWorker.TAG:
                return ValidateSyncDataWorker.class;
            case LocationTaskWorker.TAG:
                return LocationTaskWorker.class;
            case RevealSyncSettingsWorker.TAG:
                return RevealSyncSettingsWorker.class;
            case DocumentConfigurationWorker.TAG:
                return DocumentConfigurationWorker.class;
            case HdssWorker.TAG:
                return HdssWorker.class;
            case ImageUploadWorker.TAG:
                return ImageUploadWorker.class;
            case LocationStructureWorker.TAG:
                return LocationStructureWorker.class;
            case P2pProcessRecordsWorker.TAG:
                return P2pProcessRecordsWorker.class;
            case PlanWorker.TAG:
                return PlanWorker.class;
            case SyncSettingsWorker.TAG:
                return SyncSettingsWorker.class;
            case SyncTaskWorker.TAG:
                return SyncTaskWorker.class;
            default:
                return null;
        }
    }
}
