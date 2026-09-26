# Sync Never Runs (WorkManager Regression) Bugfix Design

## Overview

After migrating reveal-client's background scheduling from `com.evernote:android-job` to
`androidx.work` (WorkManager), backend sync never actually executes. On a fresh login and on
every manual "Sync" tap, work is enqueued but never runs. On-device evidence (Samsung SM-X205,
Android 14) shows `startImmediateSync() proceeding — flag set true` is logged, but
`LocationTaskWorker.doWork() started` never follows; a second tap logs
`startImmediateSync() skipped — sync already in progress`, and `dumpsys jobscheduler` shows the
enqueued jobs stuck with `Unsatisfied constraints: TIMING_DELAY DEADLINE`. WorkManager itself
initializes correctly, so initialization is not implicated.

This is a single regression caused by three interacting defects on the immediate/manual sync path:

- **Defect A — Expedited scheduling starvation.** `RevealWorkScheduler.enqueueImmediate(...)`
  unconditionally calls `.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)`. On
  Android 12+ (API 31+), once expedited quota is exhausted by the long reveal sync chain
  (LocationTask → Sync → Hdss → ExtendedSync → Validate → SyncSettings), the non-expedited
  fallback acquires `TIMING_DELAY`/`DEADLINE` constraints and effectively never becomes runnable.
- **Defect B — Sync-in-progress flag never cleared.** `Utils.startImmediateSync()` sets
  `saveIsSyncInProgress(true)` and guards on `fetchIsSyncInProgress()`. The flag is only cleared
  on a terminal broadcast carrying `EXTRA_COMPLETE_STATUS=true`, which the
  `LocationTaskWorker → LocationTaskIntentService.runLocationTaskSync()` chain never sends (and the
  auth-failure branch sends nothing). `LocationTaskWorker.doWork()` returns
  `Result.success()`/`Result.retry()` without clearing the flag, so it stays `true` for the whole
  process lifetime and blocks every subsequent tap.
- **Defect C — `ExistingWorkPolicy.KEEP` jams re-triggers.** `enqueueImmediate` uses
  `enqueueUniqueWork(uniqueName, ExistingWorkPolicy.KEEP, request)`. A prior request stuck
  enqueued-but-not-run under the same unique name (e.g. `"LocationTaskServiceJob"`) causes KEEP to
  refuse replacement, so new taps cannot schedule a fresh run even if the flag were clear.

The fix approach is targeted and minimal: change immediate-sync scheduling so it becomes runnable
(Defect A), guarantee the sync-in-progress flag is cleared and a terminal status is broadcast on
both success and failure worker paths (Defect B), and choose an existing-work policy for
immediate/manual work that allows a fresh run (Defect C). All periodic scheduling, tag→worker
mapping, WorkManager initialization, and non-immediate workers are preserved unchanged.

## Glossary

- **Bug_Condition (C)**: An immediate/manual sync request (Sync button tap or login-triggered
  sync) on Android 12–14 routed through `Utils.startImmediateSync()` →
  `RevealWorkScheduler.scheduleJobImmediately/enqueueImmediate`, scheduled as
  expedited-with-non-expedited-fallback under a unique name using `ExistingWorkPolicy.KEEP`, where
  the worker path never broadcasts a terminal complete status or clears the sync-in-progress flag.
- **Property (P)**: For a bug-condition input, the fixed path SHALL run the worker promptly, clear
  the sync-in-progress flag on both success and failure, broadcast a terminal complete status, and
  not permanently block subsequent syncs.
- **Preservation**: Behavior that must remain unchanged — periodic scheduling, tag→worker mapping
  via `RevealWorkerRegistry`, WorkManager-only scheduling (no android-job / background
  `startService()`), WorkManager initialization, network constraints, and non-immediate workers.
- **`RevealWorkScheduler.enqueueImmediate`**: The scheduler method in
  `opensrp-reveal/.../reveal/job/RevealWorkScheduler.java` that builds and enqueues one-time unique
  work for immediate sync. Source of Defect A and Defect C.
- **`Utils.startImmediateSync`**: The entry point in `opensrp-reveal/.../reveal/util/Utils.java`
  that guards on and sets the sync-in-progress flag before scheduling. Source of the guard for
  Defect B.
- **`LocationTaskWorker.doWork`**: The worker in `opensrp-reveal/.../reveal/job/LocationTaskWorker.java`
  that invokes `LocationTaskIntentService.runLocationTaskSync(...)`. Returns without clearing the
  flag or broadcasting terminal status — Defect B.
- **Sync-in-progress flag**: `AllSharedPreferences.saveIsSyncInProgress(boolean)` /
  `fetchIsSyncInProgress()`, cleared today only via `SyncProgressIndicator.setInvisible()` or
  `SyncStatusBroadcastReceiver.complete(...)`.
- **`RevealWorkerRegistry`**: Maps a work tag (legacy job TAG) to its worker class. Not a defect;
  must not change.

## Bug Details

### Bug Condition

The bug manifests when an immediate/manual sync is requested on Android 12–14 through
`Utils.startImmediateSync()` → `RevealWorkScheduler.scheduleJobImmediately/enqueueImmediate`. The
scheduling path is defective in three ways that each independently prevent sync from running or
from ever running again: expedited-with-non-expedited-fallback scheduling that gets quota-denied
and deferred (Defect A), a sync-in-progress flag that the worker path never clears (Defect B), and
`ExistingWorkPolicy.KEEP` that refuses to replace a stuck prior request (Defect C).

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type SyncRequest
  OUTPUT: boolean

  // input is an immediate/manual sync request (Sync tap or login-triggered)
  // routed through Utils.startImmediateSync() -> RevealWorkScheduler.enqueueImmediate
  RETURN input.isImmediateSync = true
     AND input.androidApiLevel >= 31                                  // Android 12+
     AND ( input.scheduledExpeditedWithNonExpeditedFallback = true    // Defect A
        OR input.syncInProgressFlagClearedByWorkerPath = false        // Defect B
        OR input.existingWorkPolicy = KEEP )                          // Defect C
END FUNCTION
```

### Examples

- **Manual Sync tap (Defect A):** On Android 14, tapping Sync logs
  `startImmediateSync() proceeding — flag set true`, but `LocationTaskWorker.doWork() started`
  never appears; `dumpsys jobscheduler` shows the job stuck with
  `Unsatisfied constraints: TIMING_DELAY DEADLINE`. *Expected:* `doWork()` runs promptly.
- **Second Sync tap (Defect B):** After the first tap, a second tap logs
  `startImmediateSync() skipped — sync already in progress` and returns early because the flag was
  never cleared. *Expected:* the new sync proceeds.
- **Auth-failure branch (Defect B):** The worker path hits the auth-failure branch and broadcasts
  nothing, leaving the flag `true`. *Expected:* flag cleared and a terminal status broadcast.
- **Stuck prior request (Defect C):** A prior request stuck enqueued-but-not-run under
  `"LocationTaskServiceJob"` causes `ExistingWorkPolicy.KEEP` to refuse a fresh run on the next
  tap. *Expected:* the existing-work policy for immediate work permits a fresh run.
- **Login-triggered sync (Defects A/B):** After a fresh login, immediate sync is enqueued but
  never becomes runnable, so no sync executes. *Expected:* sync executes promptly after login.

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Periodic sync scheduling via `enqueuePeriodic`/`scheduleJob` must continue to enqueue periodic
  unique work with `ExistingPeriodicWorkPolicy.KEEP` and the configured interval/flex (3.1).
- Tag→worker-class resolution via `RevealWorkerRegistry` must continue to map correctly (3.2).
- Background work must continue to use `androidx.work` only, with no reintroduction of
  `com.evernote:android-job` or background `startService()`/IntentService startup (3.3).
- Workers other than the immediate-sync path (Sync, Hdss, ExtendedSync, Validate, SyncSettings, and
  any others) must continue to behave as today, except where they participate in the shared
  sync-in-progress-flag / terminal-broadcast contract being fixed (3.4).
- WorkManager must continue to initialize with its default configuration (3.5).
- Network-constrained requests must continue to apply `NetworkType.CONNECTED` (3.6).

**Scope:**
All inputs that do NOT satisfy the bug condition (non-immediate-sync inputs) should be completely
unaffected by this fix. This includes:
- Periodic scheduling requests.
- Tag→worker mapping lookups.
- Any worker not on the immediate-sync path (except for the shared flag/broadcast contract).

**Note:** The actual expected correct behavior for bug-condition inputs is defined in the
Correctness Properties section (Property 1). This section focuses on what must NOT change.

## Hypothesized Root Cause

Based on the bug description and confirmed on-device evidence, the causes are:

1. **Expedited-quota starvation (Defect A) — most likely primary cause.** In
   `RevealWorkScheduler.enqueueImmediate`, the builder always sets
   `.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)`. On Android 12+, exhausted
   expedited quota causes the non-expedited fallback to sit with unsatisfied `TIMING_DELAY`/
   `DEADLINE` constraints, matching the `dumpsys jobscheduler` output. The long sync chain quickly
   exhausts quota.

2. **Sync-in-progress flag never cleared on the worker path (Defect B).** `Utils.startImmediateSync`
   sets the flag `true` and guards subsequent calls on it. `LocationTaskWorker.doWork()` returns
   `Result.success()`/`Result.retry()` without clearing it, and the
   `LocationTaskIntentService.runLocationTaskSync()` chain never broadcasts
   `EXTRA_COMPLETE_STATUS=true` (the only signal that clears the flag). The auth-failure branch
   broadcasts nothing. The flag remains `true` for the process lifetime, blocking all later taps.

3. **`ExistingWorkPolicy.KEEP` for immediate work (Defect C).** `enqueueUniqueWork(uniqueName, KEEP, request)`
   refuses to replace a stuck prior request under the same unique name, so even a clean flag cannot
   produce a fresh run.

4. **Missing terminal broadcast for listeners.** Because no terminal complete status is broadcast,
   UI listeners such as `ListTasksActivity` and `BaseRegisterFragment` never update to reflect
   completion — a downstream symptom of Defect B.

## Correctness Properties

Property 1: Bug Condition - Immediate/Manual Sync Runs and Self-Heals Its State

_For any_ input where the bug condition holds (`isBugCondition` returns true), the fixed immediate
sync path SHALL schedule work that runs `LocationTaskWorker.doWork()` promptly rather than being
deferred indefinitely by expedited-quota constraints; SHALL clear the sync-in-progress flag to
false on BOTH the success and failure worker paths; SHALL broadcast a terminal complete status so
UI listeners update; SHALL allow a subsequent sync to proceed rather than being skipped as "already
in progress"; and SHALL choose an existing-work policy for immediate/manual work such that a stuck
prior request does not permanently block a fresh run.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7**

Property 2: Preservation - Non-Immediate-Sync Behavior Unchanged

_For any_ input where the bug condition does NOT hold (`isBugCondition` returns false), the fixed
code SHALL produce the same result as the original code, preserving periodic scheduling with
`ExistingPeriodicWorkPolicy.KEEP` and configured interval/flex, correct tag→worker mapping via
`RevealWorkerRegistry`, WorkManager-only scheduling with no android-job/IntentService startup,
existing behavior of non-immediate workers, default WorkManager initialization, and
`NetworkType.CONNECTED` network constraints.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**

## Fix Implementation

### Changes Required

Assuming the root cause analysis is correct, the fix touches three collaborating locations.

**File**: `opensrp-reveal/src/main/java/org/smartregister/reveal/job/RevealWorkScheduler.java`

**Function**: `enqueueImmediate(...)`

**Specific Changes**:
1. **Defect A — make immediate work runnable.** Stop unconditionally forcing
   `setExpedited(RUN_AS_NON_EXPEDITED_WORK_REQUEST)` on the immediate-sync chain. Options in
   preference order: (a) drop `setExpedited` for these one-time requests so they schedule as
   normal-priority runnable work; or (b) keep expedited only for the single user-visible trigger
   and never for the chained downstream jobs. Chosen approach: remove the forced expedited fallback
   for the immediate-sync path so requests do not acquire `TIMING_DELAY`/`DEADLINE` constraints.
2. **Defect C — allow a fresh immediate run.** Change the immediate-work policy from
   `ExistingWorkPolicy.KEEP` to `ExistingWorkPolicy.REPLACE` for immediate/manual work so a stuck
   prior request under the same unique name is replaced by the new tap. (Periodic scheduling keeps
   `ExistingPeriodicWorkPolicy.KEEP` — see Preservation.)

**File**: `opensrp-reveal/src/main/java/org/smartregister/reveal/job/LocationTaskWorker.java`
(and, if the shared contract is centralized, `org/smartregister/job/BaseWorker.java`)

**Function**: `doWork()`

**Specific Changes**:
3. **Defect B — clear flag on success.** Before returning `Result.success()`, clear the
   sync-in-progress flag (`AllSharedPreferences.saveIsSyncInProgress(false)`) and broadcast a
   terminal complete status (`EXTRA_COMPLETE_STATUS=true`) so listeners update.
4. **Defect B — clear flag on failure/exception.** In the `catch` branch (and any terminal
   `Result.failure()`/`Result.retry()` that ends the immediate attempt), clear the flag and
   broadcast a terminal status so the app is not left blocked.
5. **Defect B — auth-failure branch.** Ensure the auth-failure path (in the
   `runLocationTaskSync`/`onHandleIntent` logic) clears the flag and broadcasts a terminal status
   instead of returning silently.

**Centralization note (optional but recommended)**:
6. **Shared contract in `BaseWorker`.** Consider a `finally`-style guarantee (e.g. a helper on
   `BaseWorker` such as `finishSync(success)` that clears the flag and broadcasts terminal status)
   so every worker on the immediate-sync path clears the flag exactly once regardless of outcome,
   preventing the flag from ever being stranded `true`.

## Testing Strategy

### Validation Approach

The strategy is two-phase: first surface counterexamples that demonstrate the bug on the UNFIXED
code, confirming (or refuting) the root cause; then verify the fix makes immediate sync run and
self-heal, and that all non-immediate-sync behavior is preserved.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or
refute the root-cause analysis for Defects A, B, and C. If refuted, re-hypothesize.

**Test Plan**: Simulate immediate/manual sync on an Android 12+ context and observe (via
WorkManager test harness / `WorkManagerTestInitHelper` and shared-prefs assertions) whether the
worker runs, whether the flag is cleared, whether a terminal status is broadcast, and whether a
stuck unique-name request blocks a new one. Run against the UNFIXED code to observe failures.

**Test Cases**:
1. **Expedited starvation (Defect A)**: Enqueue the immediate chain and assert
   `LocationTaskWorker.doWork()` becomes runnable/executes (will fail on unfixed code — deferred by
   `TIMING_DELAY`/`DEADLINE`).
2. **Flag never cleared on success (Defect B)**: Run the worker to success and assert
   `fetchIsSyncInProgress()` is false afterward (will fail on unfixed code).
3. **Flag never cleared on failure/auth-failure (Defect B)**: Force the failure/auth-failure branch
   and assert the flag is cleared and a terminal status broadcast (will fail on unfixed code).
4. **Second tap blocked (Defect B)**: After a first sync "completes", assert a second
   `startImmediateSync()` proceeds rather than logging "skipped — sync already in progress" (will
   fail on unfixed code).
5. **KEEP jams re-trigger (Defect C)**: With a stuck request under the same unique name, enqueue a
   new immediate request and assert a fresh run is scheduled (will fail on unfixed code with KEEP).

**Expected Counterexamples**:
- `doWork()` never runs; `dumpsys jobscheduler` shows `Unsatisfied constraints: TIMING_DELAY DEADLINE`.
- `fetchIsSyncInProgress()` stays `true` after success and after failure.
- Second `startImmediateSync()` returns early ("already in progress").
- Possible causes: forced expedited-fallback scheduling, worker returns without clearing the flag /
  no terminal broadcast, `ExistingWorkPolicy.KEEP` refusing replacement.

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed path produces the
expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := startImmediateSync_fixed(input)
  ASSERT workerDidWork(result) = true                          // doWork() runs promptly (2.1, 2.2)
  ASSERT syncInProgressFlagClearedOn(result, SUCCESS) = true   // (2.3)
  ASSERT syncInProgressFlagClearedOn(result, FAILURE) = true   // (2.3, 2.4)
  ASSERT terminalCompleteStatusBroadcast(result) = true        // (2.4, 2.7)
  ASSERT subsequentSyncNotBlocked(result) = true               // (2.5)
  ASSERT stuckPriorRequestDoesNotBlockNewRun(result) = true    // (2.6)
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed code produces
the same result as the original code.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT startImmediateSync_original(input) = startImmediateSync_fixed(input)
  // Covers: periodic scheduling (3.1), tag->worker mapping (3.2),
  // WorkManager-only scheduling with no IntentService/android-job (3.3),
  // other workers (3.4), WorkManager init (3.5), network constraints (3.6)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain (varied tags, intervals,
  flex, network flags, worker classes).
- It catches edge cases that manual unit tests might miss.
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs.

**Test Plan**: Observe behavior on UNFIXED code first for periodic scheduling, tag mapping, and
network constraints, then write property-based tests that capture and lock in that behavior.

**Test Cases**:
1. **Periodic scheduling preservation**: Observe `enqueuePeriodic`/`scheduleJob` enqueues periodic
   unique work with `ExistingPeriodicWorkPolicy.KEEP` and the configured interval/flex on unfixed
   code; verify it is identical after the fix (3.1).
2. **Tag→worker mapping preservation**: Observe `RevealWorkerRegistry.workerForTag(tag)` mappings on
   unfixed code; verify unchanged after the fix (3.2).
3. **WorkManager-only preservation**: Verify no `com.evernote:android-job` or background
   `startService()`/IntentService startup is reintroduced (3.3).
4. **Network-constraint preservation**: Verify `NetworkType.CONNECTED` is still applied when
   `requireNetwork` is set (3.6).

### Unit Tests

- `enqueueImmediate` no longer forces expedited-with-non-expedited fallback on the immediate-sync
  chain (Defect A).
- `enqueueImmediate` uses an existing-work policy that permits a fresh immediate run (Defect C).
- `LocationTaskWorker.doWork()` clears the sync-in-progress flag and broadcasts a terminal status on
  both success and exception/failure paths (Defect B).
- Auth-failure branch clears the flag and broadcasts a terminal status (Defect B).
- `enqueuePeriodic` still uses `ExistingPeriodicWorkPolicy.KEEP` (preservation).

### Property-Based Tests

- Generate immediate-sync requests across API levels ≥ 31 and worker outcomes (success, failure,
  auth-failure) and assert the flag is always cleared and a terminal status broadcast, and that a
  subsequent request is never permanently blocked (Property 1).
- Generate arbitrary non-immediate inputs (periodic tags/intervals/flex, tag lookups, network
  flags) and assert `F(X) == F'(X)` for scheduling shape and policy (Property 2 / preservation).

### Integration Tests

- Full manual-sync flow on an Android 12+ context: tap Sync → worker runs → flag cleared →
  terminal broadcast → UI listeners update; a second tap runs again.
- Login-triggered sync flow: sync executes promptly after login.
- Context/flow with a previously stuck unique-name request: a new tap schedules and runs a fresh
  sync.
