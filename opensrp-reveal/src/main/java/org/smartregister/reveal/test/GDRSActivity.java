package org.smartregister.reveal.test;

import static org.smartregister.reveal.interactor.BaseInteractor.gson;
import static org.smartregister.reveal.util.Constants.Action.RCD;
import static org.smartregister.reveal.util.Constants.Action.RCD_MEMBER;
import static org.smartregister.reveal.util.Constants.BusinessStatus.COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.INDEX_COMPLETE_RCD_INCOMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.RCD_COMPLETE_INDEX_INCOMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.RCD_INCOMPLETE_INDEX_INCOMPLETE;
import static org.smartregister.reveal.util.Constants.JSON_FORM_PARAM_JSON;
import static org.smartregister.reveal.util.Constants.JsonForm.ENCOUNTER_TYPE;
import static org.smartregister.reveal.util.Constants.JsonForm.GDRS_ADD_MEMBER;
import static org.smartregister.reveal.util.Constants.Preferences.ADMIN_PASSWORD_ENTERED;
import static org.smartregister.reveal.util.Constants.Preferences.EVENT_LATITUDE;
import static org.smartregister.reveal.util.Constants.Preferences.EVENT_LONGITUDE;
import static org.smartregister.reveal.util.Constants.Preferences.GPS_ACCURACY;
import static org.smartregister.reveal.util.Constants.Properties.TASK_IDENTIFIER;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_GET_JSON;
import static org.smartregister.reveal.util.Utils.getOperationalAreaLocation;
import static org.smartregister.util.JsonFormUtils.VALUE;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.HdssCompoundHousehold;
import org.smartregister.domain.HdssHouseholdIndividual;
import org.smartregister.domain.HdssIndividual;
import org.smartregister.domain.Location;
import org.smartregister.domain.LocationProperty;
import org.smartregister.domain.Task;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.HdssRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.BaseContract;
import org.smartregister.reveal.interactor.BaseInteractor;
import org.smartregister.reveal.model.BaseTaskDetails;
import org.smartregister.reveal.model.StructureDetails;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.GeoJsonUtils;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import org.smartregister.reveal.util.TaskUtils;
import org.smartregister.util.JsonFormUtils;


import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import timber.log.Timber;

public class GDRSActivity extends AppCompatActivity {

    public static final String INDIVIDUAL = "individual";
    public static final String INDIVIDUAL_ID = "individual_id";
    public static final String DOB = "dob";
    public static final String COMPOUND = "compound";
    public static final String STRUCTURE = "structure";
    public static final String HOUSEHOLD = "household";

    private RecyclerView recyclerView;
    private ActionAdapter actionAdapter;
    private List<Action> actionList;
    private RevealJsonFormUtils formUtils;
    @Getter
    private HdssRepository hdssRepository;
    private String taskIdentifier;
    private String locationUUID;
    private String taskCode;
    private String houseHoldId;
    private String compoundId;
    private GDRSPresenter presenter;
    private int position;
    private TaskRepository taskRepository;
    private TaskUtils taskUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        taskUtils = TaskUtils.getInstance();

        getHouseHoldData();
        // Set up toolbar
        setToolBar();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        presenter = new GDRSPresenter(this);
        populateActionList();
        recyclerView.setAdapter(actionAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getHouseHoldData() {

        List<HdssCompoundHousehold> householdAndCompound = hdssRepository.getCompoundAndHouseholdByStructureId(locationUUID);
        if (householdAndCompound != null && !householdAndCompound.isEmpty()) {

            HdssCompoundHousehold houseHoldIdAndCompoundId = householdAndCompound.get(0);

            if (houseHoldIdAndCompoundId != null) {
                if (houseHoldIdAndCompoundId.getCompoundId() != null) {
                    compoundId = houseHoldIdAndCompoundId.getCompoundId();
                }

                if (houseHoldIdAndCompoundId.getHouseholdId() != null) {
                    houseHoldId = houseHoldIdAndCompoundId.getHouseholdId();
                }
            }

        }
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        String title = houseHoldId != null ? houseHoldId.concat("-") : "";
        title = title.concat(taskCode);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
    }


    private void init() {
        hdssRepository = CoreLibrary.getInstance().context().getHdssRepository();
        formUtils = new RevealJsonFormUtils();
        setContentView(R.layout.activity_gdrs_main);
        taskRepository = CoreLibrary.getInstance().context().getTaskRepository();

        // Get the menu type from the intent
        Intent intent = getIntent();
        taskIdentifier = intent.getStringExtra(TASK_IDENTIFIER);
        locationUUID = intent.getStringExtra(Constants.Properties.LOCATION_UUID);
        taskCode = intent.getStringExtra(Constants.Properties.TASK_CODE);

    }

    public void populateActionList() {
        actionList = new ArrayList<>();
        Set<Task> tasksByStructure = hdssRepository.getTasksByStructure(locationUUID, PreferencesUtil.getInstance().getCurrentPlanId());
        Map<String, HdssIndividual> individualsByStructureId = hdssRepository.getIndividualsByStructureId(locationUUID);
        for (Task task : tasksByStructure) {
            if (individualsByStructureId.containsKey(task.getForEntity())) {

                HdssIndividual hdssIndividual = individualsByStructureId.get(task.getForEntity());
                if (hdssIndividual != null) {
                    actionList.add(new Action(hdssIndividual.getIndividualId(), hdssIndividual.getGender(), hdssIndividual.getDob(), task));
                }
            }

        }
        if (actionAdapter != null) {
            actionAdapter.setActions(actionList);
            actionAdapter.notifyDataSetChanged();
        } else {
            actionAdapter = new ActionAdapter(actionList, presenter);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gdrs_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_member) {
            JSONObject formJSON = formUtils.getFormJSON(GDRSActivity.this, GDRS_ADD_MEMBER, null, null);
            formUtils.populateFormWithServerOptions(GDRS_ADD_MEMBER, formJSON, null);
            AllSharedPreferences sharedPreferences = new AllSharedPreferences(PreferenceManager.getDefaultSharedPreferences(RevealApplication.getInstance().getApplicationContext()));
            sharedPreferences.savePreference(EVENT_LATITUDE, "");
            sharedPreferences.savePreference(EVENT_LONGITUDE, "");
            sharedPreferences.savePreference(ADMIN_PASSWORD_ENTERED, "");
            sharedPreferences.savePreference(GPS_ACCURACY, "");
            try {
                formUtils.populateField(formJSON, INDIVIDUAL_ID, houseHoldId, VALUE);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            formUtils.startJsonForm(formJSON, this);
            return true;
        } else if (item.getItemId() == R.id.gdrs_close) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ActionViewHolder> {

        @Setter
        private List<Action> actions;

        private GDRSPresenter gdrsPresenter;

        ActionAdapter(List<Action> actions, GDRSPresenter gdrsPresenter) {
            this.actions = actions;
            this.gdrsPresenter = gdrsPresenter;
        }


        @Override
        public ActionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.gdrs_item_action, parent, false);
            return new ActionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ActionViewHolder holder, int position) {
            Action action = getAction(holder, position);
            Task task = action.getTask();


            if (task.getCode().equals(Constants.Action.INDEX_CASE_MEMBER)) {

                if (COMPLETE.equals(task.getBusinessStatus())) {
                    holder.actionButton.setBackgroundColor(getResources().getColor(R.color.pnc_circle_green, null));
                    holder.actionButton.setText(R.string.edit_index_case);
                } else {
                    holder.actionButton.setBackgroundColor(getResources().getColor(R.color.cyan, null));
                    holder.actionButton.setText(R.string.action_index_case);
                }
            } else {

                if (COMPLETE.equals(task.getBusinessStatus())) {
                    holder.actionButton.setBackgroundColor(getResources().getColor(R.color.pnc_circle_green, null));
                    holder.actionButton.setText(R.string.edit_racd);
                } else {
                    holder.actionButton.setBackgroundColor(getResources().getColor(R.color.not_visited_yellow, null));
                    holder.actionButton.setText(R.string.action_racd);
                }
            }

            holder.actionButton.setOnClickListener(v -> {
                        if (!NOT_VISITED.equals(task.getBusinessStatus())) {
                            String eventType;
                            if (RCD_MEMBER.equals(task.getCode())) {
                                eventType = Constants.EventType.RCD_EVENT;
                            } else {
                                eventType = Constants.EventType.INDEX_CASE_MEMBER_EVENT;
                            }
                            gdrsPresenter.findLastEvent(task.getForEntity(), eventType);
                        } else {

                            BaseTaskDetails details = getBaseTaskDetails(task, task.getIdentifier(), null);

                            PreferencesUtil.getInstance().setSelectedHouseholdID(houseHoldId);
                            JSONObject formJSON;

                            if (task.getCode().equals(Constants.Action.INDEX_CASE_MEMBER)) {
                                formJSON = formUtils.getFormJSON(GDRSActivity.this, Constants.JsonForm.GDRS_INDEX_CASE, details, null);
                            } else {
                                formJSON = formUtils.getFormJSON(GDRSActivity.this, Constants.JsonForm.GDRS_RCD, details, null);
                            }
                            try {
                                JSONObject featureCollection = createFeatureCollection();
                                Location operationalAreaLocation = getOperationalAreaLocation(PreferencesUtil.getInstance().getCurrentOperationalArea());
                                List<Location> structures = RevealApplication.getInstance().getContext().getStructureRepository().getLocationsByParentIdForGdrs(operationalAreaLocation.getId(), "structure");

                                Map<String, StructureDetails> collect = structures.stream().map(structure -> new AbstractMap.SimpleEntry<>(structure.getId()
                                                , new StructureDetails(structure.getProperties().getName(), structure.getProperties().getName())))
                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));

                                Map<String, Set<Task>> map = new HashMap<>();

                                Set<Task> set = Set.of(task);
                                map.put(locationUUID, set);

                                String features = GeoJsonUtils
                                        .getGeoJsonFromStructuresAndTasks(structures, map, null, collect);

                                featureCollection.put(Constants.GeoJSON.FEATURES, new JSONArray(features));
                                RevealApplication.getInstance().setFeatureCollection(FeatureCollection.fromJson(featureCollection.toString()));
                                RevealApplication.getInstance().setOperationalArea(
                                        Feature.fromJson(gson.toJson(operationalAreaLocation))
                                );
                            } catch (Exception e) {
                                Toast.makeText(GDRSActivity.this, "Cannot open geo widget to capture structure", Toast.LENGTH_LONG).show();
                            }
                            formUtils.setDefaultValue(formJSON, Constants.JsonForm.HEALTH_WORKER_SUPERVISOR,
                                    RevealApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM());


                            if (task.getCode().equals(Constants.Action.INDEX_CASE_MEMBER)) {
                                formJSON = formUtils.getFormJSON(GDRSActivity.this, Constants.JsonForm.GDRS_INDEX_CASE, details, null);
                                try {
                                    formUtils.populateField(formJSON, INDIVIDUAL, action.individualId, JsonFormConstants.VALUE);
                                    formUtils.populateField(formJSON, HOUSEHOLD, houseHoldId, JsonFormConstants.VALUE);
                                    formUtils.populateField(formJSON, COMPOUND, compoundId, JsonFormConstants.VALUE);
                                    formUtils.populateField(formJSON, STRUCTURE, locationUUID, JsonFormConstants.VALUE);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }


                            } else {
                                formJSON = formUtils.getFormJSON(GDRSActivity.this, Constants.JsonForm.GDRS_RCD, details, null);
                                try {
                                    formUtils.populateField(formJSON, INDIVIDUAL, action.individualId, JsonFormConstants.VALUE);
                                    formUtils.populateField(formJSON, DOB, action.dob, JsonFormConstants.VALUE);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            formUtils.startJsonForm(formJSON, GDRSActivity.this);
                        }
                    }
            );
        }


        private JSONObject createFeatureCollection() throws JSONException {
            JSONObject featureCollection = new JSONObject();
            featureCollection.put(Constants.GeoJSON.TYPE, Constants.GeoJSON.FEATURE_COLLECTION);
            return featureCollection;
        }

        private @NonNull Action getAction(ActionViewHolder holder, int position) {
            Action action = actions.get(position);
            holder.gender.setText(action.getGender());
            holder.individualId.setText(action.getIndividualId());
            holder.dob.setText(action.getDob());
            return action;
        }


        @Override
        public int getItemCount() {
            return actions.size();
        }

        class ActionViewHolder extends RecyclerView.ViewHolder {

            TextView individualId;
            TextView gender;
            TextView dob;
            Button actionButton;

            ActionViewHolder(View itemView) {
                super(itemView);
                individualId = itemView.findViewById(R.id.individualId);
                gender = itemView.findViewById(R.id.individualGender);
                dob = itemView.findViewById(R.id.individualDob);
                actionButton = itemView.findViewById(R.id.actionButton);
            }
        }
    }


    private static class Action {
        @Getter
        private final String individualId;
        @Getter
        private final String gender;
        @Getter
        private final String dob;
        @Getter
        private final Task task;

        Action(String individualId, String gender, String dob, Task task) {
            this.individualId = individualId;
            this.gender = gender;
            this.dob = dob;
            this.task = task;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_GET_JSON && resultCode == RESULT_OK && data != null
                && data.hasExtra(JSON_FORM_PARAM_JSON)) {
            String json = data.getStringExtra(JSON_FORM_PARAM_JSON);
            Timber.d(json);
            presenter.saveJsonForm(json);

            try {
                JSONObject jsonForm = new JSONObject(json);
                String encounter = jsonForm.optString(ENCOUNTER_TYPE);
                String planId = PreferencesUtil.getInstance().getCurrentPlanId();

                if (encounter.equals("add_member") && PreferencesUtil.getInstance().getInterventionTypeForPlan(planId).equals(Constants.Intervention.SURVEY)) {

                    UUID uuid = UUID.randomUUID();
                    JSONArray fields = JsonFormUtils.fields(jsonForm);
                    JSONObject individualIdObj = JsonFormUtils.getFieldJSONObject(fields, "individual_id");
                    JSONObject genderObj = JsonFormUtils.getFieldJSONObject(fields, "gender");
                    JSONObject dobObj = JsonFormUtils.getFieldJSONObject(fields, "date_of_birth");

                    String individualId = null;
                    if (individualIdObj != null) {
                        individualId = individualIdObj.optString("value");
                    }

                    String gender = null;
                    if (genderObj != null) {
                        gender = genderObj.optString("value");
                    }

                    String dob = null;
                    if (dobObj != null) {
                        dob = dobObj.optString("value");
                    }

                    HdssHouseholdIndividual hdssHouseholdIndividual = new HdssHouseholdIndividual(houseHoldId, individualId);
                    hdssRepository.addOrUpdateHouseholdIndividual(List.of(hdssHouseholdIndividual));

                    HdssIndividual hdssIndividual = new HdssIndividual(uuid.toString(), individualId, dob, gender);
                    hdssRepository.addOrUpdateIndividual(List.of(hdssIndividual));

                    taskUtils.generateTask(this, uuid.toString(), locationUUID, NOT_VISITED, RCD_MEMBER, R.string.rcd_member);
                } else if (encounter.equals("index_case_member") && PreferencesUtil.getInstance().getInterventionTypeForPlan(planId).equals(Constants.Intervention.SURVEY)) {

                    JSONArray fields = JsonFormUtils.fields(jsonForm);
                    JSONObject individualObj = JsonFormUtils.getFieldJSONObject(fields, "individual");

                    JSONObject correctHouseholdCompoundObj = JsonFormUtils.getFieldJSONObject(fields, "household_compound");

                    if (individualObj != null && correctHouseholdCompoundObj != null) {
                        String individualIdValue = individualObj.getString("value");


                        String correctHouseholdCompoundJsonObject = correctHouseholdCompoundObj.getString("value");


                        try {
                            List<MultiSelectValue> maps = new Gson().<List<MultiSelectValue>>fromJson(
                                    correctHouseholdCompoundJsonObject,
                                    new TypeToken<List<MultiSelectValue>>() {
                                    }.getType());

                            if (maps != null && !maps.isEmpty() && maps.get(0).getKey() != null) {
                                String correctHousehold = maps.get(0).getKey();

                                if (correctHousehold != null) {
                                    hdssRepository.moveIndividualFromHouseholdToHousehold(houseHoldId, correctHousehold, individualIdValue);
                                }

                            }
                        } catch (JsonSyntaxException js) {
                            Timber.tag("jsonException").e(" err %s", js.getMessage());
                        } catch (Exception e) {
                            Timber.tag("jsonException").e(" err %s", e.getMessage());
                        }

                    }


                }
            } catch (JSONException e) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Data
    public static class MultiSelectValue implements Serializable {
        private String key;
        private String text;
        @SerializedName("openmrs_entity")
        private String openMrsEntity;
        @SerializedName("openmrs_entity_id")
        private String openMrsEntityId;
        @SerializedName("openmrs_entity_parent")

        private String openMrsEntityParent;
        private Property property;


    }

    @Data
    public static class Property implements Serializable {
        @SerializedName("presumed-id")
        private String presumedId;
        @SerializedName("confirmed-id")
        private String confirmedId;
    }

    public class GDRSPresenter implements BaseContract.BasePresenter {

        private final GDRSInteractor gdrsInteractor;

        private final GDRSActivity activity;

        public GDRSPresenter(GDRSActivity activity) {
            this.gdrsInteractor = new GDRSInteractor(this);
            this.activity = activity;
        }

        public void saveJsonForm(String json) {
            gdrsInteractor.saveJsonForm(json);
        }

        @Override
        public void onFormSaved(@NonNull String structureId, String taskID, @NonNull Task.TaskStatus taskStatus, @NonNull String businessStatus, String interventionType) {
            activity.populateActionList();

            Task task = taskRepository.getTaskByIdentifier(taskIdentifier);
            Set<Task> tasksByStructure = hdssRepository.getTasksByStructure(locationUUID, PreferencesUtil.getInstance().getCurrentPlanId());


            if (RCD.equals(task.getCode())) {
                String businessStatusIndexCase = Constants.BusinessStatus.NOT_VISITED;
                boolean allRCDComplete = false;
                boolean allRCDInComplete = false;

                allRCDComplete = tasksByStructure.stream()
                        .allMatch(innerTask -> COMPLETE.equals(innerTask.getBusinessStatus()));

                allRCDInComplete = tasksByStructure.stream()
                        .allMatch(innerTask -> NOT_VISITED.equals(innerTask.getBusinessStatus()));

                if (allRCDComplete) {
                    businessStatusIndexCase = COMPLETE;
                } else if (!allRCDInComplete) {
                    businessStatusIndexCase = Constants.BusinessStatus.RCD_PARTIALLY_COMPLETE;
                } else {
                    businessStatusIndexCase = Constants.BusinessStatus.NOT_VISITED;
                }
                task.setBusinessStatus(businessStatusIndexCase);
                task.setStatus(Task.TaskStatus.COMPLETED);
                task.setLastModified(new DateTime());
                taskRepository.addOrUpdate(task);
            } else {

                String businessStatusIndexCase = Constants.BusinessStatus.INDEX_CASE_NOT_VISITED;
                boolean allIndexCaseComplete = false;
                boolean allRCDComplete = false;
                boolean allRCDInComplete = false;

                allIndexCaseComplete = tasksByStructure.stream().filter(innerTask -> Constants.Action.INDEX_CASE_MEMBER.equals(innerTask.getCode()))
                        .allMatch(innerTask -> COMPLETE.equals(innerTask.getBusinessStatus()));

                allRCDComplete = tasksByStructure.stream().filter(innerTask -> RCD_MEMBER.equals(innerTask.getCode()))
                        .allMatch(innerTask -> COMPLETE.equals(innerTask.getBusinessStatus()));

                allRCDInComplete = tasksByStructure.stream().filter(innerTask -> RCD_MEMBER.equals(innerTask.getCode()))
                        .anyMatch(innerTask -> Constants.BusinessStatus.NOT_VISITED.equals(innerTask.getBusinessStatus()));


                businessStatusIndexCase = Constants.BusinessStatus.NOT_VISITED;

                if (allIndexCaseComplete) {
                    if (allRCDComplete) {
                        businessStatusIndexCase = COMPLETE;
                    } else if (allRCDInComplete) {
                        businessStatusIndexCase = INDEX_COMPLETE_RCD_INCOMPLETE;
                    }
                } else {
                    if (allRCDComplete) {
                        businessStatusIndexCase = RCD_COMPLETE_INDEX_INCOMPLETE;
                    } else {
                        businessStatusIndexCase = RCD_INCOMPLETE_INDEX_INCOMPLETE;
                    }
                }
                task.setBusinessStatus(businessStatusIndexCase);
                task.setStatus(Task.TaskStatus.COMPLETED);
                task.setLastModified(new DateTime());
                taskRepository.addOrUpdate(task);
            }

        }

        @Override
        public void onStructureAdded(Feature feature, JSONArray featureCoordinates, double zoomlevel) {

        }

        @Override
        public void onFormSaveFailure(String eventType) {
            Toast.makeText(GDRSActivity.this, "Failure to save form data", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFamilyFound(CommonPersonObjectClient finalFamily) {

        }

        public void findLastEvent(String baseEntityId, String eventType) {
            gdrsInteractor.findLastEvent(baseEntityId, eventType);
        }

    }

    public class GDRSInteractor extends BaseInteractor {

        public GDRSInteractor(BaseContract.BasePresenter presenter) {
            super(presenter);
        }

        @Override
        public void handleLasteventFound(org.smartregister.domain.Event event) {

            if (event != null) {

                String taskID = event.getDetails().get(Constants.Properties.TASK_IDENTIFIER);

                Task task = taskRepository.getTaskByIdentifier(taskID);

                HdssLocation location = getHdssLocation(locationUUID);

                BaseTaskDetails details = getBaseTaskDetails(task, task.getIdentifier(), locationUUID);
                PreferencesUtil.getInstance().setSelectedHouseholdID(houseHoldId);
                JSONObject formJSON;

                if (task.getCode().equals(Constants.Action.INDEX_CASE_MEMBER)) {
                    formJSON = formUtils.getFormJSON(GDRSActivity.this, Constants.JsonForm.GDRS_INDEX_CASE, details, location);
                } else {
                    formJSON = formUtils.getFormJSON(GDRSActivity.this, Constants.JsonForm.GDRS_RCD, details, location);
                }

                formUtils.populateForm(event, formJSON);
                formUtils.startJsonForm(formJSON, GDRSActivity.this);

            }


        }


    }

    private static @NonNull HdssLocation getHdssLocation(String locationUUID) {
        HdssLocation location = new HdssLocation();
        LocationProperty locationProperty = new LocationProperty();
        locationProperty.setType(Constants.StructureType.RESIDENTIAL);
        locationProperty.setUid(locationUUID);
        locationProperty.setVersion(0);
        location.setProperties(locationProperty);
        location.setId(locationUUID);
        return location;
    }

    private static @NonNull BaseTaskDetails getBaseTaskDetails(Task task, String taskIdentifier, String locationUUID) {
        BaseTaskDetails details = new BaseTaskDetails(taskIdentifier);
        details.setStructureId(locationUUID);
        details.setTaskCode(task.getCode());
        details.setTaskId(task.getIdentifier());
        details.setTaskStatus(task.getStatus().name());
        details.setBusinessStatus(task.getBusinessStatus());
        details.setTaskEntity(task.getForEntity());
        return details;
    }

}

class HdssLocation extends Location {

}