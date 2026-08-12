package org.smartregister.reveal.template;

import static org.smartregister.reveal.util.Constants.BusinessStatus.IN_PROGRESS;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.domain.Period;
import org.smartregister.domain.Task;
import org.smartregister.repository.TaskRepository;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.interactor.BaseInteractor;
import org.smartregister.reveal.util.AppExecutors;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import org.smartregister.util.JsonFormUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import timber.log.Timber;

/**
 * Generic concrete activity: Form 1 + Task Recycler + optional Form 2.
 *
 * <p>The task list is driven purely by the parent/child task relationship:
 * child tasks have {@link Task#getParentTaskId()} == parent task identifier.
 *
 * <h3>Intent extras</h3>
 * <ul>

 *   <li>{@link #EXTRA_GATE_FIELD_KEY}     – field key in Form 1 that gates Form 2 visibility</li>
 *   <li>{@link #EXTRA_GATE_FIELD_VALUE}   – value that makes Form 2 visible (default "yes")</li>
 *   <li>{@link #EXTRA_PARENT_TASK_ID}     – parent task identifier</li>
 *   <li>{@link #EXTRA_LOCATION_UUID}      – structure UUID</li>
 *   <li>{@link #EXTRA_CHILD_TASK_CODE}    – code for generated child tasks</li>
 *   <li>{@link #EXTRA_CHILD_FORM_NAME}    – asset path for the form opened when a child task is tapped</li>
 *   <li>{@link #EXTRA_BUSINESS_STATUS_FIELD} – field key in Form 1 holding computed business status</li>
 * </ul>
 */
public class FormRecyclerFormActivity extends TemplateHostActivity
        implements TaskListCallbacks {

    /* ------------------------------------------------------------------ intent extra keys */
    public static final String EXTRA_FORM_NAME             = "formName";
    public static final String EXTRA_GATE_FIELD_KEY        = "gateFieldKey";
    public static final String EXTRA_GATE_FIELD_VALUE      = "gateFieldValue";
    public static final String EXTRA_PARENT_TASK_ID        = Constants.Properties.TASK_IDENTIFIER;
    public static final String EXTRA_LOCATION_UUID         = Constants.Properties.LOCATION_UUID;
    public static final String EXTRA_CHILD_TASK_CODE       = "childTaskCode";
    public static final String EXTRA_CHILD_FORM_NAME       = "childFormName";
    public static final String EXTRA_BUSINESS_STATUS_FIELD = "businessStatusField";
    public static final String EXTRA_COUNT_HINT            = "countHint";
    public static final String EXTRA_COUNT_LABEL           = "countLabel";
    public static final String EXTRA_RECYCLER_HEADER       = "recyclerHeader";
    public static final String EXTRA_RECYCLER_GATE_KEY     = "recyclerGateFieldKey";
    public static final String EXTRA_RECYCLER_GATE_VALUE   = "recyclerGateFieldValue";
    public static final String EXTRA_EMPTY_MESSAGE         = "emptyMessage";

    /* ------------------------------------------------------------------ fragment tags */
    private static final String TAG_FORM1    = "form1";
    private static final String TAG_FORM2    = "form2";
    private static final String TAG_RECYCLER = "task_recycler";

    /* ------------------------------------------------------------------ form field keys */
    private static final String FIELD_VISIT_DATE       = "visit_date";
    private static final String FIELD_HOUSEHOLD_ID     = "household_id_value";
    private static final String FIELD_TASK_ID          = "task_id";
    private static final String FIELD_ENTITY_ID        = "entity_id";
    private static final String FIELD_BUSINESS_STATUS  = "business_status";

    /* ------------------------------------------------------------------ state */
    private String formName;
    private String gateFieldKey;
    private Set<String> gateFieldValues;
    private String parentTaskId;
    private String locationUUID;
    private String childTaskCode;
    private String childFormName;
    private String businessStatusField;
    private String countHint;
    private String countLabel;
    private String recyclerHeader;
    private String recyclerGateKey;
    private Set<String> recyclerGateValues;
    private String emptyMessage;

    private boolean recyclerVisible = false;

    private boolean form2Visible = false;

    private AppExecutors         appExecutors;
    private TaskRepository       taskRepository;
    private RevealJsonFormUtils  formUtils;
    private TaskListFragment     taskListFragment;

    private LinearLayout progressOverlay;
    private TextView     tvProgressMessage;
    private TextView     tvSectionHeader;

    /* ------------------------------------------------------------------ setContentView guard */
    private boolean contentViewSet = false;

    @Override
    public void setContentView(int layoutResID) {
        if (!contentViewSet) { super.setContentView(layoutResID); contentViewSet = true; }
    }

    @Override
    public void setContentView(android.view.View view) {
        if (!contentViewSet) { super.setContentView(view); contentViewSet = true; }
    }

    @Override
    public void setContentView(android.view.View view,
                               android.view.ViewGroup.LayoutParams p) {
        if (!contentViewSet) { super.setContentView(view, p); contentViewSet = true; }
    }

    /* ------------------------------------------------------------------ lifecycle */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.template_form_recycler_form);
        Timber.tag("TestFrag").i("FormRecyclerFormActivity openFormByTemplate onCreate");
        readExtras();
        formUtils      = new RevealJsonFormUtils();
        taskRepository = RevealApplication.getInstance().getTaskRepository();
        appExecutors   = RevealApplication.getInstance().getAppExecutors();

        // Load the single form JSON (contains step1 + optionally step2)
        JSONObject formJSON = formUtils.getFormJSON(this, formName, null, null);


        getIntent().putExtra(JsonFormConstants.JSON_FORM_KEY.JSON,
                formJSON != null ? formJSON.toString()
                        : "{\"encounter_type\":\"placeholder\",\"count\":\"1\","
                        + "\"step1\":{\"title\":\"\",\"fields\":[]}}");

        super.onCreate(savedInstanceState);

        progressOverlay   = findViewById(R.id.layout_progress);
        tvProgressMessage = findViewById(R.id.tv_progress_message);
        tvSectionHeader   = findViewById(R.id.tv_section_header);

        setupTaskList();

        // Populate form data on disk thread, then commit both step fragments
        showProgress("Loading…");
        appExecutors.diskIO().execute(() -> {
            populateForm1();
            appExecutors.mainThread().execute(() -> {
                commitStep1Fragment();
                // Check if the gate field is already satisfied (re-opening a saved form)
                checkGateFieldOnLoad();
                hideProgress();
            });
        });

        refreshTaskList();
    }


    @Override
    public void initializeFormFragment() {
        // no-op
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Timber.tag("RevealCamera").i("FormRecyclerFormActivity.onActivityResult: requestCode=%d, resultCode=%d", requestCode, resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        // Child task form returned
        if (requestCode == Constants.RequestCode.REQUEST_CODE_GET_JSON
                && resultCode == RESULT_OK
                && data != null
                && data.hasExtra(Constants.JSON_FORM_PARAM_JSON)) {

            String json = data.getStringExtra(Constants.JSON_FORM_PARAM_JSON);
            showProgress("Saving Task…");
            appExecutors.diskIO().execute(() -> {
                saveChildTaskForm(json);
                // refreshTaskList + checkAllComplete are called from
                // FormSaveInteractor.onFormSaved on the main thread after task update
                appExecutors.mainThread().execute(this::hideProgress);
            });
        }
    }

    /* ------------------------------------------------------------------ TemplateHostActivity */

    @Override
    protected void onFragmentDataReady(String fragmentTag, String json) {
        Timber.tag("FormSaveInteractor").i("onFragmentDataReady: fragmentTag=%s, jsonLength=%d",
                fragmentTag, json != null ? json.length() : 0);
        // Both step1 and step2 live in one mJSONObject.
        // Whichever fragment triggers save, the json contains ALL fields from both steps.
        onFormSaved(json);
    }

    /* ------------------------------------------------------------------ Fragment commits */

    private void commitStep1Fragment() {
        EmbeddedFormFragment fragment = EmbeddedFormFragment.newInstance(
                JsonFormConstants.FIRST_STEP_NAME, TAG_FORM1);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment, TAG_FORM1)
                .commitAllowingStateLoss();
    }

    private void commitStep2Fragment() {
        // Only commit if the form JSON actually has a step2 defined
        JSONObject json = getmJSONObject();
        if (json != null && json.has("step2")) {
            EmbeddedFormFragment fragment = EmbeddedFormFragment.newInstance(
                    "step2", TAG_FORM2);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_form2, fragment, TAG_FORM2)
                    .commitAllowingStateLoss();
        } else {
            Timber.tag("FormRecyclerForm")
                    .w("Cannot commit step2 fragment — 'step2' not found in form JSON. "
                     + "Ensure the form JSON has \"count\": \"2\" and a \"step2\" object.");
        }
    }

    /**
     * Called on disk thread — pre-populates the parent form with data from the
     * last saved event (if the task was previously completed).
     * This enables "edit" mode for already-completed parent tasks.
     */
    protected void populateForm1() {
        if (parentTaskId == null) {
            Timber.tag("FormSaveInteractor").i("populateForm1: parentTaskId is null, skipping");
            return;
        }

        Task parentTask = taskRepository.getTaskByIdentifier(parentTaskId);
        if (parentTask == null) {
            Timber.tag("FormSaveInteractor").i("populateForm1: parentTask not found for id=%s", parentTaskId);
            return;
        }

        Timber.tag("FormSaveInteractor").i("populateForm1: parentTask found. status=%s, businessStatus=%s, forEntity=%s",
                parentTask.getStatus(), parentTask.getBusinessStatus(), parentTask.getForEntity());

        // Always inject entity_id and details.taskIdentifier into mJSONObject
        // so that when the form saves, the event has the correct baseEntityId
        // and clientProcessor can find the task to update.
        try {
            JSONObject currentForm = getmJSONObject();
            if (currentForm != null) {
                currentForm.put("entity_id", parentTask.getForEntity());
                JSONObject details = currentForm.has("details")
                        ? currentForm.getJSONObject("details")
                        : new JSONObject();
                details.put(Constants.Properties.TASK_IDENTIFIER, parentTaskId);
                details.put(Constants.Properties.LOCATION_UUID, locationUUID);
                currentForm.put("details", details);
                Timber.tag("FormSaveInteractor").i("populateForm1: injected entity_id=%s, taskIdentifier=%s",
                        parentTask.getForEntity(), parentTaskId);

                // Pre-populate date field with today's date for fresh forms
                try {
                    formUtils.populateField(currentForm, FIELD_VISIT_DATE,
                            org.joda.time.LocalDate.now().toString("dd-MM-yyyy"),
                            com.vijay.jsonwizard.constants.JsonFormConstants.VALUE);
                    Timber.tag("FormSaveInteractor").i("populateForm1: date set to %s",
                            org.joda.time.LocalDate.now().toString("dd-MM-yyyy"));
                } catch (Exception dateEx) {
                    Timber.tag("FormSaveInteractor").e(dateEx, "populateForm1: date population failed");
                }

                // Pre-populate household_id_value with UUID for fresh forms only
                // (when re-opening, populateForm from last event will overwrite with the saved value)
                if (Constants.BusinessStatus.NOT_VISITED.equals(parentTask.getBusinessStatus())) {
                    try {
                        formUtils.populateField(currentForm, FIELD_HOUSEHOLD_ID,
                                java.util.UUID.randomUUID().toString(),
                                com.vijay.jsonwizard.constants.JsonFormConstants.VALUE);
                    } catch (Exception e) {
                        Timber.tag("FormSaveInteractor").w(e, "populateForm1: household_id_value population failed");
                    }
                }
            }
        } catch (Exception e) {
            Timber.tag("FormSaveInteractor").e(e, "populateForm1: error injecting entity_id/details");
        }

        // If the parent task has been completed/started before, find the last event and pre-fill
        if (Task.TaskStatus.COMPLETED.equals(parentTask.getStatus())
                || !NOT_VISITED.equals(parentTask.getBusinessStatus())) {

            Timber.tag("FormSaveInteractor").i("populateForm1: task not NOT_VISITED, looking for last event");

            String encounterType = getEncounterTypeFromForm();
            String entityId = parentTask.getForEntity();

            Timber.tag("FormSaveInteractor").i("populateForm1: searching with entityId=%s, encounterType=%s",
                    entityId, encounterType);

            org.smartregister.repository.EventClientRepository ecRepo =
                    RevealApplication.getInstance().getContext().getEventClientRepository();

            JSONObject eventJson = ecRepo.getEventsByBaseEntityIdAndEventType(
                    entityId, encounterType);

            Timber.tag("FormSaveInteractor").i("populateForm1: eventJson found=%b",
                    eventJson != null);

            if (eventJson != null) {
                Timber.tag("FormSaveInteractor").i("populateForm1: eventJson keys=%s",
                        eventJson.keys() != null ? eventJson.keys().toString() : "null");

                org.smartregister.domain.Event lastEvent =
                        ecRepo.convert(eventJson, org.smartregister.domain.Event.class);

                Timber.tag("FormSaveInteractor").i("populateForm1: converted event=%b, eventType=%s, obs count=%d",
                        lastEvent != null,
                        lastEvent != null ? lastEvent.getEventType() : "null",
                        lastEvent != null && lastEvent.getObs() != null ? lastEvent.getObs().size() : 0);

                if (lastEvent != null) {
                    JSONObject currentForm = getmJSONObject();
                    Timber.tag("FormSaveInteractor").i("populateForm1: currentForm=%b, calling populateForm",
                            currentForm != null);
                    if (currentForm != null) {
                        formUtils.populateForm(lastEvent, currentForm);
                        Timber.tag("FormSaveInteractor").i("populateForm1: populateForm complete");
                    }
                }
            } else {
                Timber.tag("FormSaveInteractor").i("populateForm1: no event found in DB for entity=%s, encounterType=%s",
                        entityId, encounterType);
            }
        } else {
            Timber.tag("FormSaveInteractor").i("populateForm1: task is NOT_VISITED, no pre-population needed");
        }
    }

    /**
     * Extracts the encounter_type from the current mJSONObject.
     */
    private String getEncounterTypeFromForm() {
        JSONObject json = getmJSONObject();
        if (json != null) {
            return json.optString(Constants.JsonForm.ENCOUNTER_TYPE, "");
        }
        return "";
    }

    /**
     * Finds the latest event for a specific task using the taskId column on the event table.
     * Must be called on the disk thread.
     */
    private JSONObject findEventByTaskIdentifier(String taskIdentifier) {
        if (taskIdentifier == null) return null;
        try {
            net.sqlcipher.database.SQLiteDatabase db =
                    RevealApplication.getInstance().getRepository().getReadableDatabase();
            String query = "SELECT json FROM event WHERE taskId = ? ORDER BY updatedAt DESC LIMIT 1";
            try (net.sqlcipher.Cursor cursor = db.rawQuery(query, new String[]{taskIdentifier})) {
                if (cursor.moveToFirst()) {
                    String jsonStr = cursor.getString(0);
                    if (jsonStr != null) {
                        return new JSONObject(jsonStr.replaceAll("'", ""));
                    }
                }
            }
        } catch (Exception e) {
            Timber.tag("FormSaveInteractor").e(e, "findEventByTaskIdentifier error for taskId=%s", taskIdentifier);
        }
        return null;
    }

    private void onFormSaved(String json) {
        Timber.tag("FormSaveInteractor").i("onFormSaved: starting save process");

        // Validate: count field must have a value — but only if recycler is visible
        if (recyclerVisible) {
            String countText = getTaskCountFromFragment();
            if (countText == null || countText.trim().isEmpty()) {
                Timber.tag("FormSaveInteractor").i("onFormSaved: count field is empty, blocking save");
                android.widget.Toast.makeText(this,
                        "Please enter the number of tasks", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
        }

        showProgress("Saving…");
        appExecutors.diskIO().execute(() -> {
            Timber.tag("FormSaveInteractor").i("onFormSaved: diskIO - calling saveForm");
            saveForm(json);
            Timber.tag("FormSaveInteractor").i("onFormSaved: diskIO - saveForm complete");

            Timber.tag("FormSaveInteractor").i("onFormSaved: diskIO - updating parent business status");
            updateParentBusinessStatus(json);
            Timber.tag("FormSaveInteractor").i("onFormSaved: diskIO - parent business status updated");

            appExecutors.mainThread().execute(() -> {
                Timber.tag("FormSaveInteractor").i("onFormSaved: mainThread - save complete, finishing activity");
                hideProgress();
                finish();
            });
        });
    }

    /**
     * Reads the current count value from the TaskListFragment's count field.
     */
    private String getTaskCountFromFragment() {
        if (taskListFragment != null && taskListFragment.getView() != null) {
            android.widget.EditText etCount =
                    taskListFragment.getView().findViewById(R.id.et_task_count);
            if (etCount != null) {
                return etCount.getText().toString();
            }
        }
        return null;
    }

    /* ------------------------------------------------------------------ reactive gate: watch field changes */

    /**
     * Intercepts every field value write.
     * When the gate field matches, the step2 container is shown.
     * When it doesn't match, the step2 container is hidden.
     * The step2 fragment is already committed — we just toggle its container visibility.
     */
    @Override
    public void writeValue(String stepName, String key, String value,
                           String openMrsEntityParent, String openMrsEntity,
                           String openMrsEntityId, boolean popup)
            throws org.json.JSONException {
        super.writeValue(stepName, key, value, openMrsEntityParent, openMrsEntity,
                openMrsEntityId, popup);

        // Form 2 gate
        if (gateFieldKey != null && gateFieldKey.equals(key)) {
            if (gateFieldValues.contains(value.toLowerCase())) {
                showStep2();
            } else {
                hideStep2();
            }
        }

        // Recycler gate
        if (recyclerGateKey != null && recyclerGateKey.equals(key)) {
            if (recyclerGateValues.contains(value.toLowerCase())) {
                showRecycler();
            } else {
                hideRecycler();
            }
        }
    }

    private void showRecycler() {
        if (!recyclerVisible) {
            recyclerVisible = true;
            setSectionVisible(true);
            // Also show the task list fragment container
            findViewById(R.id.container_recycler).setVisibility(View.VISIBLE);
            refreshTaskList();
        }
    }

    private void hideRecycler() {
        if (recyclerVisible) {
            recyclerVisible = false;
            setSectionVisible(false);
            findViewById(R.id.container_recycler).setVisibility(View.GONE);
        }
    }

    private boolean step2Committed = false;

    private void showStep2() {
        if (!form2Visible) {
            form2Visible = true;
            findViewById(R.id.view_divider_form2).setVisibility(View.VISIBLE);
            findViewById(R.id.container_form2).setVisibility(View.VISIBLE);

            // Commit step2 fragment on first reveal
            if (!step2Committed) {
                commitStep2Fragment();
                step2Committed = true;
            }
        }
    }

    /**
     * Checks the gate field value in mJSONObject on load.
     * If the form was previously saved with the gate satisfied, step2 should
     * be visible immediately without waiting for a user interaction.
     */
    private void checkGateFieldOnLoad() {
        JSONObject json = getmJSONObject();
        if (json == null) return;
        String jsonStr = json.toString();

        // Check Form 2 gate
        if (gateFieldKey != null) {
            String value = org.smartregister.util.JsonFormUtils.getFieldValue(jsonStr, gateFieldKey);
            Timber.tag("FormSaveInteractor").i("checkGateFieldOnLoad: gateFieldKey=%s, value=%s, gateFieldValues=%s",
                    gateFieldKey, value, gateFieldValues);
            if (value != null && gateFieldValues.contains(value.toLowerCase())) {
                showStep2();
            }
        }

        // Check Recycler gate
        if (recyclerGateKey != null) {
            String value = org.smartregister.util.JsonFormUtils.getFieldValue(jsonStr, recyclerGateKey);
            Timber.tag("FormSaveInteractor").i("checkGateFieldOnLoad: recyclerGateKey=%s, value=%s, recyclerGateValues=%s",
                    recyclerGateKey, value, recyclerGateValues);
            if (value != null && recyclerGateValues.contains(value.toLowerCase())) {
                showRecycler();
            }
        }
    }

    private void hideStep2() {
        if (form2Visible) {
            form2Visible = false;
            findViewById(R.id.view_divider_form2).setVisibility(View.GONE);
            findViewById(R.id.container_form2).setVisibility(View.GONE);
        }
    }

    /* ------------------------------------------------------------------ Form 2 */



    private void onForm2Saved(String json) {
        showProgress("Saving…");
        appExecutors.diskIO().execute(() -> {
            saveForm(json);
            appExecutors.mainThread().execute(() -> {
                hideProgress();
                checkAllComplete();
            });
        });
    }

    /* ------------------------------------------------------------------ Task List */

    private void setupTaskList() {
        taskListFragment = TaskListFragment.newInstance();
        taskListFragment.setDisplayProvider(new DefaultTaskDisplayProvider());
        taskListFragment.setCallbacks(this);
        if (countHint != null) taskListFragment.setCountHint(countHint);
        if (countLabel != null) taskListFragment.setCountLabel(countLabel);
        if (recyclerHeader != null) taskListFragment.setHeader(recyclerHeader);
        if (emptyMessage != null) taskListFragment.setEmptyMessage(emptyMessage);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container_recycler, taskListFragment, TAG_RECYCLER)
                .commit();

        // If a recycler gate is configured, start hidden
        if (recyclerGateKey != null) {
            recyclerVisible = false;
            findViewById(R.id.container_recycler).setVisibility(View.GONE);
            setSectionVisible(false);
        } else {
            // No gate — recycler is always visible
            recyclerVisible = true;
        }
    }

    public void refreshTaskList() {
        if (appExecutors == null) return;
        showProgress("Loading Tasks…");
        appExecutors.diskIO().execute(() -> {
            String planId = PreferencesUtil.getInstance().getCurrentPlanId();
            Set<Task> childTasks = taskRepository.getTasksByParentId(parentTaskId, planId);

            Timber.tag("FormSaveInteractor").i("refreshTaskList: parentTaskId=%s, planId=%s, childTasks found=%d",
                    parentTaskId, planId, childTasks.size());
            for (Task t : childTasks) {
                Timber.tag("FormSaveInteractor").i("  child: id=%s, status=%s, businessStatus=%s",
                        t.getIdentifier(), t.getStatus(), t.getBusinessStatus());
            }

            DefaultTaskDisplayProvider provider = new DefaultTaskDisplayProvider();

            List<TaskRowItem> items = childTasks.stream()
                    .sorted(Comparator.comparing(Task::getRowid,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(task -> TaskRowItem.from(task, provider,
                            task.getAuthoredOn() != null
                                    ? task.getAuthoredOn().toString("yyyy-MM-dd") : ""))
                    .collect(Collectors.toList());

            appExecutors.mainThread().execute(() -> {
                hideProgress();
                boolean hasTasks = !items.isEmpty();
                setSectionVisible(hasTasks);
                taskListFragment.setItems(items);
            });
        });
    }

    @Override
    public void onGenerateTasks(int count) {
        showProgress("Generating tasks…");
        appExecutors.diskIO().execute(() -> {
            String planId = PreferencesUtil.getInstance().getCurrentPlanId();

            // 1. Cancel all existing child tasks
            Set<Task> existing = taskRepository.getTasksByParentId(parentTaskId, planId);
            for (Task t : existing) {
                taskRepository.cancelTaskByIdentifier(t.getIdentifier());
            }

            // 2. Create N new child tasks
            String owner = RevealApplication.getInstance().getContext()
                    .allSharedPreferences().fetchRegisteredANM();

            for (int i = 0; i < count; i++) {
                Task childTask = new Task();
                childTask.setIdentifier(java.util.UUID.randomUUID().toString());
                childTask.setPlanIdentifier(planId);
                childTask.setGroupIdentifier(
                        PreferencesUtil.getInstance().getCurrentOperationalAreaId());
                childTask.setStatus(Task.TaskStatus.READY);
                childTask.setBusinessStatus(NOT_VISITED);
                childTask.setPriority(Task.TaskPriority.ROUTINE);
                childTask.setCode(childTaskCode);
                childTask.setDescription(childTaskCode + " " + (i + 1));
                childTask.setForEntity(locationUUID);
                childTask.setStructureId(locationUUID);
                childTask.setAuthoredOn(org.joda.time.DateTime.now());
                childTask.setLastModified(org.joda.time.DateTime.now());
                childTask.setOwner(owner);
                childTask.setSyncStatus(org.smartregister.repository.BaseRepository.TYPE_Created);
                childTask.setParentTaskId(parentTaskId);
                childTask.setExecutionPeriod(new Period(DateTime.now(),DateTime.now()));

                Task task = taskRepository.addOrUpdate(childTask, false);

                Timber.tag("FormSaveInteractor").i("onGenerateTasks: created child %d/%d id=%s, parentId=%s",
                    (i + 1), count, childTask.getIdentifier(), parentTaskId);

                Timber.tag("FormSaveInteractor").i("onGenerateTasks: saved child %d/%d id=%s, parentId=%s",
                    (i + 1), count, task.getIdentifier(), parentTaskId);
            }

            // 3. Update parent task to "In Progress" since it now has child tasks
            Task parentTask = taskRepository.getTaskByIdentifier(parentTaskId);
            if (parentTask != null) {
                parentTask.setBusinessStatus(Constants.BusinessStatus.IN_PROGRESS);
                parentTask.setStatus(Task.TaskStatus.IN_PROGRESS);
                parentTask.setLastModified(org.joda.time.DateTime.now());
                parentTask.setSyncStatus(
                        org.smartregister.repository.BaseRepository.TYPE_Unsynced);
                taskRepository.addOrUpdate(parentTask, false);
                Timber.tag("FormSaveInteractor")
                        .i("onGenerateTasks: parent task %s → In Progress", parentTaskId);
            }

            // Signal the map to refresh when returning to ListTasksActivity
            RevealApplication.getInstance().setRefreshMapOnEventSaved(true);

            appExecutors.mainThread().execute(() -> {
                hideProgress();
                refreshTaskList();
            });
        });
    }

    @Override
    public void onTaskTap(Task task) {
        if (childFormName == null) {
            Timber.tag("FormRecyclerForm").w("No childFormName configured");
            return;
        }

        appExecutors.diskIO().execute(() -> {
            // Build task details so TASK_IDENTIFIER gets injected into form details.
            org.smartregister.reveal.model.BaseTaskDetails taskDetails =
                    new org.smartregister.reveal.model.BaseTaskDetails(task.getIdentifier());
            taskDetails.setTaskId(task.getIdentifier());
            taskDetails.setTaskStatus(task.getStatus().name());
            taskDetails.setBusinessStatus(task.getBusinessStatus());
            taskDetails.setStructureId(task.getStructureId());
            taskDetails.setTaskCode(task.getCode());
            taskDetails.setTaskEntity(task.getForEntity());

            JSONObject formJSON = formUtils.getFormJSON(this, childFormName, taskDetails, null);
            if (formJSON == null) return;

            // Pre-populate entity/task info
            try {
                formUtils.populateField(formJSON, FIELD_TASK_ID, task.getIdentifier(),
                        JsonFormConstants.VALUE);
                formUtils.populateField(formJSON, FIELD_ENTITY_ID, task.getForEntity(),
                        JsonFormConstants.VALUE);
            } catch (Exception e) {
                Timber.tag("FormRecyclerForm").w(e, "Pre-population failed");
            }

            // If child task is already completed, find last event and populate form
            if (!NOT_VISITED.equals(task.getBusinessStatus())) {
                Timber.tag("FormSaveInteractor").i("onTaskTap: task completed, finding last event for taskId=%s",
                        task.getIdentifier());

                org.smartregister.repository.EventClientRepository ecRepo =
                        RevealApplication.getInstance().getContext().getEventClientRepository();

                // Query by taskIdentifier in event details — not by baseEntityId,
                // since all child tasks share the same forEntity.
                JSONObject eventJson = findEventByTaskIdentifier(task.getIdentifier());

                if (eventJson != null) {
                    org.smartregister.domain.Event lastEvent =
                            ecRepo.convert(eventJson, org.smartregister.domain.Event.class);
                    if (lastEvent != null) {
                        formUtils.populateForm(lastEvent, formJSON);
                        Timber.tag("FormSaveInteractor").i("onTaskTap: populated form from last event");
                    }
                } else {
                    Timber.tag("FormSaveInteractor").i("onTaskTap: no event found for taskId=%s",
                            task.getIdentifier());
                }
            }

            appExecutors.mainThread().execute(() ->
                    formUtils.startJsonForm(formJSON, this,
                            Constants.RequestCode.REQUEST_CODE_GET_JSON));
        });
    }

    @Override
    public void onTaskLongPress(Task task) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reset Task")
                .setMessage("Reset this task to Not Visited?")
                .setPositiveButton("Reset", (d, w) -> {
                    showProgress("Resetting…");
                    appExecutors.diskIO().execute(() -> {
                        taskRepository.cancelCompletedTaskByIdentifier(task.getIdentifier());
                        org.smartregister.reveal.util.TaskUtils.getInstance().generateTask(
                                this, task.getForEntity(), task.getStructureId(),
                                NOT_VISITED,
                                task.getCode(), R.string.tasks_section_header);
                        appExecutors.mainThread().execute(() -> {
                            hideProgress();
                            refreshTaskList();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* ------------------------------------------------------------------ completion check */

    private void checkAllComplete() {
        appExecutors.diskIO().execute(() -> {
            String planId = PreferencesUtil.getInstance().getCurrentPlanId();
            Set<Task> childTasks = taskRepository.getTasksByParentId(parentTaskId, planId);

            boolean allChildrenDone = !childTasks.isEmpty() && childTasks.stream()
                    .allMatch(t -> Constants.BusinessStatus.COMPLETE
                            .equals(t.getBusinessStatus()));

            boolean formSaved = !savedFormData.isEmpty();

            if (formSaved && allChildrenDone) {
                appExecutors.mainThread().execute(this::onAllComplete);
            }
        });
    }

    /**
     * Called when all conditions are met: Form 1 saved, all child tasks complete,
     * and Form 2 saved (if required). Override in subclass for custom behavior.
     */
    protected void onAllComplete() {
        Timber.tag("FormRecyclerForm").i("All forms and tasks complete");
        // Default: update parent task to COMPLETE
        appExecutors.diskIO().execute(() -> {
            Task parentTask = taskRepository.getTaskByIdentifier(parentTaskId);
            if (parentTask != null) {
                parentTask.setBusinessStatus(Constants.BusinessStatus.COMPLETE);
                parentTask.setStatus(Task.TaskStatus.COMPLETED);
                parentTask.setLastModified(org.joda.time.DateTime.now());
                taskRepository.addOrUpdate(parentTask, false);
            }
        });
    }

    /* ------------------------------------------------------------------ save helpers */

    private void saveForm(String json) {
        Timber.tag("FormSaveInteractor").i("saveForm: entering");
        try {
            // Delegate to BaseInteractor.saveJsonForm which handles all encounter types
            new FormSaveInteractor().saveJsonForm(json);
            Timber.tag("FormSaveInteractor").i("saveForm: saveJsonForm dispatched");
        } catch (Exception e) {
            Timber.tag("FormSaveInteractor").e(e, "saveForm: Error saving form");
        }
    }

    private void saveChildTaskForm(String json) {
        Timber.tag("FormSaveInteractor").i("saveChildTaskForm: entering");
        // FormSaveInteractor.onFormSaved callback handles the task status update.
        saveForm(json);
    }

    private void updateParentBusinessStatus(String form1Json) {
        Timber.tag("FormSaveInteractor").i("updateParentBusinessStatus: businessStatusField=%s, parentTaskId=%s",
                businessStatusField, parentTaskId);

        String status = null;

        // Try to read from a configured business_status field in the form
        if (businessStatusField != null) {
            status = JsonFormUtils.getFieldValue(form1Json, businessStatusField);
            Timber.tag("FormSaveInteractor").i("updateParentBusinessStatus: extracted status from form=%s", status);
        }

        // If no explicit status from form, derive it from child task state
        if (status == null || status.isEmpty() || status.equals(NOT_VISITED) || status.equals(IN_PROGRESS)) {
            String planId = PreferencesUtil.getInstance().getCurrentPlanId();
            Set<Task> childTasks = taskRepository.getTasksByParentId(parentTaskId, planId);

            if (childTasks.isEmpty()) {
                // No children → count was 0 and locked → Complete
                status = Constants.BusinessStatus.COMPLETE;
            } else {
                boolean allChildrenDone = childTasks.stream()
                        .allMatch(t -> Constants.BusinessStatus.COMPLETE.equals(t.getBusinessStatus()));
                // Only Complete if ALL children are done; otherwise In Progress
                status = allChildrenDone
                        ? Constants.BusinessStatus.COMPLETE
                        : Constants.BusinessStatus.IN_PROGRESS;
            }
            Timber.tag("FormSaveInteractor").i("updateParentBusinessStatus: derived status=%s (childTasks=%d)",
                    status, childTasks.size());
        }

        Task parentTask = taskRepository.getTaskByIdentifier(parentTaskId);
        Timber.tag("FormSaveInteractor").i("updateParentBusinessStatus: parentTask found=%b", parentTask != null);
        if (parentTask != null) {
            parentTask.setBusinessStatus(status);
            parentTask.setStatus(Task.TaskStatus.COMPLETED);
            parentTask.setLastModified(org.joda.time.DateTime.now());
            parentTask.setSyncStatus(org.smartregister.repository.BaseRepository.TYPE_Unsynced);
            taskRepository.addOrUpdate(parentTask, false);
            Timber.tag("FormSaveInteractor").i("updateParentBusinessStatus: parent task %s → %s",
                    parentTaskId, status);
        }
    }

    /* ------------------------------------------------------------------ UI helpers */

    private void setSectionVisible(boolean visible) {
        int vis = visible ? View.VISIBLE : View.GONE;
        findViewById(R.id.view_divider).setVisibility(vis);
        tvSectionHeader.setVisibility(vis);
        if (visible && tvSectionHeader != null) {
            tvSectionHeader.setText(recyclerHeader != null ? recyclerHeader : "Tasks");
        }
    }

    public void showProgress(String message) {
        if (tvProgressMessage != null) tvProgressMessage.setText(message);
        if (progressOverlay != null) progressOverlay.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);
    }

    /* ------------------------------------------------------------------ extras */

    private void readExtras() {
        Intent intent       = getIntent();
        formName            = intent.getStringExtra(EXTRA_FORM_NAME);
        gateFieldKey        = intent.getStringExtra(EXTRA_GATE_FIELD_KEY);
        parentTaskId        = intent.getStringExtra(EXTRA_PARENT_TASK_ID);
        locationUUID        = intent.getStringExtra(EXTRA_LOCATION_UUID);
        childTaskCode       = intent.getStringExtra(EXTRA_CHILD_TASK_CODE);
        childFormName       = intent.getStringExtra(EXTRA_CHILD_FORM_NAME);
        businessStatusField = intent.getStringExtra(EXTRA_BUSINESS_STATUS_FIELD);
        countHint           = intent.getStringExtra(EXTRA_COUNT_HINT);
        countLabel          = intent.getStringExtra(EXTRA_COUNT_LABEL);
        recyclerHeader      = intent.getStringExtra(EXTRA_RECYCLER_HEADER);
        recyclerGateKey     = intent.getStringExtra(EXTRA_RECYCLER_GATE_KEY);
        emptyMessage        = intent.getStringExtra(EXTRA_EMPTY_MESSAGE);

        // Parse gate field values as comma-separated list (e.g. "yes_all,yes_some")
        String gateRaw = intent.getStringExtra(EXTRA_GATE_FIELD_VALUE);
        if (gateRaw == null || gateRaw.isEmpty()) {
            gateFieldValues = new HashSet<>(Collections.singletonList("yes"));
        } else {
            gateFieldValues = new HashSet<>();
            for (String v : gateRaw.split(",")) {
                gateFieldValues.add(v.trim().toLowerCase());
            }
        }

        // Parse recycler gate values as comma-separated list (e.g. "yes_all,yes_some")
        String recyclerGateRaw = intent.getStringExtra(EXTRA_RECYCLER_GATE_VALUE);
        if (recyclerGateRaw == null || recyclerGateRaw.isEmpty()) {
            recyclerGateValues = new HashSet<>(Collections.singletonList("yes"));
        } else {
            recyclerGateValues = new HashSet<>();
            for (String v : recyclerGateRaw.split(",")) {
                recyclerGateValues.add(v.trim().toLowerCase());
            }
        }
    }

    /* ------------------------------------------------------------------ default display provider */

    /**
     * Simple display provider using only Task fields — no external data needed.
     */
    private static class DefaultTaskDisplayProvider implements TaskDisplayProvider {
        @Override
        public String getPrimaryLabel(Task task) {
            return task.getDescription() != null ? task.getDescription() : task.getCode();
        }

        @Override
        public String getSecondaryLabel(Task task) {
            return task.getBusinessStatus();
        }

        @Override
        public String getActionLabel(Task task) {
            return Constants.BusinessStatus.COMPLETE.equals(task.getBusinessStatus())
                    ? "Edit" : "Record";
        }

        @Override
        public int getActionColourRes(Task task) {
            return Constants.BusinessStatus.COMPLETE.equals(task.getBusinessStatus())
                    ? R.color.pnc_circle_green : R.color.not_visited_yellow;
        }

        @Override
        public int getActionTextColourRes(Task task) {
            return android.R.color.black;
        }
    }

    /* ------------------------------------------------------------------ stub interactor */

    /**
     * Interactor that saves form JSON and updates the child task via the presenter callback.
     */
    private class FormSaveInteractor extends BaseInteractor {

        FormSaveInteractor() {
            super(new org.smartregister.reveal.contract.BaseContract.BasePresenter() {
                @Override
                public void onFormSaved(String structureId, String taskID,
                                        Task.TaskStatus taskStatus, String businessStatus,
                                        String interventionType) {
                    // Only update CHILD tasks here (not the parent).
                    // The parent task status is managed by updateParentBusinessStatus().
                    TaskRepository repo = RevealApplication.getInstance().getTaskRepository();
                    if (taskID != null && !taskID.isEmpty()) {
                        Task task = repo.getTaskByIdentifier(taskID);
                        if (task != null
                                && task.getParentTaskId() != null  // only child tasks
                                && task.getStatus() != Task.TaskStatus.CANCELLED
                                && task.getStatus() != Task.TaskStatus.ARCHIVED) {
                            task.setBusinessStatus(businessStatus != null
                                    ? businessStatus : Constants.BusinessStatus.COMPLETE);
                            task.setStatus(Task.TaskStatus.COMPLETED);
                            task.setLastModified(org.joda.time.DateTime.now());
                            // Only change to Unsynced if already Synced.
                            // If still Created (never synced to server), keep as Created
                            // so the server receives it as a new task, not an update.
                            if (org.smartregister.repository.BaseRepository.TYPE_Synced
                                .equals(task.getSyncStatus())) {
                                task.setSyncStatus(
                                    org.smartregister.repository.BaseRepository.TYPE_Unsynced);
                            }
                            repo.addOrUpdate(task, false);
                            Timber.tag("FormSaveInteractor")
                                    .i("Child task %s → %s", taskID, businessStatus);
                        } else {
                            Timber.tag("FormSaveInteractor")
                                    .i("Skipping parent task update in callback (taskID=%s)", taskID);
                        }
                    }
                    // Refresh UI and check completion
                    refreshTaskList();
                    checkAllComplete();
                }

                @Override
                public void onStructureAdded(com.mapbox.geojson.Feature f,
                                             org.json.JSONArray c, double z) {}

                @Override
                public void onFormSaveFailure(String et) {
                    Timber.tag("FormSaveInteractor").e("Save failure: %s", et);
                }

                @Override
                public void onFamilyFound(
                        org.smartregister.commonregistry.CommonPersonObjectClient c) {}
            });
        }
    }
}
