# Design — WorkManager migration (remove android-job + IntentService job path)

## Goal
Replace `com.evernote:android-job` scheduling and the `Job → startService(IntentService)`
execution path with `androidx.work` (WorkManager). No android-job, no background
`startService`, no reliance on `IntentService` for scheduled work.

## Current architecture (to be replaced)

```
Utils.startImmediateSync()
  -> BaseJob.scheduleJobImmediately(TAG)          // android-job JobRequest.startNow()
       -> JobManager.schedule()                    // CRASH: PendingIntent w/o FLAG_IMMUTABLE
            -> RevealJobCreator.create(TAG) -> XxxServiceJob (android-job Job)
                 -> onRunJob(): startService(XxxIntentService)   // background startService (illegal)
                      -> XxxIntentService.onHandleIntent()        // real work (some in core base)
```

Periodic scheduling: `BaseJob.scheduleJob(tag, start, flex)` builds a periodic
`JobRequest`; android-job floors periodic at 15 minutes (existing workaround flag
`TO_RESCHEDULE` marks sub-15m jobs so `onRunJob` returns RESCHEDULE).

## Target architecture

```
Utils.startImmediateSync()
  -> RevealWorkScheduler.enqueueImmediate(TAG)
       -> WorkManager.enqueueUniqueWork(TAG, KEEP, OneTimeWorkRequest<XxxWorker>)
            -> XxxWorker.doWork(): performs the work synchronously, returns Result
```

Periodic: `RevealWorkScheduler.enqueuePeriodic(TAG, intervalMin, flexMin)`
  -> `WorkManager.enqueueUniquePeriodicWork(TAG, KEEP, PeriodicWorkRequest<XxxWorker>)`.

### Components

1. **`RevealWorkScheduler`** (new, replaces `BaseJob` static scheduling API)
   - `enqueueImmediate(String uniqueName, Class<? extends ListenableWorker> worker)`
     -> OneTimeWorkRequest, `setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)`
        where safe, `ExistingWorkPolicy.KEEP`.
   - `enqueuePeriodic(String uniqueName, Class<? extends ListenableWorker> worker,
     long intervalMinutes, long flexMinutes)`
     -> PeriodicWorkRequest (WorkManager min interval 15m; values < 15 are clamped
        by WorkManager, matching prior android-job behavior), `ExistingPeriodicWorkPolicy.KEEP`.
   - `cancelAll()` -> `WorkManager.getInstance(context).cancelAllWork()`.
   - Constraints: network-required workers set `NetworkType.CONNECTED`.

2. **One `Worker` per current job** (`androidx.work.Worker`, since work is synchronous
   blocking I/O; WorkManager runs `doWork()` on a background thread).
   Naming: `XxxWorker` replacing `XxxServiceJob`.

   `doWork()` body strategy per job:
   - **Logic fully in-app IntentService** (e.g. LocationTaskIntentService): move the
     body of `onHandleIntent` into the Worker (or a shared helper) and call it directly.
   - **IntentService delegates to core `BaseSyncIntentService.onHandleIntent(super)`**
     (e.g. ExtendedSyncIntentService, SyncIntentService): the core base class contains
     the actual sync loop. We cannot edit core here. Strategy: the Worker instantiates
     and drives the sync through the same public entry points the base service used
     (e.g. `CoreLibrary...`, `ActionService.fetchNewActions()`), replicating only the
     small in-app orchestration that lived in the app subclass. Where the base class's
     work is only reachable via the service, we retain a thin invocation but run it on
     the Worker thread (never via background startService). Each such case is called
     out in tasks with its exact entry point.

3. **Worker registry** (replaces `RevealJobCreator`)
   - WorkManager instantiates workers by class, so the TAG→Job factory is not needed.
   - Keep the `TAG` constants (used as unique work names) on the Worker classes for
     compatibility with existing callers.

4. **RevealApplication**
   - Remove `JobManager.create(this).addJobCreator(new RevealJobCreator())`.
   - WorkManager self-initializes via its manifest `androidx.startup` provider; no
     explicit init needed unless a custom `Configuration` is required (it isn't).

5. **ResetAppHelper**
   - Replace `JobManager.create(application).cancelAll()` with
     `WorkManager.getInstance(application).cancelAllWork()`.

## Scheduling semantics mapping

| android-job | WorkManager | Notes |
|---|---|---|
| `JobRequest.Builder(tag).startNow().schedule()` | `enqueueUniqueWork(tag, KEEP, OneTimeWorkRequest)` | expedited when allowed |
| periodic `setPeriodic(start, flex)` | `PeriodicWorkRequest(interval, flex)` | both floor at 15m |
| `getAllJobRequestsForTag(tag).isEmpty()` guard | `ExistingWorkPolicy.KEEP` / `ExistingPeriodicWorkPolicy.KEEP` | dedup handled by policy |
| `Result.RESCHEDULE` (sub-15m workaround) | not needed | periodic handles re-run |
| `JobManager.cancelAll()` | `WorkManager.cancelAllWork()` | |

## PendingIntent audit (R6)
Grep app code for `PendingIntent.get{Activity,Service,Broadcast}` lacking a
mutability flag; add `FLAG_IMMUTABLE` (or `FLAG_MUTABLE` only where the intent is
mutated, e.g. notifications with inline reply). Covered as a dedicated task.

## Dependencies
- `androidx.work:work-runtime:2.7.1` is already declared. Keep (compatible with
  compileSdk 34 / minSdk 26). Remove all `com.evernote:android-job:1.2.6` lines.

## Verification (R7/R8)
- `:opensrp-reveal:assembleDebug` green.
- App launches past startup (no PendingIntent crash from scheduling).
- Manual/log verification: immediate sync enqueues + runs; periodic sync registers;
  `WorkManager` unique-work dedup prevents duplicates; cancelAllWork on reset.

## Risks
- Expedited one-time work is quota-limited on API 31+; if immediate sync must be
  truly immediate, fall back to a foreground service only if strictly required
  (avoid; prefer expedited + non-expedited fallback).
- Core `BaseSyncIntentService` internals not editable here; per-job entry points
  documented in tasks. If a job's work is not reachable without the service, flag it
  during that task rather than introduce a background service.
