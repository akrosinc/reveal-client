# Requirements Document

## Introduction

This feature generalizes `GDRSActivity` — a hardcoded Android Activity in the `opensrp-reveal` module — into a fully configurable, JSON-form-driven activity called `ConfigurableFormTaskActivity`. The current `GDRSActivity` is tightly coupled to the GDRS build country and HDSS data structures (compound IDs, household IDs, individual records). The goal is to remove those couplings so that any task code / intervention type can launch the same activity pattern: a primary capture section driven by `RevealJsonFormFragment`, whose output determines how many sub-task rows appear in a `RecyclerView`, with each sub-task row also opened via a configurable JSON form.

The launcher entry point (`ListTasksActivity.openRCD()`) is also generalised so that any task code can be routed to `ConfigurableFormTaskActivity` by configuration rather than by hard-coded checks.

## Glossary

- **Activity**: An Android `AppCompatActivity` screen.
- **ConfigurableFormTaskActivity**: The new generalised activity that replaces `GDRSActivity`.
- **GDRSActivity**: The existing hardcoded activity being superseded.
- **RevealJsonFormFragment**: The existing form-rendering fragment (`org.smartregister.reveal.fragment.RevealJsonFormFragment`) that presents a JSON form definition to the user.
- **Primary Form**: The main JSON form shown in the top section of `ConfigurableFormTaskActivity`, whose fields capture data for the parent task.
- **Sub-Task**: A child task record displayed as a single row in the `RecyclerView`. Each sub-task is associated with the parent location/task.
- **Sub-Task Form**: The JSON form opened when the user taps a sub-task row.
- **Activity Config**: A JSON configuration object that maps a task code to the Primary Form name, the count-field key, the Sub-Task Form name, and the sub-task row display fields. Stored in the assets under `json.form/activity_config/`.
- **Count Field**: A named field within the Primary Form whose integer value controls how many Sub-Task rows are generated.
- **Presenter**: The MVP presenter class (`ConfigurableFormTaskPresenter`) that mediates between the Activity and the data/interactor layer.
- **Interactor**: The MVP interactor class (`ConfigurableFormTaskInteractor`) that performs background data operations.
- **TaskRepository**: The existing `org.smartregister.repository.TaskRepository` used to read and write `Task` records.
- **TaskUtils**: The existing `org.smartregister.reveal.util.TaskUtils` used to generate new task records.
- **AppExecutors**: The existing `org.smartregister.reveal.util.AppExecutors` used to move work between background and main threads.
- **ListTasksActivity**: The existing map-based activity that launches child activities when the user taps a map structure.
- **Intent Contract**: The set of Android `Intent` extras used to pass data into `ConfigurableFormTaskActivity`: `LOCATION_UUID`, `TASK_IDENTIFIER`, `TASK_BUSINESS_STATUS`, `TASK_CODE`.
- **Business Status**: The workflow state string stored on a `Task` (e.g. `"Not Visited"`, `"Complete"`).
- **Plan ID**: The identifier of the active campaign plan, obtained from `PreferencesUtil.getCurrentPlanId()`.

---

## Requirements

### Requirement 1: Activity Launch via Configurable Task Codes

**User Story:** As a field supervisor, I want any configured task code to open `ConfigurableFormTaskActivity` when I tap the corresponding map structure, so that I am not limited to the GDRS-specific RCD / INDEX_CASE workflow.

#### Acceptance Criteria

1. WHEN the user taps a map structure whose `TASK_CODE` property matches a task code listed in the Activity Config, THE `ListTasksActivity` SHALL launch `ConfigurableFormTaskActivity` instead of `GDRSActivity`.
2. THE `ListTasksActivity` SHALL pass the following extras to `ConfigurableFormTaskActivity`: `LOCATION_UUID`, `TASK_IDENTIFIER`, `TASK_BUSINESS_STATUS`, and `TASK_CODE`.
3. IF the tapped structure's `TASK_CODE` is not listed in the Activity Config, THEN THE `ListTasksActivity` SHALL fall back to its existing form-launch behaviour and SHALL NOT launch `ConfigurableFormTaskActivity`.
4. THE `ConfigurableFormTaskActivity` SHALL read the Activity Config from the assets directory at the path `json.form/activity_config/<task_code>.json` during `onCreate`.
5. IF the Activity Config file for the received `TASK_CODE` is not found in assets, THEN THE `ConfigurableFormTaskActivity` SHALL display an error message to the user and finish the activity.

---

### Requirement 2: Primary Form Display

**User Story:** As a field worker, I want the activity to show a JSON-driven data-capture form at the top of the screen, so that I can record information for the parent task without a hardcoded UI.

#### Acceptance Criteria

1. WHEN `ConfigurableFormTaskActivity` starts, THE `ConfigurableFormTaskActivity` SHALL load the Primary Form identified by the `primaryFormName` field in the Activity Config using `RevealJsonFormFragment`.
2. THE `ConfigurableFormTaskActivity` SHALL embed the `RevealJsonFormFragment` in a dedicated container view within the activity layout.
3. WHILE the Primary Form is loading, THE `ConfigurableFormTaskActivity` SHALL display a progress indicator to the user.
4. IF the Primary Form JSON file is not found in assets, THEN THE `ConfigurableFormTaskActivity` SHALL display an error message and SHALL NOT display an empty form fragment.
5. THE `RevealJsonFormFragment` SHALL pre-populate any field whose key matches an intent extra value (e.g. `TASK_CODE`, `LOCATION_UUID`) if the Activity Config specifies a `prefillMappings` entry for that field.

---

### Requirement 3: Sub-Task Count Derivation from Primary Form

**User Story:** As a field worker, I want the number of sub-task rows to be driven by a count I enter in the primary form, so that the correct number of follow-up tasks are generated for me to complete.

#### Acceptance Criteria

1. WHEN the user submits the Primary Form, THE `ConfigurableFormTaskActivity` SHALL read the integer value of the field identified by `countFieldKey` in the Activity Config from the submitted form JSON.
2. IF the value of the count field is a positive integer, THEN THE `ConfigurableFormTaskPresenter` SHALL generate exactly that number of sub-task records using `TaskUtils.generateTask()`, each associated with `LOCATION_UUID` and the sub-task code defined in the Activity Config.
3. IF the value of the count field is zero or absent, THEN THE `ConfigurableFormTaskPresenter` SHALL NOT generate any sub-task records.
4. IF the value of the count field is not a valid positive integer, THEN THE `ConfigurableFormTaskActivity` SHALL display a validation error message and SHALL NOT generate sub-task records.
5. WHEN sub-task records are generated, THE `ConfigurableFormTaskPresenter` SHALL execute the generation on a background thread via `AppExecutors.diskIO()` and SHALL notify the Activity on the main thread upon completion.

---

### Requirement 4: Sub-Task List Display

**User Story:** As a field worker, I want to see a scrollable list of sub-tasks below the primary form, so that I can track and act on each sub-task individually.

#### Acceptance Criteria

1. THE `ConfigurableFormTaskActivity` SHALL display sub-tasks in a `RecyclerView` positioned below the Primary Form container.
2. WHEN sub-tasks are loaded or refreshed, THE `ConfigurableFormTaskPresenter` SHALL retrieve all `Task` records for `LOCATION_UUID` and the active Plan ID from `TaskRepository` on a background thread.
3. WHEN the sub-task list is ready, THE `ConfigurableFormTaskActivity` SHALL update the `RecyclerView` adapter on the main thread.
4. THE `RecyclerView` adapter SHALL display each sub-task row using the display fields specified in `rowDisplayFields` of the Activity Config; absent fields SHALL render as empty strings.
5. WHILE sub-task data is being loaded, THE `ConfigurableFormTaskActivity` SHALL display a progress indicator.
6. THE `ConfigurableFormTaskActivity` SHALL support a text search field that filters the sub-task list by any value in `rowDisplayFields`; filtering SHALL be case-insensitive.
7. THE `ConfigurableFormTaskActivity` SHALL provide a clear button that resets the search field and restores the full sub-task list.

---

### Requirement 5: Sub-Task Form Launch

**User Story:** As a field worker, I want to tap a sub-task row to open its associated JSON form, so that I can capture the data required for that individual sub-task.

#### Acceptance Criteria

1. WHEN the user taps a sub-task row, THE `ConfigurableFormTaskActivity` SHALL open the Sub-Task Form identified by `subTaskFormName` in the Activity Config using `RevealJsonFormUtils.startJsonForm()`.
2. THE `ConfigurableFormTaskActivity` SHALL pre-populate the Sub-Task Form with all field values specified in `subTaskPrefillFields` of the Activity Config, using data from the tapped row's `Task` record.
3. WHEN the Sub-Task Form returns a result with `RESULT_OK` and the `JSON_FORM_PARAM_JSON` extra, THE `ConfigurableFormTaskPresenter` SHALL save the form data via the Interactor.
4. WHEN a Sub-Task Form result is saved, THE `ConfigurableFormTaskPresenter` SHALL recompute the parent task's `Business Status` according to the `businessStatusRules` defined in the Activity Config and update the `Task` record in `TaskRepository`.
5. WHEN form saving completes, THE `ConfigurableFormTaskActivity` SHALL refresh the sub-task list.

---

### Requirement 6: Sub-Task Reset

**User Story:** As a field supervisor, I want to long-press a completed sub-task row to reset it, so that a worker can re-capture data for that sub-task if needed.

#### Acceptance Criteria

1. WHEN the user long-presses a sub-task row whose `Business Status` is not `"Not Visited"`, THE `ConfigurableFormTaskActivity` SHALL display a reset confirmation dialog.
2. WHEN the user confirms the reset, THE `ConfigurableFormTaskPresenter` SHALL cancel the existing sub-task via `TaskRepository.cancelCompletedTaskByIdentifier()` and SHALL generate a replacement task with `Business Status` `"Not Visited"` using `TaskUtils.generateTask()`.
3. WHEN the user long-presses a sub-task row whose `Business Status` is `"Not Visited"`, THE `ConfigurableFormTaskActivity` SHALL NOT display the reset confirmation dialog.
4. WHEN a sub-task is reset, THE `ConfigurableFormTaskPresenter` SHALL execute the cancel and regeneration on a background thread via `AppExecutors.diskIO()` and SHALL refresh the sub-task list on the main thread upon completion.

---

### Requirement 7: Toolbar Configuration

**User Story:** As a field worker, I want the activity toolbar to show context-relevant information derived from the task and location, so that I can confirm I am working on the correct record.

#### Acceptance Criteria

1. THE `ConfigurableFormTaskActivity` SHALL display a toolbar with three text regions: left, center, and bottom.
2. THE `ConfigurableFormTaskActivity` SHALL populate the left toolbar region with the value of `TASK_CODE` received in the intent.
3. THE `ConfigurableFormTaskActivity` SHALL populate the center and bottom toolbar regions using the values of fields specified by `toolbarCenterField` and `toolbarBottomField` in the Activity Config; absent values SHALL render as empty strings.
4. WHEN location context data (e.g. compound ID, household IDs) is available in the local database, THE `ConfigurableFormTaskPresenter` SHALL provide that data to the Activity to populate the toolbar.

---

### Requirement 8: Add Sub-Task via Menu

**User Story:** As a field worker, I want a menu option to add a new sub-task entry, so that I can register additional subjects that were not covered by the initial count.

#### Acceptance Criteria

1. WHERE the Activity Config contains a non-empty `addSubTaskFormName` field, THE `ConfigurableFormTaskActivity` SHALL display an "Add" menu item in the options menu.
2. WHEN the user selects the "Add" menu item, THE `ConfigurableFormTaskActivity` SHALL open the form identified by `addSubTaskFormName` using `RevealJsonFormUtils.startJsonForm()`.
3. WHEN the Add Sub-Task form returns `RESULT_OK` with `JSON_FORM_PARAM_JSON`, THE `ConfigurableFormTaskPresenter` SHALL save the form data, generate a new sub-task record, and trigger a list refresh.
4. WHERE the Activity Config does not contain `addSubTaskFormName`, THE `ConfigurableFormTaskActivity` SHALL NOT display the "Add" menu item.

---

### Requirement 9: MVP Architecture Compliance

**User Story:** As a developer, I want the new activity to follow the existing MVP pattern used throughout the reveal-client project, so that the code is consistent and maintainable.

#### Acceptance Criteria

1. THE `ConfigurableFormTaskActivity` SHALL implement a `ConfigurableFormTaskContract.View` interface that defines all methods callable by the Presenter.
2. THE `ConfigurableFormTaskPresenter` SHALL implement a `ConfigurableFormTaskContract.Presenter` interface that defines all methods callable by the View.
3. THE `ConfigurableFormTaskInteractor` SHALL implement a `ConfigurableFormTaskContract.Interactor` interface and SHALL perform all database and file I/O on background threads.
4. THE `ConfigurableFormTaskPresenter` SHALL hold a weak reference to the View to prevent memory leaks.
5. THE `ConfigurableFormTaskActivity` SHALL create the Presenter in `onCreate` and SHALL call `presenter.onDestroy()` in `onDestroy`.

---

### Requirement 10: Activity Config JSON Schema

**User Story:** As a developer, I want a documented and validated JSON config schema for `ConfigurableFormTaskActivity`, so that I can add support for new task codes without modifying Java code.

#### Acceptance Criteria

1. THE Activity Config JSON file SHALL contain a required `primaryFormName` string field identifying the Primary Form asset path.
2. THE Activity Config JSON file SHALL contain a required `countFieldKey` string field identifying the field in the Primary Form whose value determines the sub-task count.
3. THE Activity Config JSON file SHALL contain a required `subTaskCode` string field identifying the task code used when generating sub-task records.
4. THE Activity Config JSON file SHALL contain a required `subTaskFormName` string field identifying the Sub-Task Form asset path.
5. THE Activity Config JSON file SHALL contain a required `rowDisplayFields` array of strings, each naming a field to display in a sub-task row.
6. THE Activity Config JSON file SHALL contain an optional `businessStatusRules` object mapping completion state conditions to Business Status strings.
7. THE Activity Config JSON file SHALL contain an optional `addSubTaskFormName` string field.
8. THE Activity Config JSON file SHALL contain an optional `prefillMappings` object mapping form field keys to intent extra keys.
9. THE Activity Config JSON file SHALL contain optional `toolbarCenterField` and `toolbarBottomField` string fields.
10. IF any required field is missing from the Activity Config, THEN THE `ConfigurableFormTaskActivity` SHALL log the validation error and display a user-visible error message before finishing the activity.

---

### Requirement 11: Background Thread Safety

**User Story:** As a developer, I want all database and file operations to run off the main thread, so that the UI remains responsive and does not trigger ANR errors.

#### Acceptance Criteria

1. THE `ConfigurableFormTaskInteractor` SHALL execute all `TaskRepository` read and write operations on `AppExecutors.diskIO()`.
2. THE `ConfigurableFormTaskInteractor` SHALL execute all asset file reads on `AppExecutors.diskIO()`.
3. WHEN background work completes, THE `ConfigurableFormTaskInteractor` SHALL deliver results to the Presenter callback on `AppExecutors.mainThread()`.
4. THE `ConfigurableFormTaskActivity` SHALL NOT perform any database or file I/O on the main thread.

---

### Requirement 12: Backward Compatibility with GDRSActivity

**User Story:** As a release manager, I want the existing GDRS workflow to continue functioning during the migration period, so that production users are not disrupted.

#### Acceptance Criteria

1. THE `GDRSActivity` SHALL remain present and functional in the codebase until explicitly removed by a follow-up task.
2. WHEN the `ConfigurableFormTaskActivity` is launched for GDRS task codes (`RCD`, `INDEX_CASE`, `SECONDARY_INDEX_CASE`), THE `ConfigurableFormTaskActivity` SHALL produce equivalent functional behaviour to the current `GDRSActivity` when configured with the corresponding GDRS Activity Config files.
3. THE `ListTasksActivity` SHALL use `ConfigurableFormTaskActivity` for GDRS task codes only after the corresponding Activity Config files are present in assets AND the feature flag `USE_CONFIGURABLE_FORM_TASK_ACTIVITY` is set to `true` in `global_configs`.
4. IF the feature flag `USE_CONFIGURABLE_FORM_TASK_ACTIVITY` is `false` or absent, THEN THE `ListTasksActivity` SHALL continue to launch `GDRSActivity` for GDRS task codes.
