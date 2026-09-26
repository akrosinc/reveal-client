# Implementation Plan — WorkManager migration

## Overview

Remove the legacy `com.evernote:android-job` scheduling path and the
`Job → startService(IntentService)` execution path from reveal-client, replacing
both with `androidx.work` (WorkManager). See `requirements.md` (R1–R8) and
`design.md` in this folder for full context.

Execution order respects dependencies: scheduler infra first, then the crash-path
jobs (sync/immediate), then remaining jobs, then wiring cleanup and dependency
removal. Build (`:opensrp-reveal:assembleDebug`) after each numbered task.

## Tasks

- [x] 1. Add `RevealWorkScheduler` infrastructure
  - Create `org.smartregister.reveal.job.RevealWorkScheduler` with
    `enqueueImmediate(...)`, `enqueuePeriodic(...)`, `cancelAll()` using WorkManager.
  - Include a network `Constraints` helper.
  - _Requirements: R2, R5_

- [x] 2. Introduce `BaseWorker` + migrate `BaseJob` callers' entry points
  - Create `org.smartregister.job.BaseWorker` (extends `androidx.work.Worker`) with
    shared helpers (context access, reschedule-flag handling no longer needed).
  - Keep `BaseJob` temporarily as a thin shim delegating `scheduleJob*` to
    `RevealWorkScheduler` so existing callers compile during migration.
  - _Requirements: R2, R3_

- [x] 3. Migrate crash-path jobs first: SyncServiceJob, ExtendedSyncServiceJob,
        ValidateSyncDataServiceJob
  - Create `SyncWorker`, `ExtendedSyncWorker`, `ValidateSyncDataWorker`.
  - Move or invoke the corresponding IntentService logic in `doWork()`
    (ExtendedSync: `ActionService.fetchNewActions()` + trigger validate worker).
  - Update `Utils.startImmediateSync` / `ExtendedSyncServiceJob.scheduleJobImmediately`
    call sites to the new scheduler.
  - _Requirements: R2, R3, R8_

- [x] 4. Migrate reveal-module jobs: LocationTaskServiceJob,
        RevealSyncSettingsServiceJob
  - Create `LocationTaskWorker`, `RevealSyncSettingsWorker`; port IntentService bodies.
  - _Requirements: R3_

- [x] 5. Migrate remaining common jobs: DocumentConfigurationServiceJob,
        HdssServiceJob, ImageUploadServiceJob, LocationStructureServiceJob,
        P2pServiceJob, PlanIntentServiceJob, SyncSettingsServiceJob, SyncTaskServiceJob
  - One Worker per job; port each IntentService body; wire scheduling calls.
  - _Requirements: R3_

- [ ] 6. Replace `RevealJobCreator` and RevealApplication wiring
  - Remove `RevealJobCreator`; remove
    `JobManager.create(this).addJobCreator(...)` from RevealApplication.
  - _Requirements: R4, R5_

- [ ] 7. Update `ResetAppHelper`
  - `JobManager.create(application).cancelAll()` → `WorkManager.cancelAllWork()`.
  - _Requirements: R5_

- [ ] 8. Remove `BaseJob` shim and all android-job imports
  - Delete `BaseJob`, all `com.evernote.android.job.*` imports, and the 15 old
    `*ServiceJob` classes once Workers replace them.
  - _Requirements: R1_

- [ ] 9. Remove the `com.evernote:android-job` dependency (all 3 declarations)
  - _Requirements: R1_

- [ ] 10. PendingIntent mutability audit
  - Grep app for `PendingIntent.get*` without a flag; add FLAG_IMMUTABLE
    (FLAG_MUTABLE only where mutation is required).
  - _Requirements: R6_

- [ ] 11. Build + launch verification
  - `:opensrp-reveal:assembleDebug` green; install; confirm no PendingIntent crash;
    confirm immediate + periodic sync scheduling via logs.
  - _Requirements: R7, R8_

## Notes

- Task ordering follows the dependency chain: scheduler infra (1) → BaseWorker (2) →
  crash-path jobs (3) → remaining jobs (4–5) → wiring cleanup (6–7) → removal of
  shim/dependency (8–9) → PendingIntent audit (10) → build/launch verification (11).
- Each numbered task references the specific requirements clause(s) it satisfies for
  traceability against `requirements.md` (R1–R8).
- Build `:opensrp-reveal:assembleDebug` after each numbered task to catch regressions early.
- The `BaseJob` shim is intentionally retained through tasks 2–7 so existing callers
  compile during migration, and is removed only in task 8.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1"] },
    { "id": 1, "tasks": ["2"] },
    { "id": 2, "tasks": ["3"] },
    { "id": 3, "tasks": ["4", "5"] },
    { "id": 4, "tasks": ["6", "7"] },
    { "id": 5, "tasks": ["8", "9"] },
    { "id": 6, "tasks": ["10"] },
    { "id": 7, "tasks": ["11"] }
  ]
}
```
