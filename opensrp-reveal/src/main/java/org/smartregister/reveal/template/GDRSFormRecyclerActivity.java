package org.smartregister.reveal.template;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.HdssCompoundHousehold;
import org.smartregister.domain.HdssIndividual;
import org.smartregister.domain.Task;
import org.smartregister.repository.HdssRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.reveal.R;
import org.smartregister.reveal.model.BaseTaskDetails;
import org.smartregister.reveal.test.GDRSActivity;
import org.smartregister.reveal.test.GDRSPresenter;
import org.smartregister.reveal.test.HdssLocation;
import org.smartregister.reveal.test.HdssTask;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import org.smartregister.reveal.util.TaskUtils;
import org.smartregister.reveal.application.RevealApplication;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import timber.log.Timber;

/**
 * Concrete GDRS implementation of {@link FormRecyclerTemplate}.
 *
 * <p>Slot 1 (form):     the parent task form (Index Case / RCD / Secondary Index Case),
 *                       driven by the form name passed via {@link #EXTRA_PARENT_FORM_NAME}.
 * <p>Slot 2 (recycler): member sub-tasks for the selected structure, fetched from
 *                       {@link HdssRepository} and presented via {@link GDRSTaskDisplayProvider}.
 *
 * <h3>Intent extras</h3>
 * <ul>
 *   <li>{@link #EXTRA_PARENT_FORM_NAME}  – asset path of the parent form JSON</li>
 *   <li>{@link Constants.Properties#LOCATION_UUID} – structure UUID</li>
 *   <li>{@link Constants.Properties#TASK_IDENTIFIER} – parent task identifier</li>
 *   <li>{@link Constants.Properties#TASK_CODE}       – parent task code (RCD / Index Case / ...)</li>
 * </ul>
 *
 * <h3>GDRS-specific behaviour preserved</h3>
 * <ul>
 *   <li>Menu items: Add RCD Member, Add Manual RCD Member</li>
 *   <li>onActivityResult: handles sub-task forms returned from {@link RevealJsonFormUtils#startJsonForm}</li>
 *   <li>Parent task business-status rollup after each sub-task save</li>
 *   <li>Sub-task form population (individual ID, household, compound, structure, etc.)</li>
 * </ul>
 */
public class GDRSFormRecyclerActivity extends FormRecyclerTemplate {

    /* ------------------------------------------------------------------ intent extra keys */
    public static final String EXTRA_PARENT_FORM_NAME = "parentFormName";

    /* ------------------------------------------------------------------ state */
    private String locationUUID;
    private String taskIdentifier;
    private String taskCode;
    private String compoundId;
    private Set<String> householdIds = new HashSet<>();

    private HdssRepository   hdssRepository;
    private TaskRepository   taskRepository;
    private RevealJsonFormUtils formUtils;
    private TaskUtils        taskUtils;

    /** Cached individual map — fetched once, reused by display provider */
    private Map<String, HdssIndividual> individualMap;

    /* ------------------------------------------------------------------ lifecycle */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Read extras BEFORE super.onCreate() because getParentFormJson() is
        // called during super.onCreate() → setContentView → initializeFormFragment chain.
        readExtras();
        initRepositories();

        super.onCreate(savedInstanceState);
    }

    /* ================================================================== FormRecyclerTemplate contract */

    /**
     * Loads the raw parent form JSON from assets.
     * Called on the main thread before super.onCreate() — must be fast.
     * Asset reads are acceptable here (APK file I/O, no DB).
     * No field population — that belongs in onParentFormReady().
     */
    @Override
    protected String getParentFormJson() {
        String formName = getIntent().getStringExtra(EXTRA_PARENT_FORM_NAME);
        if (formName == null) {
            Timber.tag("GDRS").w("No parentFormName extra provided");
            // Return a minimal valid stub so JsonFormBaseActivity.init() succeeds.
            // encounter_type is required — without it init() throws before setting
            // localBroadcastManager, causing an NPE in onResume.
            return "{\"encounter_type\":\"placeholder\",\"count\":\"1\","
                    + "\"step1\":{\"title\":\"\",\"fields\":[]}}";
        }
        // Load raw form from assets — no details/location pre-population yet.
        JSONObject formJSON = formUtils.getFormJSON(this, formName, null, null);
        return formJSON != null ? formJSON.toString()
                : "{\"encounter_type\":\"placeholder\",\"count\":\"1\","
                + "\"step1\":{\"title\":\"\",\"fields\":[]}}";
    }

    /**
     * Runs on the disk thread. Resolves compound/task data, loads the populated
     * form JSON, and calls {@link #setmJSONObject(JSONObject)} so the fragment
     * renders pre-filled values when it is committed on the main thread.
     */
    @Override
    protected void onParentFormReady() {
        resolveCompoundData();

        String formName = getIntent().getStringExtra(EXTRA_PARENT_FORM_NAME);
        if (formName == null) return;

        Task parentTask = taskRepository.getTaskByIdentifier(taskIdentifier);
        BaseTaskDetails details = parentTask != null
                ? GDRSActivity.getBaseTaskDetails(parentTask, taskIdentifier, locationUUID)
                : null;
        HdssLocation location = GDRSActivity.getHdssLocation(locationUUID);

        JSONObject formJSON = formUtils.getFormJSON(this, formName, details, location);
        if (formJSON == null) return;

        try {
            formUtils.populateField(formJSON, GDRSActivity.STRUCTURE, locationUUID,
                    JsonFormConstants.VALUE);
            formUtils.setDefaultValue(formJSON,
                    Constants.JsonForm.HEALTH_WORKER_SUPERVISOR,
                    RevealApplication.getInstance().getContext()
                            .allSharedPreferences().fetchRegisteredANM());

            // Update mJSONObject with the fully populated form so the fragment
            // renders pre-filled values when committed on the main thread.
            setmJSONObject(formJSON);

        } catch (JSONException e) {
            Timber.tag("GDRS").e(e, "onParentFormReady population error");
        }
    }

    /**
     * Fetches GDRS sub-tasks (member tasks) for the current structure,
     * joins them with individual data, and returns {@link TaskRowItem}s.
     * Called on the disk thread.
     */
    @Override
    protected void loadTaskItems(TaskItemsCallback callback) {
        List<TaskRowItem> items = new ArrayList<>();

        try {
            String planId = PreferencesUtil.getInstance().getCurrentPlanId();

            Set<HdssTask> tasksByStructure =
                    hdssRepository.getTasksByStructure(locationUUID, planId);

            // Cache individual map for the display provider
            individualMap = hdssRepository.getIndividualsByStructureId(locationUUID);

            Set<HdssTask> sorted = tasksByStructure.stream()
                    .sorted(Comparator.comparing(HdssTask::getIndividualId))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            GDRSTaskDisplayProvider provider = new GDRSTaskDisplayProvider(individualMap);

            for (HdssTask hdssTask : sorted) {
                Task task = taskRepository.getTaskByIdentifier(hdssTask.getIdentifier());
                if (task == null) continue;

                String createdDate = hdssTask.getAuthoredOn() != null
                        ? hdssTask.getAuthoredOn().toString("yyyy-MM-dd")
                        : "";

                TaskRowItem item = TaskRowItem.from(task, provider, createdDate);
                items.add(item);
            }
        } catch (Exception e) {
            Timber.tag("GDRS").e(e, "Error loading task items");
        }

        callback.onItemsLoaded(items);
    }

    /**
     * Saves the parent form JSON via {@link org.smartregister.reveal.test.GDRSInteractor}.
     * Called on the main thread after the embedded form passes validation.
     */
    @Override
    protected void onFormSaved(String json) {
        showOverlayProgress("Saving…");
        appExecutors.diskIO().execute(() -> {
            // GDRSInteractor extends BaseInteractor — saveJsonForm handles
            // all encounter types (rcd, index_case_member, etc.) generically.
            GDRSFormInteractor interactor =
                    new GDRSFormInteractor(locationUUID, compoundId);
            interactor.saveJsonForm(json);
            appExecutors.mainThread().execute(this::hideOverlayProgress);
        });
    }

    /**
     * Opens the correct GDRS form for the tapped sub-task.
     *
     * - NOT_VISITED task  → open blank form pre-populated with individual data
     * - Already completed → find the last saved event and open pre-filled form
     */
    @Override
    protected void onTaskTapped(Task task) {
        if (Constants.BusinessStatus.NOT_VISITED.equals(task.getBusinessStatus())) {
            openNotVisitedSubTask(task);
        } else {
            openCompletedSubTask(task);
        }
    }

    /** Opens a blank sub-task form pre-populated from individual data. */
    private void openNotVisitedSubTask(Task task) {
        appExecutors.diskIO().execute(() -> {
            BaseTaskDetails details =
                    GDRSActivity.getBaseTaskDetails(task, task.getIdentifier(), locationUUID);
            HdssLocation location = GDRSActivity.getHdssLocation(locationUUID);

            JSONObject formJSON = resolveSubTaskForm(task, details, location);
            if (formJSON == null) return;

            populateSubTaskForm(task, formJSON);

            appExecutors.mainThread().execute(() ->
                    formUtils.startJsonForm(formJSON, this,
                            Constants.RequestCode.REQUEST_CODE_GET_JSON));
        });
    }

    /** Finds the last saved event for a completed sub-task and opens the pre-filled form. */
    private void openCompletedSubTask(Task task) {
        String eventType;
        switch (task.getCode()) {
            case Constants.Action.INDEX_CASE_MEMBER:
                eventType = Constants.EventType.INDEX_CASE_MEMBER_EVENT;
                break;
            case Constants.Action.SECONDARY_INDEX_CASE_MEMBER:
                eventType = Constants.EventType.SECONDARY_INDEX_CASE_MEMBER_EVENT;
                break;
            case Constants.Action.RCD_MEMBER:
            default:
                eventType = Constants.EventType.RCD_EVENT;
        }

        // findLastEvent runs on disk thread internally via BaseInteractor
        GDRSFormInteractor interactor =
                new GDRSFormInteractor(this, locationUUID, compoundId);
        interactor.findLastEvent(task.getForEntity(), eventType);
    }

    /**
     * Shows reset dialog for long-pressed sub-task.
     */
    @Override
    protected void onTaskLongPressed(Task task) {
        // Reuse existing reset logic — show a simple confirm dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reset Task")
                .setMessage("Reset this task to Not Visited?")
                .setPositiveButton("Reset", (d, w) -> {
                    showOverlayProgress("Resetting…");
                    appExecutors.diskIO().execute(() -> {
                        resetSubTask(task);
                        appExecutors.mainThread().execute(() -> {
                            hideOverlayProgress();
                            refreshTaskList();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected TaskDisplayProvider getTaskDisplayProvider() {
        // individualMap may be null on first call (before loadTaskItems runs).
        // Return a provider that handles null gracefully.
        return new GDRSTaskDisplayProvider(
                individualMap != null ? individualMap : new java.util.HashMap<>());
    }

    @Override
    protected String getSectionHeaderLabel() {
        return "Member Tasks";
    }

    /* ------------------------------------------------------------------ menu (Add Member) */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gdrs_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_rcd_member) {
            openAddMemberForm(Constants.JsonForm.GDRS_ADD_MEMBER);
            return true;
        } else if (item.getItemId() == R.id.action_add_manual_rcd_member) {
            openAddMemberForm(Constants.JsonForm.GDRS_MANUALLY_ADD_MEMBER);
            return true;
        } else if (item.getItemId() == R.id.gdrs_close) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* ------------------------------------------------------------------ sub-task form result */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RequestCode.REQUEST_CODE_GET_JSON
                && resultCode == RESULT_OK
                && data != null
                && data.hasExtra(Constants.JSON_FORM_PARAM_JSON)) {

            String json = data.getStringExtra(Constants.JSON_FORM_PARAM_JSON);
            showOverlayProgress("Saving Task…");

            appExecutors.diskIO().execute(() -> {
                GDRSFormInteractor interactor =
                        new GDRSFormInteractor(locationUUID, compoundId);
                interactor.saveJsonForm(json);
                appExecutors.mainThread().execute(() -> {
                    hideOverlayProgress();
                    refreshTaskList();
                });
            });
        }
    }

    /* ================================================================== private helpers */

    private void readExtras() {
        Intent intent   = getIntent();
        locationUUID    = intent.getStringExtra(Constants.Properties.LOCATION_UUID);
        taskIdentifier  = intent.getStringExtra(Constants.Properties.TASK_IDENTIFIER);
        taskCode        = intent.getStringExtra(Constants.Properties.TASK_CODE);
    }

    private void initRepositories() {
        hdssRepository = CoreLibrary.getInstance().context().getHdssRepository();
        taskRepository = CoreLibrary.getInstance().context().getTaskRepository();
        formUtils      = new RevealJsonFormUtils();
        taskUtils      = TaskUtils.getInstance();
    }

    /**
     * Resolves compoundId and householdIds from the structure UUID.
     * Must be called on whichever thread calls {@link #getParentFormJson()}.
     */
    private void resolveCompoundData() {
        List<HdssCompoundHousehold> compoundData =
                hdssRepository.getCompoundAndHouseholdListByStructureId(locationUUID);
        if (compoundData != null && !compoundData.isEmpty()) {
            HdssCompoundHousehold first = compoundData.get(0);
            if (first != null && first.getCompoundId() != null) {
                compoundId = first.getCompoundId();
            }
            householdIds = compoundData.stream()
                    .map(HdssCompoundHousehold::getHouseholdId)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Returns the appropriate form JSON for a sub-task based on its code.
     * Mirrors the logic in {@link org.smartregister.reveal.test.ActionAdapter}.
     */
    @Nullable
    private JSONObject resolveSubTaskForm(Task task, BaseTaskDetails details,
                                          HdssLocation location) {
        switch (task.getCode()) {
            case Constants.Action.SECONDARY_INDEX_CASE_MEMBER:
                return formUtils.getFormJSON(this,
                        Constants.JsonForm.GDRS_SECONDARY_INDEX_CASE, details, location);
            case Constants.Action.INDEX_CASE_MEMBER:
                return formUtils.getFormJSON(this,
                        Constants.JsonForm.GDRS_INDEX_CASE, details, location);
            case Constants.Action.RCD_MEMBER:
            default:
                return formUtils.getFormJSON(this,
                        Constants.JsonForm.GDRS_RCD, details, location);
        }
    }

    /**
     * Pre-populates sub-task form fields from the individual/task data.
     */
    private void populateSubTaskForm(Task task, JSONObject formJSON) {
        try {
            HdssIndividual individual = individualMap != null
                    ? individualMap.get(task.getForEntity())
                    : null;

            if (individual != null) {
                formUtils.populateField(formJSON,
                        GDRSActivity.INDIVIDUAL, individual.getIndividualId(),
                        JsonFormConstants.VALUE);
                formUtils.populateField(formJSON,
                        GDRSActivity.NAME, individual.getName(),
                        JsonFormConstants.VALUE);
                formUtils.populateField(formJSON,
                        GDRSActivity.DOB, individual.getDob(),
                        JsonFormConstants.VALUE);
                formUtils.populateField(formJSON,
                        GDRSActivity.GENDER, individual.getGender(),
                        JsonFormConstants.VALUE);
            }

            // Fields common to index case / secondary forms
            if (Constants.Action.INDEX_CASE_MEMBER.equals(task.getCode())
                    || Constants.Action.SECONDARY_INDEX_CASE_MEMBER.equals(task.getCode())) {
                formUtils.populateField(formJSON,
                        GDRSActivity.HOUSEHOLD, task.getHouseholdId(),
                        JsonFormConstants.VALUE);
                formUtils.populateField(formJSON,
                        GDRSActivity.COMPOUND, compoundId,
                        JsonFormConstants.VALUE);
                formUtils.populateField(formJSON,
                        GDRSActivity.STRUCTURE, locationUUID,
                        JsonFormConstants.VALUE);
                formUtils.populateFieldWithOperationalAreas(formJSON,
                        GDRSActivity.OPERATIONAL);
            }

            // setDefaultValue does not guard against missing fields — check first
            safeSetDefaultValue(formJSON, GDRSActivity.DATE,
                    org.joda.time.LocalDate.now().toString("dd-MM-yyyy"));

        } catch (JSONException e) {
            Timber.tag("GDRS").e(e, "Error populating sub-task form");
        }
    }

    /**
     * Calls {@link RevealJsonFormUtils#setDefaultValue} only if the field key
     * exists in the form. Prevents NPE when a field is absent in a particular form.
     */
    private void safeSetDefaultValue(JSONObject formJSON, String fieldKey, String value) {
        org.json.JSONObject field = org.smartregister.util.JsonFormUtils
                .getFieldJSONObject(
                        org.smartregister.util.JsonFormUtils.fields(formJSON), fieldKey);
        if (field != null) {
            formUtils.setDefaultValue(formJSON, fieldKey, value);
        }
    }

    /**
     * Opens an Add Member form (Add or Manual Add).
     */
    private void openAddMemberForm(String formName) {
        JSONObject formJSON = formUtils.getFormJSON(this, formName, null, null);
        formUtils.populateFormWithServerOptions(formName, formJSON, null);

        try {
            List<Pair<String, String>> pairs = new ArrayList<>();
            for (String hId : householdIds) {
                pairs.add(new Pair<>(hId, hId));
            }
            formUtils.populateSpinner(formJSON, Constants.Properties.HOUSEHOLD_ID, pairs);
        } catch (JSONException e) {
            Timber.tag("GDRS").e(e, "populateSpinner error");
        }

        formUtils.startJsonForm(formJSON, this);
    }

    /**
     * Resets a completed sub-task back to NOT_VISITED.
     * Must be called on the disk thread.
     */
    private void resetSubTask(Task task) {
        taskRepository.cancelCompletedTaskByIdentifier(task.getIdentifier());

        int descRes;
        switch (task.getCode()) {
            case Constants.Action.INDEX_CASE_MEMBER:
            case Constants.Action.SECONDARY_INDEX_CASE_MEMBER:
                descRes = R.string.index_case;
                break;
            default:
                descRes = R.string.rcd_member;
        }

        taskUtils.generateTask(
                this,
                task.getForEntity(),
                task.getStructureId(),
                Constants.BusinessStatus.NOT_VISITED,
                task.getCode(),
                descRes);

        // Recalculate parent task status
        recalculateParentStatus();
    }

    /**
     * Recalculates and persists parent task business status based on current sub-task states.
     * Mirrors the rollup logic in {@link GDRSPresenter#onFormSaved}.
     * Must be called on the disk thread.
     */
    private void recalculateParentStatus() {
        try {
            Task parentTask = taskRepository.getTaskByIdentifier(taskIdentifier);
            if (parentTask == null
                    || Task.TaskStatus.CANCELLED.equals(parentTask.getStatus())) return;

            String planId = PreferencesUtil.getInstance().getCurrentPlanId();
            Set<HdssTask> subTasks = hdssRepository.getTasksByStructure(locationUUID, planId);

            String newStatus = deriveParentStatus(parentTask.getCode(), subTasks);
            parentTask.setBusinessStatus(newStatus);
            parentTask.setStatus(Task.TaskStatus.COMPLETED);
            parentTask.setLastModified(new DateTime());
            taskRepository.add(parentTask);

            Timber.tag("GDRS").i("Parent %s → %s", taskIdentifier, newStatus);
        } catch (Exception e) {
            Timber.tag("GDRS").e(e, "recalculateParentStatus error");
        }
    }

    private String deriveParentStatus(String code, Set<HdssTask> subTasks) {
        switch (code) {
            case Constants.Action.RCD: {
                boolean allComplete = subTasks.stream()
                        .allMatch(t -> Constants.BusinessStatus.COMPLETE
                                .equals(t.getBusinessStatus()));
                boolean allNotVisited = subTasks.stream()
                        .allMatch(t -> Constants.BusinessStatus.NOT_VISITED
                                .equals(t.getBusinessStatus()));
                if (allComplete)   return Constants.BusinessStatus.COMPLETE;
                if (allNotVisited) return Constants.BusinessStatus.NOT_VISITED;
                return Constants.BusinessStatus.RCD_PARTIALLY_COMPLETE;
            }
            case Constants.Action.INDEX_CASE: {
                boolean indexDone = subTasks.stream()
                        .filter(t -> Constants.Action.INDEX_CASE_MEMBER.equals(t.getCode()))
                        .allMatch(t -> Constants.BusinessStatus.COMPLETE
                                .equals(t.getBusinessStatus()));
                boolean rcdDone = subTasks.stream()
                        .filter(t -> Constants.Action.RCD_MEMBER.equals(t.getCode()))
                        .allMatch(t -> Constants.BusinessStatus.COMPLETE
                                .equals(t.getBusinessStatus()));
                if (indexDone && rcdDone) return Constants.BusinessStatus.COMPLETE;
                if (indexDone)           return Constants.BusinessStatus.INDEX_CASE_COMPLETE;
                return Constants.BusinessStatus.INDEX_CASE_NOT_VISITED;
            }
            case Constants.Action.SECONDARY_INDEX_CASE: {
                boolean secDone = subTasks.stream()
                        .filter(t -> Constants.Action.SECONDARY_INDEX_CASE_MEMBER
                                .equals(t.getCode()))
                        .allMatch(t -> Constants.BusinessStatus.COMPLETE
                                .equals(t.getBusinessStatus()));
                boolean rcdDone = subTasks.stream()
                        .filter(t -> Constants.Action.RCD_MEMBER.equals(t.getCode()))
                        .allMatch(t -> Constants.BusinessStatus.COMPLETE
                                .equals(t.getBusinessStatus()));
                if (secDone && rcdDone) return Constants.BusinessStatus.COMPLETE;
                if (secDone)           return Constants.BusinessStatus.SECONDARY_INDEX_CASE_COMPLETE;
                return Constants.BusinessStatus.SECONDARY_INDEX_CASE_NOT_VISITED;
            }
            default:
                return Constants.BusinessStatus.NOT_VISITED;
        }
    }
}
