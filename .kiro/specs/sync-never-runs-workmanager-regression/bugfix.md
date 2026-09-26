# Bugfix Requirements Document

## Introduction

After migrating reveal-client's background jobs from `com.evernote:android-job` to `androidx.work` (WorkManager), backend sync never runs. On a fresh login and on every manual "Sync" button tap, no sync work actually executes. On-device evidence on a Samsung SM-X205 (Android 14) confirms the symptom: tapping Sync logs `startImmediateSync() proceeding — flag set true`, but no `LocationTaskWorker.doWork() started` ever follows. A second tap logs `startImmediateSync() skipped — sync already in progress`, and `dumpsys jobscheduler` shows the enqueued WorkManager jobs stuck with `Unsatisfied constraints: TIMING_DELAY DEADLINE` — the signature of expedited work that was quota-denied and deferred indefinitely. WorkManager itself initializes correctly, so initialization is not the cause.

This regression is caused by three interacting defects, all treated here as a single regression:

- **Defect A — Expedited scheduling starvation.** `RevealWorkScheduler.enqueueImmediate(...)` always calls `.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)`. On Android 12+, once expedited quota is exhausted (the reveal sync chains many jobs: LocationTask → Sync → Hdss → ExtendedSync → Validate → SyncSettings), the non-expedited fallback acquires TIMING_DELAY/DEADLINE constraints and effectively never becomes runnable.
- **Defect B — Sync-in-progress flag never cleared.** `Utils.startImmediateSync()` sets `saveIsSyncInProgress(true)` and guards on `fetchIsSyncInProgress()`. The flag is only ever cleared by `SyncProgressIndicator.setInvisible()` or `SyncStatusBroadcastReceiver.complete(...)` (which fires only on a broadcast carrying `EXTRA_COMPLETE_STATUS=true`). The LocationTaskWorker → `LocationTaskIntentService.runLocationTaskSync()` chain never broadcasts a terminal complete status and the auth-failure branch broadcasts nothing at all, so `saveIsSyncInProgress(false)` is never called. `LocationTaskWorker.doWork()` also returns `Result.success()`/`Result.retry()` without clearing the flag. The flag stays `true` for the whole process lifetime (only reset by `RevealApplication.onCreate`), blocking all subsequent taps.
- **Defect C — `ExistingWorkPolicy.KEEP` jams re-triggers.** `enqueueImmediate` uses `enqueueUniqueWork(uniqueName, ExistingWorkPolicy.KEEP, request)`. Because a prior request is stuck enqueued-but-not-run under the same unique name (e.g. `"LocationTaskServiceJob"`), KEEP refuses to replace it, so new taps can never schedule a fresh run even if the flag were clear.

**Bug Condition C(X):** an immediate/manual sync is requested (Sync button tap or login-triggered sync) on Android 12–14 through `Utils.startImmediateSync()` → `RevealWorkScheduler.scheduleJobImmediately/enqueueImmediate`, where the request is scheduled as expedited-with-non-expedited-fallback under a unique name using `ExistingWorkPolicy.KEEP`, and the worker path never broadcasts a terminal complete status or clears the sync-in-progress flag.

**Non-buggy inputs ¬C(X):** periodic sync scheduling, worker-to-class tag mapping via `RevealWorkerRegistry`, and all other workers not on the immediate-sync path.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN the user taps the "Sync" button on Android 12–14 THEN the system enqueues expedited-with-non-expedited-fallback work that gets quota-denied and deferred with unsatisfied TIMING_DELAY/DEADLINE constraints, so `LocationTaskWorker.doWork()` never executes

1.2 WHEN login-triggered immediate sync runs on Android 12–14 THEN the system enqueues work that never becomes runnable, so no sync executes after login

1.3 WHEN an immediate sync is requested and `startImmediateSync()` sets the sync-in-progress flag to true THEN the system never clears the flag on the worker path because the LocationTaskWorker → `runLocationTaskSync()` chain never broadcasts `EXTRA_COMPLETE_STATUS=true` and `doWork()` returns without clearing it

1.4 WHEN the worker path hits the auth-failure branch in `onHandleIntent` THEN the system returns with no broadcast at all, leaving the sync-in-progress flag set to true

1.5 WHEN the user taps "Sync" again after a first tap THEN the system logs `startImmediateSync() skipped — sync already in progress` and returns early because the flag was never cleared, blocking all subsequent syncs for the process lifetime

1.6 WHEN a prior immediate request is stuck enqueued-but-not-run under the same unique name THEN the system uses `ExistingWorkPolicy.KEEP` and refuses to replace it, so a new tap cannot schedule a fresh run even if the flag were clear

1.7 WHEN a sync completes or fails on the worker path THEN the system does not broadcast a terminal complete status, so listeners such as `ListTasksActivity` and `BaseRegisterFragment` never update to reflect completion

### Expected Behavior (Correct)

2.1 WHEN the user taps the "Sync" button on Android 12–14 THEN the system SHALL schedule immediate work such that `LocationTaskWorker.doWork()` runs promptly rather than being deferred indefinitely by expedited-quota constraints

2.2 WHEN login-triggered immediate sync runs on Android 12–14 THEN the system SHALL execute the sync promptly after login

2.3 WHEN an immediate sync is requested and the sync-in-progress flag is set to true THEN the system SHALL reliably clear the flag to false on BOTH the success and failure paths of the worker

2.4 WHEN the worker path hits the auth-failure branch THEN the system SHALL clear the sync-in-progress flag and broadcast a terminal status so the app is not left blocked

2.5 WHEN the user taps "Sync" again after a prior sync has finished (successfully or with failure) THEN the system SHALL allow the new sync to proceed rather than skipping it as "already in progress"

2.6 WHEN a prior immediate request is stuck or already present under the same unique name THEN the system SHALL choose an existing-work policy for immediate/manual work that allows a fresh run to be scheduled (reconsider KEEP vs REPLACE for immediate work) so new manual syncs are not permanently blocked

2.7 WHEN a sync completes or fails on the worker path THEN the system SHALL broadcast a terminal complete status so listeners such as `ListTasksActivity` and `BaseRegisterFragment` update accordingly

### Unchanged Behavior (Regression Prevention)

3.1 WHEN periodic sync work is scheduled via `enqueuePeriodic`/`scheduleJob` THEN the system SHALL CONTINUE TO schedule periodic unique work with `ExistingPeriodicWorkPolicy.KEEP` and the configured interval/flex, unaffected by the immediate-sync fix

3.2 WHEN any work tag is resolved to a worker class THEN the system SHALL CONTINUE TO map tags to worker classes correctly via `RevealWorkerRegistry` (this is not a defect and must not change)

3.3 WHEN background work is scheduled or executed THEN the system SHALL CONTINUE TO use `androidx.work` (WorkManager) and SHALL NOT reintroduce `com.evernote:android-job` or background `startService()`/IntentService startup

3.4 WHEN workers other than the immediate-sync path (Sync, Hdss, ExtendedSync, Validate, SyncSettings, and any others) are enqueued or run THEN the system SHALL CONTINUE TO behave as they do today, except where they participate in the shared sync-in-progress flag / terminal-broadcast contract being fixed

3.5 WHEN WorkManager is initialized THEN the system SHALL CONTINUE TO initialize with its default configuration (initialization is already correct and must not change)

3.6 WHEN a network constraint is required for a work request THEN the system SHALL CONTINUE TO apply `NetworkType.CONNECTED` constraints as it does today

## Bug Condition and Properties

### Bug Condition Function

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type SyncRequest
  OUTPUT: boolean

  // X is an immediate/manual sync request (Sync button tap or login-triggered)
  // scheduled through Utils.startImmediateSync() -> RevealWorkScheduler.enqueueImmediate
  RETURN X.isImmediateSync = true
     AND X.androidApiLevel >= 31            // Android 12+
     AND ( X.scheduledExpeditedWithNonExpeditedFallback = true   // Defect A
        OR X.syncInProgressFlagClearedByWorkerPath = false        // Defect B
        OR X.existingWorkPolicy = KEEP )                          // Defect C
END FUNCTION
```

### Property: Fix Checking

```pascal
// Property: immediate/manual sync actually executes and self-heals its state
FOR ALL X WHERE isBugCondition(X) DO
  result ← startImmediateSync'(X)   // fixed path

  ASSERT workerDidWork(result) = true                       // doWork() runs promptly (2.1, 2.2)
  ASSERT syncInProgressFlagClearedOn(result, SUCCESS) = true   // (2.3)
  ASSERT syncInProgressFlagClearedOn(result, FAILURE) = true   // (2.3, 2.4)
  ASSERT terminalCompleteStatusBroadcast(result) = true        // (2.4, 2.7)
  ASSERT subsequentSyncNotBlocked(result) = true               // (2.5)
  ASSERT stuckPriorRequestDoesNotBlockNewRun(result) = true    // (2.6)
END FOR
```

### Property: Preservation Checking

```pascal
// Property: for all non-immediate-sync inputs, fixed code behaves identically to original
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT F(X) = F'(X)
  // Covers: periodic scheduling (3.1), tag->worker mapping (3.2),
  // WorkManager-only scheduling with no IntentService/android-job (3.3),
  // other workers (3.4), WorkManager init (3.5), network constraints (3.6)
END FOR
```

Where **F** is the original (unfixed) code and **F'** is the fixed code.
