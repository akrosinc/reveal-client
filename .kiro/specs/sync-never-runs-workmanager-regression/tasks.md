# Implementation Plan

## Overview

This plan fixes a single WorkManager-migration regression where immediate/manual sync never runs on
Android 12–14. It follows the exploratory bugfix workflow: write a bug-condition exploration test
and preservation tests BEFORE the fix, apply the three-part fix (Defects A, B, C), then re-run both
test sets to confirm the bug is fixed and nothing regressed.

## Task Dependency Graph

Tasks 1 and 2 run first (on unfixed code) with no dependencies. The fix in Task 3.1 and 3.2 depends
on both. Verification sub-tasks 3.3 and 3.4 depend on the fix sub-tasks, and the checkpoint depends
on all fix work.

```json
{
  "waves": [
    {
      "wave": 1,
      "tasks": ["1", "2"],
      "dependsOn": []
    },
    {
      "wave": 2,
      "tasks": ["3.1", "3.2"],
      "dependsOn": ["1", "2"]
    },
    {
      "wave": 3,
      "tasks": ["3.3", "3.4"],
      "dependsOn": ["3.1", "3.2"]
    },
    {
      "wave": 4,
      "tasks": ["4"],
      "dependsOn": ["3.3", "3.4"]
    }
  ]
}
```

## Tasks

- [ ] 1. Write bug condition exploration test (BEFORE implementing the fix)
  - **Property 1: Bug Condition** - Immediate/Manual Sync Runs and Self-Heals Its State
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails** - the failure is the expected, correct outcome at this stage
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate Defects A, B, and C exist, confirming (or refuting) the root-cause analysis
  - **Scoped PBT Approach**: These are deterministic scheduling/state defects. Scope the property to concrete failing cases (immediate/manual sync request, `androidApiLevel >= 31`) and vary worker outcome across {success, failure, auth-failure} rather than generating unrelated inputs
  - Set up a WorkManager test harness (`WorkManagerTestInitHelper` / `WorkManagerTestInitHelper.getTestDriver`) and controllable `AllSharedPreferences` for `fetchIsSyncInProgress()` assertions
  - Encode `isBugCondition(input)` from the design Bug Condition: `input.isImmediateSync = true AND input.androidApiLevel >= 31 AND (scheduledExpeditedWithNonExpeditedFallback OR NOT syncInProgressFlagClearedByWorkerPath OR existingWorkPolicy = KEEP)`
  - Assert the Expected Behavior Properties from Property 1 / Fix Checking (these are the assertions the fix must satisfy):
    - `workerDidWork(result) = true` — `LocationTaskWorker.doWork()` becomes runnable/executes promptly rather than being deferred by `TIMING_DELAY`/`DEADLINE` (Defect A)
    - `syncInProgressFlagClearedOn(result, SUCCESS) = true` — flag cleared after a successful worker run (Defect B)
    - `syncInProgressFlagClearedOn(result, FAILURE) = true` — flag cleared on failure/auth-failure path (Defect B)
    - `terminalCompleteStatusBroadcast(result) = true` — a terminal `EXTRA_COMPLETE_STATUS=true` status is broadcast (Defect B)
    - `subsequentSyncNotBlocked(result) = true` — a second `startImmediateSync()` proceeds instead of "skipped — sync already in progress" (Defect B)
    - `stuckPriorRequestDoesNotBlockNewRun(result) = true` — a stuck unique-name request does not block a fresh run (Defect C)
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found to understand root cause, e.g.:
    - `doWork()` never runs; jobs stuck with `Unsatisfied constraints: TIMING_DELAY DEADLINE` (Defect A)
    - `fetchIsSyncInProgress()` stays `true` after success and after failure/auth-failure (Defect B)
    - Second `startImmediateSync()` returns early ("already in progress") (Defect B)
    - New immediate request with `ExistingWorkPolicy.KEEP` does not replace the stuck request (Defect C)
  - Mark task complete when the test is written, run on unfixed code, and its failure is documented
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

- [ ] 2. Write preservation property tests (BEFORE implementing the fix)
  - **Property 2: Preservation** - Non-Immediate-Sync Behavior Unchanged
  - **IMPORTANT**: Follow the observation-first methodology - run UNFIXED code first, record actual outputs, then assert those outputs
  - **GOAL**: Lock in the behavior that must NOT change for inputs where `isBugCondition` returns false
  - Observe behavior on UNFIXED code for non-bug-condition (`NOT isBugCondition(input)`) inputs and record actual outputs:
    - `enqueuePeriodic`/`scheduleJob` enqueues periodic unique work with `ExistingPeriodicWorkPolicy.KEEP` and the configured interval/flex (3.1)
    - `RevealWorkerRegistry.workerForTag(tag)` maps each tag to its worker class (3.2)
    - Scheduling uses `androidx.work` only — no `com.evernote:android-job` and no background `startService()`/IntentService startup (3.3)
    - Non-immediate workers (Sync, Hdss, ExtendedSync, Validate, SyncSettings) behave as today (3.4)
    - WorkManager initializes with its default configuration (3.5)
    - `NetworkType.CONNECTED` is applied when `requireNetwork` is set (3.6)
  - Write property-based tests that generate arbitrary non-immediate inputs (periodic tags/intervals/flex, tag lookups, worker classes, network flags) and assert `F(input) == F'(input)` for scheduling shape and policy (from Preservation Requirements in design)
  - Property-based testing generates many test cases automatically for stronger guarantees that non-buggy behavior is unchanged
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms the baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [ ] 3. Fix for sync never running after WorkManager migration (Defects A, B, C)

  - [ ] 3.1 Defect A + C - Make immediate work runnable and allow a fresh run in RevealWorkScheduler.enqueueImmediate
    - In `opensrp-reveal/src/main/java/org/smartregister/reveal/job/RevealWorkScheduler.java`, `enqueueImmediate(...)`, stop unconditionally forcing `.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)` on the immediate-sync chain so requests do not acquire `TIMING_DELAY`/`DEADLINE` constraints and become runnable (Defect A)
    - Change the immediate/manual work policy from `ExistingWorkPolicy.KEEP` to `ExistingWorkPolicy.REPLACE` in `enqueueUniqueWork(uniqueName, ..., request)` so a stuck prior request under the same unique name is replaced (Defect C)
    - Leave periodic scheduling untouched — it must keep `ExistingPeriodicWorkPolicy.KEEP` and the configured interval/flex (preservation)
    - _Bug_Condition: isBugCondition(input) where input.isImmediateSync AND input.androidApiLevel >= 31 AND (scheduledExpeditedWithNonExpeditedFallback OR existingWorkPolicy = KEEP)_
    - _Expected_Behavior: workerDidWork(result) = true; stuckPriorRequestDoesNotBlockNewRun(result) = true (Fix Checking pseudocode from design)_
    - _Preservation: Periodic scheduling keeps ExistingPeriodicWorkPolicy.KEEP and interval/flex; WorkManager-only scheduling; NetworkType.CONNECTED (Preservation Requirements from design)_
    - _Requirements: 2.1, 2.2, 2.6_

  - [ ] 3.2 Defect B - Clear the sync-in-progress flag and broadcast terminal status in the worker path
    - In `opensrp-reveal/src/main/java/org/smartregister/reveal/job/LocationTaskWorker.java`, `doWork()`, before returning `Result.success()`, clear the flag via `AllSharedPreferences.saveIsSyncInProgress(false)` and broadcast a terminal complete status (`EXTRA_COMPLETE_STATUS=true`) so listeners update
    - In the `catch` branch and any terminal `Result.failure()`/`Result.retry()` that ends the immediate attempt, clear the flag and broadcast a terminal status so the app is not left blocked
    - Ensure the auth-failure path (in `runLocationTaskSync`/`onHandleIntent` logic) clears the flag and broadcasts a terminal status instead of returning silently
    - Recommended: centralize the contract in `org/smartregister/job/BaseWorker.java` via a `finally`-style helper (e.g. `finishSync(success)`) that clears the flag and broadcasts terminal status exactly once regardless of outcome, so the flag can never be stranded `true`
    - _Bug_Condition: isBugCondition(input) where input.syncInProgressFlagClearedByWorkerPath = false_
    - _Expected_Behavior: syncInProgressFlagClearedOn(result, SUCCESS) = true; syncInProgressFlagClearedOn(result, FAILURE) = true; terminalCompleteStatusBroadcast(result) = true; subsequentSyncNotBlocked(result) = true (Fix Checking pseudocode from design)_
    - _Preservation: Non-immediate workers behave as today except for the shared flag/broadcast contract (Preservation Requirements 3.4 from design)_
    - _Requirements: 2.3, 2.4, 2.5, 2.7_

  - [ ] 3.3 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Immediate/Manual Sync Runs and Self-Heals Its State
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior; when it passes it confirms the expected behavior is satisfied
    - Run the bug condition exploration test from step 1 on the FIXED code
    - **EXPECTED OUTCOME**: Test PASSES (confirms the bug is fixed - worker runs, flag cleared on success and failure, terminal status broadcast, subsequent sync not blocked, stuck request replaced)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

  - [ ] 3.4 Verify preservation tests still pass
    - **Property 2: Preservation** - Non-Immediate-Sync Behavior Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run the preservation property tests from step 2 on the FIXED code
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions - periodic scheduling, tag→worker mapping, WorkManager-only scheduling, non-immediate workers, WorkManager init, and network constraints are unchanged)
    - Confirm all tests still pass after the fix (no regressions)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [ ] 4. Checkpoint - Ensure all tests pass
  - Run the full unit, property-based, and integration test suites (manual-sync flow, login-triggered sync flow, and the stuck-unique-name-request flow)
  - Ensure all tests pass; ask the user if questions arise

## Notes

- Tasks 1 and 2 MUST be completed on the UNFIXED code before any fix in Task 3 is applied. Task 1 is
  expected to FAIL (proving the bug); Task 2 is expected to PASS (locking in preserved behavior).
- Property 1 (Bug Condition / Expected Behavior) and Property 2 (Preservation) are property-based
  tests. The `**Property N:**` headings enable hover status tracking.
- Do not reintroduce `com.evernote:android-job` or background `startService()`/IntentService startup
  (Requirement 3.3). All scheduling stays on `androidx.work`.
- Periodic scheduling must keep `ExistingPeriodicWorkPolicy.KEEP`; only immediate/manual work moves
  to `ExistingWorkPolicy.REPLACE`.
