package org.smartregister.reveal.job;

import android.content.Context;

import org.smartregister.CoreLibrary;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * WorkManager-based scheduler that replaces the legacy android-job {@code BaseJob}
 * static scheduling API. Provides helpers for enqueuing immediate (one-time) and
 * periodic unique work, cancelling work, and building network constraints.
 */
public class RevealWorkScheduler {

    private RevealWorkScheduler() {
        // Utility class, no instances.
    }

    /**
     * Builds {@link Constraints} that require an active network connection.
     */
    public static Constraints networkConstraints() {
        return new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
    }

    /**
     * Enqueues a one-time unique work request that runs promptly. Uses expedited
     * scheduling that degrades gracefully to a non-expedited request when the app
     * is out of expedited quota. Equivalent to the old
     * {@code BaseJob.scheduleJobImmediately(tag)}.
     */
    public static void enqueueImmediate(Context context, String uniqueName,
                                        Class<? extends ListenableWorker> workerClass) {
        enqueueImmediate(context, uniqueName, workerClass, null, false);
    }

    /**
     * Enqueues a one-time unique work request with input data.
     */
    public static void enqueueImmediate(Context context, String uniqueName,
                                        Class<? extends ListenableWorker> workerClass, Data inputData) {
        enqueueImmediate(context, uniqueName, workerClass, inputData, false);
    }

    /**
     * Enqueues a one-time unique work request, optionally requiring network connectivity.
     */
    public static void enqueueImmediate(Context context, String uniqueName,
                                        Class<? extends ListenableWorker> workerClass, Data inputData,
                                        boolean requireNetwork) {
        try {
            // Do NOT force expedited scheduling here. On Android 12+ (API 31+) the long reveal
            // sync chain quickly exhausts expedited quota; the non-expedited fallback then sits
            // with unsatisfied TIMING_DELAY/DEADLINE constraints and never becomes runnable, so
            // immediate/manual sync effectively never starts. Scheduling as normal-priority
            // one-time work lets it run promptly.
            OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(workerClass);

            if (inputData != null) {
                builder.setInputData(inputData);
            }
            if (requireNetwork) {
                builder.setConstraints(networkConstraints());
            }

            OneTimeWorkRequest request = builder.build();
            // Use REPLACE (not KEEP) for immediate/manual work so a prior request that is stuck
            // enqueued-but-not-run under the same unique name does not permanently block a fresh
            // run when the user taps Sync again.
            WorkManager.getInstance(context)
                    .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request);

            Timber.d("Enqueued immediate unique work with name " + uniqueName + " : WORK ID " + request.getId());

        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e);
        }
    }

    /**
     * Enqueues a periodic unique work request. WorkManager clamps intervals below
     * 15 minutes automatically. Equivalent to the old
     * {@code BaseJob.scheduleJob(tag, start, flex)}.
     */
    public static void enqueuePeriodic(Context context, String uniqueName,
                                       Class<? extends ListenableWorker> workerClass,
                                       long intervalMinutes, long flexMinutes) {
        enqueuePeriodic(context, uniqueName, workerClass, intervalMinutes, flexMinutes, false);
    }

    /**
     * Enqueues a periodic unique work request, optionally requiring network connectivity.
     */
    public static void enqueuePeriodic(Context context, String uniqueName,
                                       Class<? extends ListenableWorker> workerClass,
                                       long intervalMinutes, long flexMinutes,
                                       boolean requireNetwork) {
        try {
            PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(workerClass,
                    intervalMinutes, TimeUnit.MINUTES,
                    flexMinutes, TimeUnit.MINUTES);

            if (requireNetwork) {
                builder.setConstraints(networkConstraints());
            }

            PeriodicWorkRequest request = builder.build();
            WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(uniqueName, ExistingPeriodicWorkPolicy.KEEP, request);

            Timber.d("Enqueued periodic unique work with name " + uniqueName + " : WORK ID " + request.getId()
                    + " every " + intervalMinutes + " minutes with flex value of " + flexMinutes + " minutes");

        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e);
        }
    }

    /**
     * Convenience shortcut that resolves the worker class for the given tag via
     * {@link RevealWorkerRegistry} and enqueues immediate one-time work using the
     * application context from {@link CoreLibrary}. Replaces the removed
     * {@code BaseJob.scheduleJobImmediately(tag)} entry point so callers only need
     * to swap {@code BaseJob} for {@code RevealWorkScheduler}.
     *
     * @param tag unique work name / legacy job tag
     */
    public static void scheduleJobImmediately(String tag) {
        Class<? extends ListenableWorker> workerClass = RevealWorkerRegistry.workerForTag(tag);
        if (workerClass == null) {
            Timber.w("No worker registered for tag %s; skipping immediate schedule", tag);
            return;
        }
        enqueueImmediate(getApplicationContext(), tag, workerClass);
    }

    /**
     * Convenience shortcut that resolves the worker class for the given tag via
     * {@link RevealWorkerRegistry} and enqueues periodic work using the application
     * context from {@link CoreLibrary}. Replaces the removed
     * {@code BaseJob.scheduleJob(tag, start, flex)} entry point.
     *
     * @param tag           unique work name / legacy job tag
     * @param startMinutes  interval in minutes (WorkManager clamps values below 15 minutes)
     * @param flexMinutes   flex window in minutes
     */
    public static void scheduleJob(String tag, long startMinutes, long flexMinutes) {
        Class<? extends ListenableWorker> workerClass = RevealWorkerRegistry.workerForTag(tag);
        if (workerClass == null) {
            Timber.w("No worker registered for tag %s; skipping periodic schedule", tag);
            return;
        }
        // Periodic and immediate work must NOT share a unique work name. WorkManager keeps unique
        // one-time and periodic work in the same namespace, so reusing the tag makes the idle
        // periodic registration (perpetually ENQUEUED between windows) collide with immediate work
        // — either blocking the immediate sync or having the immediate REPLACE cancel the periodic
        // schedule. Give periodic work a distinct name; the worker class is still resolved from the
        // base tag so RevealWorkerRegistry lookups are unaffected.
        enqueuePeriodic(getApplicationContext(), periodicUniqueName(tag), workerClass, startMinutes, flexMinutes, false);
    }

    /**
     * Builds the distinct unique work name used for periodic scheduling so it never collides with
     * the immediate/one-time work registered under the raw {@code tag}.
     */
    public static String periodicUniqueName(String tag) {
        return tag + "-periodic";
    }

    private static Context getApplicationContext() {
        return CoreLibrary.getInstance().context().applicationContext();
    }

    /**
     * Returns {@code true} only when the unique work identified by {@code uniqueName} is currently
     * {@link WorkInfo.State#RUNNING} — i.e. a sync is actually executing right now.
     *
     * <p>We deliberately do NOT treat {@link WorkInfo.State#ENQUEUED} as "in progress". The
     * periodic sync is registered under the same unique name and sits {@code ENQUEUED} between
     * its interval windows; counting that as active would make {@link Utils#startImmediateSync()}
     * skip every login/manual sync (nothing happens until a force-restart cancels the periodic
     * work). Only a genuinely {@code RUNNING} worker should block a new immediate sync.</p>
     *
     * <p>This queries WorkManager directly rather than relying on a manually-maintained
     * "sync in progress" boolean flag, which could get stranded {@code true} if a worker path
     * failed to clear it and would then block every subsequent sync for the process lifetime.</p>
     *
     * <p>The query is bounded by a short timeout and fails <b>open</b> (returns {@code false})
     * on any error, so a query hiccup can never prevent a sync from starting.</p>
     */
    public static boolean isWorkActive(Context context, String uniqueName) {
        try {
            List<WorkInfo> infos = WorkManager.getInstance(context)
                    .getWorkInfosForUniqueWork(uniqueName)
                    .get(2, TimeUnit.SECONDS);
            if (infos == null) {
                return false;
            }
            for (WorkInfo info : infos) {
                if (info.getState() == WorkInfo.State.RUNNING) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "isWorkActive query failed; treating work as not active");
            return false;
        }
    }

    /**
     * Cancels all work enqueued via WorkManager. Equivalent to the old
     * {@code JobManager.cancelAll()}.
     */
    public static void cancelAll(Context context) {
        WorkManager.getInstance(context).cancelAllWork();
        Timber.d("Cancelled all WorkManager work");
    }

    /**
     * Cancels the unique work identified by the given name.
     */
    public static void cancelByName(Context context, String uniqueName) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName);
        Timber.d("Cancelled unique work with name " + uniqueName);
    }
}
