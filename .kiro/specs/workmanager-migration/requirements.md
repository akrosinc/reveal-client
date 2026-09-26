# Requirements — Migrate background jobs off android-job to WorkManager

## Context

reveal-client schedules background work through the deprecated, unmaintained
`com.evernote:android-job:1.2.6`. Targeting SDK 34 exposes two blocking defects:

1. android-job's internal `TransientBundleCompat` creates a `PendingIntent`
   without `FLAG_IMMUTABLE`, which throws `IllegalArgumentException` on API 31+
   (crash observed in `JobManager.schedule` during `Utils.startImmediateSync`).
2. Each `*Job` launches a legacy `IntentService` via background `startService()`,
   which is disallowed for background apps on API 26+/31+.

Primary objective: **remove all legacy** — replace both android-job and the
IntentService indirection with `androidx.work` (WorkManager), which is already
on the classpath.

## Inventory (15 job classes + BaseJob + JobCreator)

Common module `org.smartregister.job`:
- BaseJob (scheduling helpers: scheduleJob periodic, scheduleJobImmediately)
- SyncServiceJob → RevealSyncIntentService (via ctor)
- ExtendedSyncServiceJob → ExtendedSyncIntentService
- DocumentConfigurationServiceJob → DocumentConfigurationIntentService (via ctor)
- HdssServiceJob → HdssSyncIntentService
- ImageUploadServiceJob → (image upload IntentService)
- LocationStructureServiceJob → LocationIntentService
- P2pServiceJob → (p2p IntentService)
- PlanIntentServiceJob → PlanIntentService
- SyncSettingsServiceJob → SettingsSyncIntentService
- SyncTaskServiceJob → (sync task IntentService)
- ValidateSyncDataServiceJob → ValidateIntentService

Reveal module `org.smartregister.reveal.job`:
- LocationTaskServiceJob → LocationTaskIntentService
- RevealSyncSettingsServiceJob → RevealSettingsSyncIntentService
- RevealJobCreator (maps TAG → Job instance)

Wiring:
- RevealApplication.onCreate → JobManager.create(this).addJobCreator(new RevealJobCreator())
- ResetAppHelper → JobManager.create(application).cancelAll()

## Requirements

R1. Remove the `com.evernote:android-job` dependency entirely (all 3 declarations).

R2. Replace `BaseJob` scheduling API with WorkManager equivalents:
    - `scheduleJob(tag, start, flex)` → unique PeriodicWorkRequest (min interval 15m).
    - `scheduleJobImmediately(tag)` → unique OneTimeWorkRequest (expedited where allowed).
    - Preserve the existing "skip if already scheduled" semantics via
      ExistingPeriodicWorkPolicy.KEEP / ExistingWorkPolicy.KEEP as appropriate.

R3. Each job's work must run **inside a Worker** (no background startService,
    no IntentService). The logic currently in each `*IntentService` must be
    invoked directly from the corresponding Worker's doWork().

R4. Replace `RevealJobCreator` (android-job's tag→Job factory) with WorkManager
    Worker classes; remove the creator once obsolete.

R5. Update `RevealApplication` and `ResetAppHelper` to WorkManager
    initialization and `WorkManager.cancelAllWork()` / cancel-by-tag.

R6. `PendingIntent` usages anywhere else in app code must specify
    FLAG_IMMUTABLE/FLAG_MUTABLE (audit as part of this work).

R7. Behavior verification (android-job vs WorkManager differ):
    - periodic sync cadence still fires (allowing WorkManager 15m floor + flex),
    - immediate sync runs promptly,
    - reschedule-on-boot still works,
    - no duplicate scheduling,
    - jobs survive process death/reboot.

R8. App builds (assembleDebug) and launches past startup without the
    PendingIntent crash.

## Out of scope
- Migrating IntentServices used outside the job system (if any) — only those
  reachable from the 15 jobs.
- Changing sync business logic; only the scheduling/execution mechanism changes.
