package org.smartregister.reveal.test;

import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE;
import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE_MEMBER;
import static org.smartregister.reveal.util.Constants.Action.RCD;
import static org.smartregister.reveal.util.Constants.Action.RCD_MEMBER;
import static org.smartregister.reveal.util.Constants.Action.SECONDARY_INDEX_CASE;
import static org.smartregister.reveal.util.Constants.Action.SECONDARY_INDEX_CASE_MEMBER;
import static org.smartregister.reveal.util.Constants.BusinessStatus.COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.INDEX_CASE_COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.INDEX_CASE_NOT_VISITED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.SECONDARY_INDEX_CASE_COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.SECONDARY_INDEX_CASE_NOT_VISITED;
import static org.smartregister.reveal.util.Constants.JSON_FORM_PARAM_JSON;
import static org.smartregister.reveal.util.Constants.JsonForm.ENCOUNTER_TYPE;
import static org.smartregister.reveal.util.Constants.JsonForm.GDRS_ADD_MEMBER;
import static org.smartregister.reveal.util.Constants.JsonForm.GDRS_MANUALLY_ADD_MEMBER;
import static org.smartregister.reveal.util.Constants.Preferences.ADMIN_PASSWORD_ENTERED;
import static org.smartregister.reveal.util.Constants.Preferences.EVENT_LATITUDE;
import static org.smartregister.reveal.util.Constants.Preferences.EVENT_LONGITUDE;
import static org.smartregister.reveal.util.Constants.Preferences.GPS_ACCURACY;
import static org.smartregister.reveal.util.Constants.Properties.HOUSEHOLD_ID;
import static org.smartregister.reveal.util.Constants.Properties.TASK_IDENTIFIER;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_GET_JSON;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.HdssCompoundHousehold;
import org.smartregister.domain.HdssHousehold;
import org.smartregister.domain.HdssHouseholdIndividual;
import org.smartregister.domain.HdssHouseholdStructure;
import org.smartregister.domain.HdssIndividual;
import org.smartregister.domain.IndividualsAndTasksForCompound;
import org.smartregister.domain.LocationProperty;
import org.smartregister.domain.StructureTaskForCompound;
import org.smartregister.domain.Task;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.HdssRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.model.BaseTaskDetails;
import org.smartregister.reveal.util.AppExecutors;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import org.smartregister.reveal.util.TaskUtils;
import org.smartregister.util.JsonFormUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Getter;
import timber.log.Timber;

public class GDRSActivity extends AppCompatActivity {

  public static final String INDIVIDUAL = "individual";
  public static final String INDIVIDUAL_ID = "individual_id";
  public static final String DOB = "dob";
  public static final String COMPOUND = "compound";
  public static final String STRUCTURE = "structure";
  public static final String HOUSEHOLD = "household";
  public static final String GENDER = "gender";
  public static final String GENDER_CAPTURE = "gender_capture";

  public static final String NAME = "name";
  public static final String DATE = "date";
  public static final String OPERATIONAL = "operational";
  public static final String ADD_MEMBER = "add_member";

  private RecyclerView recyclerView;
  @Getter @Setter private ActionAdapter actionAdapter;
  private List<Action> actionList;
  @Getter private RevealJsonFormUtils formUtils;
  @Getter private HdssRepository hdssRepository;
  private String taskIdentifier;
  private String locationUUID;
  private String taskCode;
  private Set<String> houseHoldIds = new HashSet<>();
  private String thisCompoundId;
  @Getter private GDRSPresenter presenter;
  private int position;
  @Getter private TaskRepository taskRepository;
  @Getter private TaskUtils taskUtils;
  private LocationRepository locationRepository;
  @Getter protected AppExecutors appExecutors;

  private LinearLayout progressBar;

  private MenuItem addMemberItem;

  private MenuItem addManualMemberItem;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    init();

    taskUtils = TaskUtils.getInstance();
    appExecutors = RevealApplication.getInstance().getAppExecutors();
    getHouseHoldData();
    // Set up toolbar
    setToolBar();
    recyclerView = findViewById(R.id.recyclerView);
    progressBar = findViewById(R.id.progressBar);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    setupSearchfield();
    setupClearButton();
    DividerItemDecoration dividerItemDecoration =
        new DividerItemDecoration(
            recyclerView.getContext(),
            ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation());
    recyclerView.addItemDecoration(dividerItemDecoration);

    presenter = new GDRSPresenter(this, thisCompoundId, locationUUID, taskIdentifier);

  }

  public void populateList(String loadMessage) {
    populateActionList(loadMessage);
  }

  public JSONObject createFeatureCollection() throws JSONException {
    JSONObject featureCollection = new JSONObject();
    featureCollection.put(Constants.GeoJSON.TYPE, Constants.GeoJSON.FEATURE_COLLECTION);
    return featureCollection;
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  private void getHouseHoldData() {

    List<HdssCompoundHousehold> householdAndCompound =
        hdssRepository.getCompoundAndHouseholdListByStructureId(locationUUID);
    if (householdAndCompound != null && !householdAndCompound.isEmpty()) {

      HdssCompoundHousehold houseHoldIdAndCompoundId = householdAndCompound.get(0);

      if (houseHoldIdAndCompoundId != null) {
        if (houseHoldIdAndCompoundId.getCompoundId() != null) {
          thisCompoundId = houseHoldIdAndCompoundId.getCompoundId();
        }
      }
      houseHoldIds =
          householdAndCompound.stream()
              .map(HdssCompoundHousehold::getHouseholdId)
              .collect(Collectors.toSet());
    }
  }
  private void setupClearButton(){
    Button clearButton = findViewById(R.id.clearSearchButton);
    EditText editText = findViewById(R.id.searchField);
    clearButton.setOnClickListener(v -> {
      editText.setText("");
      filterList("");
    });
  }
  private void setupSearchfield(){
    EditText editText = findViewById(R.id.searchField);

    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        filterList(s.toString());
        Button clearButton = findViewById(R.id.clearSearchButton);
        clearButton.setEnabled(!s.toString().isEmpty());
      }
    });
  }
  public void filterList(String query) {
    if (!query.isEmpty()) {
      List<Action> filtered = new ArrayList<>();
      for (Action action : actionAdapter.getFullactions()) {
        // You can filter based on any fields of the Action object
        if (action.getName().toLowerCase().contains(query.toLowerCase())) {
          filtered.add(action);
        }
        if (action.getIndividualId().toLowerCase().contains(query.toLowerCase())){
          filtered.add(action);
        }
        if (action.getHouseholdId().toLowerCase().contains(query.toLowerCase())){
          filtered.add(action);
        }
        Timber.tag("Reveal").i("results %s",filtered);
      }
      actionList = filtered;
    } else {
      actionList = actionAdapter.getFullactions();
    }
    actionAdapter.setActions(actionList);
    actionAdapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
  }
  private void setToolBar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    TextView viewLeft = toolbar.findViewById(R.id.textViewLeft);
    viewLeft.setText(taskCode);
    TextView viewCenter = toolbar.findViewById(R.id.textViewCenter);

    viewCenter.setText("Compound: ".concat(thisCompoundId));

    TextView textViewBottom = findViewById(R.id.textViewBottom);
    textViewBottom.setText(String.join(" / ", houseHoldIds));
    setSupportActionBar(toolbar);
  }

  private void init() {
    hdssRepository = CoreLibrary.getInstance().context().getHdssRepository();
    locationRepository = CoreLibrary.getInstance().context().getLocationRepository();
    formUtils = new RevealJsonFormUtils();
    setContentView(R.layout.activity_gdrs_main);
    taskRepository = CoreLibrary.getInstance().context().getTaskRepository();

    // Get the menu type from the intent
    Intent intent = getIntent();
    taskIdentifier = intent.getStringExtra(TASK_IDENTIFIER);
    locationUUID = intent.getStringExtra(Constants.Properties.LOCATION_UUID);
    taskCode = intent.getStringExtra(Constants.Properties.TASK_CODE);
  }

  public void copyToClipboard(String text) {
    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("Copied Text", text);
    clipboard.setPrimaryClip(clip);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.gdrs_menu, menu);

    this.addMemberItem = menu.findItem(R.id.action_add_rcd_member);

    this.addManualMemberItem = menu.findItem(R.id.action_add_manual_rcd_member);

    populateList("Loading Tasks...");

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_add_rcd_member) {
      JSONObject formJSON = formUtils.getFormJSON(GDRSActivity.this, GDRS_ADD_MEMBER, null, null);
      formUtils.populateFormWithServerOptions(GDRS_ADD_MEMBER, formJSON, null);
      AllSharedPreferences sharedPreferences =
          new AllSharedPreferences(
              PreferenceManager.getDefaultSharedPreferences(
                  RevealApplication.getInstance().getApplicationContext()));
      sharedPreferences.savePreference(EVENT_LATITUDE, "");
      sharedPreferences.savePreference(EVENT_LONGITUDE, "");
      sharedPreferences.savePreference(ADMIN_PASSWORD_ENTERED, "");
      sharedPreferences.savePreference(GPS_ACCURACY, "");
      try {
        List<Pair<String, String>> householdPairs = new ArrayList<>();
        for (String householdId : houseHoldIds) {
          householdPairs.add(new Pair<>(householdId, householdId));
        }

        formUtils.populateSpinner(formJSON, HOUSEHOLD_ID, householdPairs);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      formUtils.startJsonForm(formJSON, this);
      return true;
    }  else if (item.getItemId() == R.id.action_add_manual_rcd_member) {
      JSONObject formJSON = formUtils.getFormJSON(GDRSActivity.this, GDRS_MANUALLY_ADD_MEMBER, null, null);
      formUtils.populateFormWithServerOptions(GDRS_MANUALLY_ADD_MEMBER, formJSON, null);
      AllSharedPreferences sharedPreferences =
          new AllSharedPreferences(
              PreferenceManager.getDefaultSharedPreferences(
                  RevealApplication.getInstance().getApplicationContext()));
      sharedPreferences.savePreference(EVENT_LATITUDE, "");
      sharedPreferences.savePreference(EVENT_LONGITUDE, "");
      sharedPreferences.savePreference(ADMIN_PASSWORD_ENTERED, "");
      sharedPreferences.savePreference(GPS_ACCURACY, "");
      try {
        List<Pair<String, String>> householdPairs = new ArrayList<>();
        for (String householdId : houseHoldIds) {
          householdPairs.add(new Pair<>(householdId, householdId));
        }

        formUtils.populateSpinner(formJSON, HOUSEHOLD_ID, householdPairs);
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_CODE_GET_JSON
        && resultCode == RESULT_OK
        && data != null
        && data.hasExtra(JSON_FORM_PARAM_JSON)) {
      TextView textView = progressBar.findViewById(R.id.loadingText);
      textView.setText("Saving Task...");

      progressBar.setVisibility(View.VISIBLE);

      String json = data.getStringExtra(JSON_FORM_PARAM_JSON);

      try {
        JSONObject jsonForm = new JSONObject(json);
        String encounter = jsonForm.optString(ENCOUNTER_TYPE);
        String planId = PreferencesUtil.getInstance().getCurrentPlanId();

        if (encounter.equals(ADD_MEMBER)
            && PreferencesUtil.getInstance()
                .getInterventionTypeForPlan(planId)
                .equals(Constants.Intervention.SURVEY)) {
          handleAddMemberTask(jsonForm);
        } else if (encounter.equals("index_case_member")
            && PreferencesUtil.getInstance()
                .getInterventionTypeForPlan(planId)
                .equals(Constants.Intervention.SURVEY)) {
          handleIndexCaseMemberTask(jsonForm, planId);
        }
      } catch (JSONException e) {
        Timber.tag("RevealMap").e(e,"GDRSActivity: here 8");
      } catch (Exception e) {
        Timber.tag("RevealMap").e(e, "GDRSActivity: ee 9");
      }
      presenter.saveJsonForm(json);
    }

  }

  public void showMessage(String loadMessage){
    TextView textView = progressBar.findViewById(R.id.loadingText);
    textView.setText(loadMessage);
    progressBar.setVisibility(View.VISIBLE);
  }

  public void populateActionList(String loadMessage) {

    TextView textView = progressBar.findViewById(R.id.loadingText);
    textView.setText(loadMessage);
    Timber.tag("RevealMap").i("Show progress");
    progressBar.setVisibility(View.VISIBLE);
    Runnable runnable =
        new Runnable() {
          @Override
          public void run() {

            List<Action> actionList = new ArrayList<>();

            Set<HdssTask> tasksByStructure =
                hdssRepository.getTasksByStructure(
                    locationUUID, PreferencesUtil.getInstance().getCurrentPlanId());
            Map<String, HdssIndividual> individualsByStructureId =
                hdssRepository.getIndividualsByStructureId(locationUUID);

            Timber.tag("RevealMap").i("got tasks");

            Set<HdssTask> sorted =
                tasksByStructure.stream()
                    .sorted(Comparator.comparing(HdssTask::getIndividualId))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Optional<HdssTask> any =
                sorted.stream()
                    .peek(
                        t -> {
                          Timber.tag("RevealMap").i("business state %s", t.getBusinessStatus());
                        })
                    .filter(
                        t ->
                            INDEX_CASE_MEMBER.equals(t.getCode())
                                && Objects.equals(NOT_VISITED, t.getBusinessStatus()))
                    .findAny();

            for (HdssTask task : sorted) {
              Timber.tag("RevealMap")
                  .i("got individual task %s code %s individual %s", task.getIdentifier(), task.getCode(), task.getIndividualId());
              if (individualsByStructureId.containsKey(task.getForEntity())) {
                HdssIndividual hdssIndividual = individualsByStructureId.get(task.getForEntity());
                if (hdssIndividual != null) {
                  actionList.add(
                      new Action(
                          hdssIndividual.getIndividualId(),
                          hdssIndividual.getGender(),
                          hdssIndividual.getDob(),
                          task.getAuthoredOn().toString("yyyy-MM-dd"),
                          task,
                          hdssIndividual.getName(),
                          task.getHouseholdId()));
                }
              }
            }

            appExecutors
                .mainThread()
                .execute(
                    new Runnable() {
                      @Override
                      public void run() {
                        progressBar.setVisibility(View.GONE);
                        Timber.tag("RevealMap").i("Hide progress");
                        if (actionAdapter != null) {
                          actionAdapter.setActions(actionList);
                          actionAdapter.setFullactions(actionList);
                          actionAdapter.notifyDataSetChanged();
                        } else {
                          actionAdapter =
                              new ActionAdapter(
                                  getThis(),
                                  actionList,
                                  getThis().presenter,
                                  thisCompoundId,
                                  locationUUID,
                                  taskIdentifier);
                          recyclerView.setAdapter(actionAdapter);
                        }
                        if (any.isPresent()) {
                          Timber.tag("RevealMap").i("any.isPresent()");

                          addMemberItem.setVisible(false);
                          addManualMemberItem.setVisible(false);
                        } else {
                          Timber.tag("RevealMap").i("any is NOT Present()");
                          addMemberItem.setVisible(true);
                          addManualMemberItem.setVisible(true);

                        }
                      }
                    });
          }
        };

    appExecutors.diskIO().execute(runnable);
  }

  public GDRSActivity getThis() {
    return this;
  }

  private void handleAddMemberTask(JSONObject jsonForm) {
    UUID uuid = UUID.randomUUID();
    JSONArray fields = JsonFormUtils.fields(jsonForm);
    JSONObject individualIdObj = JsonFormUtils.getFieldJSONObject(fields, "individual_capture");
    JSONObject householdIdObj = JsonFormUtils.getFieldJSONObject(fields, HOUSEHOLD_ID);
    JSONObject genderObj = JsonFormUtils.getFieldJSONObject(fields, GENDER_CAPTURE);
    JSONObject dobObj = JsonFormUtils.getFieldJSONObject(fields, "date_of_birth_capture");
    JSONObject nameObj = JsonFormUtils.getFieldJSONObject(fields, "name_capture");

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
    String name = null;
    if (nameObj != null) {
      name = nameObj.optString("value");
    }
    String householdId = null;
    if (householdIdObj != null) {
      householdId = householdIdObj.optString("value");
    }

    long hdssMaxServerVersion = hdssRepository.getMaxServerVersion();

    hdssMaxServerVersion++;
    HdssHouseholdIndividual hdssHouseholdIndividual =
        new HdssHouseholdIndividual(householdId, individualId, hdssMaxServerVersion);
    hdssRepository.addOrUpdateHouseholdIndividual(List.of(hdssHouseholdIndividual));

    HdssIndividual hdssIndividual =
        new HdssIndividual(
            uuid.toString(),
            individualId,
            dob,
            gender,
            name,
            hdssMaxServerVersion,
            null,
            null,
            null,
            PreferencesUtil.getInstance().getCurrentOperationalArea());
    hdssRepository.addOrUpdateIndividual(List.of(hdssIndividual));

    taskUtils.generateTask(
        this, uuid.toString(), locationUUID, NOT_VISITED, RCD_MEMBER, R.string.rcd_member);

    Task task = taskRepository.getTaskByIdentifier(taskIdentifier);
    Set<HdssTask> tasksByStructure =
        hdssRepository.getTasksByStructure(
            locationUUID, PreferencesUtil.getInstance().getCurrentPlanId());

    if (RCD.equals(task.getCode())) {
      boolean anyRCDComplete = false;

      anyRCDComplete =
          tasksByStructure.stream()
              .anyMatch(innerTask -> COMPLETE.equals(innerTask.getBusinessStatus()));

      if (anyRCDComplete) {
        String businessStatusIndexCase = Constants.BusinessStatus.RCD_PARTIALLY_COMPLETE;
        task.setBusinessStatus(businessStatusIndexCase);
        taskRepository.addOrUpdate(task);
      }

    } else if (INDEX_CASE.equals(task.getCode())) {
      if (COMPLETE.equals(task.getBusinessStatus())) {
        task.setBusinessStatus(INDEX_CASE_COMPLETE);
        taskRepository.addOrUpdate(task);
      }
    } else {
      if (COMPLETE.equals(task.getBusinessStatus())) {
        task.setBusinessStatus(SECONDARY_INDEX_CASE_COMPLETE);
        taskRepository.addOrUpdate(task);
      }
    }
  }

  private void handleIndexCaseMemberTask(JSONObject jsonForm, String planId) throws JSONException {
    Timber.tag("RevealMap").i("GDRSActivity: here 4");
    JSONArray fields = JsonFormUtils.fields(jsonForm);
    JSONObject individualObj = JsonFormUtils.getFieldJSONObject(fields, "individual");
    JSONObject correctHouseholdCompoundObj =
        JsonFormUtils.getFieldJSONObject(fields, "household_compound");
    JSONObject geoStructure = JsonFormUtils.getFieldJSONObject(fields, "geo_structure");
    JSONObject operational = JsonFormUtils.getFieldJSONObject(fields, "operational");
    JSONObject compound = JsonFormUtils.getFieldJSONObject(fields, "compound");
    JSONObject currentHouseholdIdOfIndexCase =
        JsonFormUtils.getFieldJSONObject(fields, "household");
    JSONObject capturedCompound = JsonFormUtils.getFieldJSONObject(fields, "captured_compound");
    JSONObject capturedHousehold = JsonFormUtils.getFieldJSONObject(fields, "captured_household");

    JSONObject correctStructure = JsonFormUtils.getFieldJSONObject(fields, "correct_structure");

    String individualIdValue = null;
    if (individualObj != null) {
      try {
        individualIdValue = individualObj.getString("value");
      } catch (Exception e) {
        Timber.tag("RevealMap").i("GDRSActivity: Err 1");
      }

      if (individualIdValue != null) {
        Timber.tag("RevealMap").i("GDRSActivity: here 5 %s", correctHouseholdCompoundObj);
        String correctStructureValue = null;
        if (correctStructure != null) {
          try {
            correctStructureValue = correctStructure.getString("value");
          } catch (Exception e) {
            Timber.tag("RevealMap").i("GDRSActivity: Err 1");
          }
          if (correctStructureValue != null) {
            Timber.tag("RevealMap").i("correctStructureValue: %s", correctStructureValue);

            if ("no".equals(correctStructureValue)) {
              if (correctHouseholdCompoundObj != null) {
                handleHouseholdIdSelection(
                    correctHouseholdCompoundObj,
                    planId,
                    individualIdValue,
                    currentHouseholdIdOfIndexCase,
                    individualObj,
                    individualIdValue);
              }

              if (geoStructure != null) {
                handleStructureSelection(
                    geoStructure,
                    individualIdValue,
                    planId,
                    currentHouseholdIdOfIndexCase,
                    capturedHousehold,
                    capturedCompound);
              }

              if (operational != null) {
                handleOperationalAreaSelection(operational, planId, individualIdValue);
              }
            } else {
              Timber.tag("RevealMap").i("handleIsCorrectStructureScenario");

              handleIsCorrectStructureScenario(planId,currentHouseholdIdOfIndexCase);
            }
          }
        }
      }
    }
  }

  private void handleHouseholdIdSelection(
      JSONObject correctHouseholdCompoundObj,
      String planId,
      String finalIndividualIdValue,
      JSONObject household,
      JSONObject individualObj,
      String individualIdValue) {
    String correctHouseholdCompoundString = null;
    try {
      correctHouseholdCompoundString = correctHouseholdCompoundObj.getString("value");
    } catch (Exception e) {
      Timber.tag("RevealMap").i("GDRSActivity: Err 2");
    }

    if (correctHouseholdCompoundString != null) {
      handleIndexCaseChangeHousehold(
          correctHouseholdCompoundString,
          planId,
          finalIndividualIdValue,
          household,
          individualObj,
          individualIdValue);
    }
  }

  private void handleOperationalAreaSelection(
      JSONObject operational, String planId, String individualIdValue) {
    String operationalValue = null;
    try {
      operationalValue = operational.getString("value");
    } catch (Exception e) {
      Timber.tag("RevealMap").i("GDRSActivity: Err 4");
    }
    if (operationalValue != null) {

      handleIndexCaseMoveToFloatingOperationalArea(planId, individualIdValue, operationalValue);
    }
  }

  private void handleIsCorrectStructureScenario(String planId,  JSONObject currentHouseholdIdOfIndexCase) {
    String correctHousehold = null;
    if (currentHouseholdIdOfIndexCase != null){
      try {
        correctHousehold = currentHouseholdIdOfIndexCase.getString("value");
      } catch (Exception e) {
        Timber.tag("RevealMap").i("GDRSActivity: Err 4");
      }
    }

    Timber.tag("RevealMap").i("correctHousehold %s",correctHousehold);

    List<StructureTaskForCompound> structuresAndTasksForCompoundByHouseholdId =
        hdssRepository.getStructuresAndTasksForHouseholdId(
            correctHousehold, planId);

    List<StructureTaskForCompound> potentialStructuresForTaskGeneration = new ArrayList<>();
    for (StructureTaskForCompound structure : structuresAndTasksForCompoundByHouseholdId) {

      if (structure.getBusinessStatus() == null){
        potentialStructuresForTaskGeneration.add(structure);
      }
    }
    for (StructureTaskForCompound taskForCompound : potentialStructuresForTaskGeneration) {
      if (taskForCompound.getBusinessStatus() != null) {
        if (taskForCompound.getStructureId().equals(this.locationUUID)
            && taskForCompound.getCode().equals(RCD)) {
          taskRepository.cancelTaskByIdentifier(taskForCompound.getTaskId());
        }
      } else {
//        if (!taskForCompound.getStructureId().equals(this.locationUUID)) {
//          taskUtils.generateTask(
//              this,
//              taskForCompound.getStructureId(),
//              taskForCompound.getStructureId(),
//              NOT_VISITED,
//              RCD,
//              R.string.rcd);
//        }
      }
    }
    List<IndividualsAndTasksForCompound> individualsAndTasksForCompoundByHouseholdId =
        hdssRepository.getIndividualsAndTasksForHouseholdId(
            correctHousehold, planId);

    Timber.tag("RevealMap").i("individualsAndTasksForCompoundByHouseholdId %s",individualsAndTasksForCompoundByHouseholdId);

    String groupNameForSelectedStructure =
        locationRepository.getLocationsParentName(this.locationUUID);
    Timber.tag("RevealMap").i("groupNameForSelectedStructure getLocationsParentName %s",groupNameForSelectedStructure);

    if (groupNameForSelectedStructure == null) {
      groupNameForSelectedStructure =
          PreferencesUtil.getInstance().getCurrentOperationalArea();
    }
    Timber.tag("RevealMap").i("groupNameForSelectedStructure %s",groupNameForSelectedStructure);


    for (IndividualsAndTasksForCompound individualsAndTasksForCompound :
        individualsAndTasksForCompoundByHouseholdId) {
      if (individualsAndTasksForCompound.getBusinessStatus() == null) {
        Timber.tag("RevealMap").i("generating for  %s",individualsAndTasksForCompound.getIndividualId());

        taskUtils.generateTaskWithGroupName(
            this,
            individualsAndTasksForCompound.getIndividualIdentifier(),
            this.locationUUID,
            NOT_VISITED,
            RCD_MEMBER,
            R.string.rcd,
            groupNameForSelectedStructure);
      }
    }
  }

  private void handleStructureSelection(
      JSONObject geoStructure,
      String individualIdValue,
      String planId,
      JSONObject currentHouseholdIdOfIndexCase,
      JSONObject capturedHousehold,
      JSONObject capturedCompound)
      throws JSONException {
    Timber.tag("RevealMap").i("GDRSActivity: geoStructure %s", geoStructure);

    String geoStructureValue = null;
    try {
      geoStructureValue = geoStructure.getString("value");
    } catch (Exception e) {
      Timber.tag("RevealMap").i("GDRSActivity: Err 3");
    }

    String currentHouseholdIdOfIndexCaseString = null;

    try {
      currentHouseholdIdOfIndexCaseString = currentHouseholdIdOfIndexCase.getString("value");
    } catch (Exception e) {

    }

    if (geoStructureValue != null && currentHouseholdIdOfIndexCaseString != null) {
      Timber.tag("RevealMap").i("GDRSActivity: here 7");

      Timber.tag("RevealMap")
          .i(
              "GDRSActivity: updating structure for individual 1 %s %s %s",
              individualIdValue, geoStructureValue, houseHoldIds);

      handleIndexCaseToNewStructure(planId, geoStructureValue, currentHouseholdIdOfIndexCaseString);
    }
  }

  private void handleIndexCaseChangeHousehold(
      String correctHouseholdCompoundString,
      String planId,
      String finalIndividualIdValue,
      JSONObject household,
      JSONObject individualObj,
      String individualIdValue) {
    Timber.tag("RevealMap").i("GDRSActivity: here 6");

    try {
      List<MultiSelectValue> maps =
          new Gson()
              .<List<MultiSelectValue>>fromJson(
                  correctHouseholdCompoundString,
                  new TypeToken<List<MultiSelectValue>>() {}.getType());

      if (maps != null && !maps.isEmpty() && maps.get(0).getKey() != null) {
        String correctHousehold = maps.get(0).getKey();
        if (household != null) {
          if (correctHousehold != null) {

            String householdIdValue = household.getString("value");
            if (householdIdValue != null) {
              hdssRepository.moveIndividualFromHouseholdToHousehold(
                  householdIdValue, correctHousehold, individualIdValue);

              List<String> tasksForCompoundLinkedToHouseholdId =
                  hdssRepository.getTasksForHouseholdId(householdIdValue, planId, "");
              for (String id : tasksForCompoundLinkedToHouseholdId) {
                taskRepository.cancelTaskByIdentifier(id);
              }
              String structureIdByHouseholdId =
                  hdssRepository.getStructureIdByHouseholdId(correctHousehold);
              List<StructureTaskForCompound> structuresAndTasksForCompoundByHouseholdId =
                  hdssRepository.getStructuresAndTasksForHouseholdId(correctHousehold, planId);

              boolean hasExistingIndexCase = false;
              List<StructureTaskForCompound> potentialStructuresForTaskGeneration = new ArrayList<>();
              for (StructureTaskForCompound structure : structuresAndTasksForCompoundByHouseholdId) {

                if (structure.getBusinessStatus() != null
                    && structure.getStatus() != null
                    && structure.getCode() != null
                    && !structure.getCode().equals(RCD)) {
                  if (structure.getCode().equals(INDEX_CASE)) {
                    if (!structure.getStatus().equals(Task.TaskStatus.CANCELLED.toString())
                        && structure.getBusinessStatus().equals(INDEX_CASE_NOT_VISITED)) {
                      hasExistingIndexCase = true;
                    }
                  }
                } else {
                  potentialStructuresForTaskGeneration.add(structure);
                }
              }
              for (StructureTaskForCompound taskForCompound : potentialStructuresForTaskGeneration) {
                if (taskForCompound.getBusinessStatus() != null) {
                  if (taskForCompound.getStructureId().equals(structureIdByHouseholdId)
                      && taskForCompound.getCode().equals(RCD)) {
                    taskRepository.cancelTaskByIdentifier(taskForCompound.getTaskId());
                  }
                } else {
                  //              if
                  // (!taskForCompound.getStructureId().equals(structureIdByHouseholdId)) {
                  //                taskUtils.generateTask(
                  //                    this,
                  //                    taskForCompound.getStructureId(),
                  //                    taskForCompound.getStructureId(),
                  //                    NOT_VISITED,
                  //                    RCD,
                  //                    R.string.rcd);
                  //              }
                }
              }
              List<IndividualsAndTasksForCompound> individualsAndTasksForCompoundByHouseholdId =
                  hdssRepository.getIndividualsAndTasksForHouseholdId(correctHousehold, planId);

              String groupNameForSelectedStructure =
                  locationRepository.getLocationsParentName(structureIdByHouseholdId);

              if (groupNameForSelectedStructure == null) {
                groupNameForSelectedStructure =
                    PreferencesUtil.getInstance().getCurrentOperationalArea();
              }

              for (IndividualsAndTasksForCompound individualsAndTasksForCompound :
                  individualsAndTasksForCompoundByHouseholdId) {
                if (individualsAndTasksForCompound.getBusinessStatus() == null) {

                  taskUtils.generateTaskWithGroupName(
                      this,
                      individualsAndTasksForCompound.getIndividualIdentifier(),
                      structureIdByHouseholdId,
                      NOT_VISITED,
                      RCD_MEMBER,
                      R.string.rcd,
                      groupNameForSelectedStructure);
                }
              }

              try {
                HdssIndividual individualsByIndividualId =
                    hdssRepository.getIndividualByIndividualId(individualIdValue);
                if (!hasExistingIndexCase) {
                  taskUtils.generateTaskWithGroupName(
                      this,
                      structureIdByHouseholdId,
                      structureIdByHouseholdId,
                      INDEX_CASE_NOT_VISITED,
                      INDEX_CASE,
                      R.string.index_case,
                      groupNameForSelectedStructure);

                  taskUtils.generateTaskWithGroupName(
                      this,
                      individualsByIndividualId.getIdentifier(),
                      structureIdByHouseholdId,
                      NOT_VISITED,
                      INDEX_CASE_MEMBER,
                      R.string.index_case,
                      groupNameForSelectedStructure);

                } else {
                  taskUtils.generateTaskWithGroupName(
                      this,
                      structureIdByHouseholdId,
                      structureIdByHouseholdId,
                      SECONDARY_INDEX_CASE_NOT_VISITED,
                      SECONDARY_INDEX_CASE,
                      R.string.index_case,
                      groupNameForSelectedStructure);
                  taskUtils.generateTaskWithGroupName(
                      this,
                      individualsByIndividualId.getIndividualId(),
                      structureIdByHouseholdId,
                      NOT_VISITED,
                      SECONDARY_INDEX_CASE_MEMBER,
                      R.string.index_case,
                      groupNameForSelectedStructure);
                }
              } catch (Exception e) {
                Timber.tag("RevealMap").e(e, "the error");
              }
            }
          }
        }
      }
    } catch (JsonSyntaxException js) {
      Timber.tag("RevealMap").e(" err %s", js.getMessage());
    } catch (Exception e) {
      Timber.tag("RevealMap").e(e, " err %s", e.getMessage());
    }
  }

  private void handleIndexCaseMoveToFloatingOperationalArea(
      String planId, String individualIdValue, String operationalValue) {
    List<String> tasksForCompoundLinkedToHouseholdId =
        hdssRepository.getTasksForCompoundLinkedToHouseholdId(
            thisCompoundId, planId, individualIdValue);
    for (String id : tasksForCompoundLinkedToHouseholdId) {
      taskRepository.cancelTaskByIdentifier(id);
      Timber.tag("GDRSActivity").d("Operational cancel tasks %s", id);
    }

    String householdIdByIndividualId =
        hdssRepository.getHouseholdIdByIndividualId(individualIdValue);

    long hdssMaxServerVersion = hdssRepository.getMaxServerVersion();

    HdssHousehold hdssHousehold =
        hdssRepository.getHdssHouseholdIdByHouseholdId(householdIdByIndividualId);

    hdssRepository.removeHouseholdFromCompound(householdIdByIndividualId);
    hdssRepository.removeHouseholdFromStructure(householdIdByIndividualId);

    if (hdssHousehold != null) {
      hdssHousehold.setFloatingHouseholdLocationName(operationalValue);
      hdssHousehold.setServerVersion(hdssMaxServerVersion);
      hdssRepository.addOrUpdateHousehold(List.of(hdssHousehold));
    }
  }

//  private void handleIndexCaseToNewStructureWithoutHousehold(
//      JSONObject capturedHousehold,
//      JSONObject capturedCompound,
//      String planId,
//      String currentHouseholdIdOfIndexCaseString,
//      String geoStructureValue)
//      throws JSONException {
//
//    hdssRepository.removeHouseholdFromStructure(currentHouseholdIdOfIndexCaseString);
//
//    if (capturedHousehold != null && capturedCompound != null) {
//      String capturedHouseholdValue = capturedHousehold.getString("value");
//      String capturedCompoundValue = capturedCompound.getString("value");
//      if (capturedHouseholdValue != null
//          && !capturedHouseholdValue.isEmpty()
//          && capturedCompoundValue != null
//          && !capturedCompoundValue.isEmpty()) {
//
//        int maxServerVersion = hdssRepository.getMaxServerVersion();
//
//        List<String> tasksForCompoundLinkedToHouseholdId =
//            hdssRepository.getStructureTasksForCompoundLinkedToHouseholdId(thisCompoundId, planId);
//
//        if (tasksForCompoundLinkedToHouseholdId != null
//            && !tasksForCompoundLinkedToHouseholdId.isEmpty()) {
//          for (String id : tasksForCompoundLinkedToHouseholdId) {
//            taskRepository.cancelTaskByIdentifier(id);
//          }
//        }
//
//        String householdIdValue = currentHouseholdIdOfIndexCaseString;
//
//        if (householdIdValue != null) {
//
//          hdssRepository.removeHouseholdFromCompound(householdIdValue);
//
//          List<String> individualTasksForCompoundExcludingHousehold =
//              hdssRepository.getIndividualTasksForCompoundExcludingHousehold(
//                  thisCompoundId, householdIdValue, planId);
//
//          if (individualTasksForCompoundExcludingHousehold != null
//              && !individualTasksForCompoundExcludingHousehold.isEmpty()) {
//            for (String id : individualTasksForCompoundExcludingHousehold) {
//              taskRepository.cancelTaskByIdentifier(id);
//            }
//          }
//        }
//
//        HdssCompound hdssCompound =
//            HdssCompound.builder()
//                .compoundId(capturedCompoundValue)
//                .serverVersion(maxServerVersion)
//                .build();
//        hdssRepository.addOrUpdateCompounds(List.of(hdssCompound));
//
//        HdssCompoundHousehold compoundHousehold =
//            HdssCompoundHousehold.builder()
//                .householdId(capturedHouseholdValue)
//                .compoundId(capturedCompoundValue)
//                .serverVersion(maxServerVersion)
//                .build();
//        hdssRepository.addOrUpdateCompoundHouseholds(List.of(compoundHousehold));
//
//        HdssHouseholdStructure hdssHouseholdStructure =
//            HdssHouseholdStructure.builder()
//                .householdId(capturedHouseholdValue)
//                .structureId(geoStructureValue)
//                .serverVersion(maxServerVersion)
//                .build();
//        hdssRepository.addOrUpdateHouseholdStructure(List.of(hdssHouseholdStructure));
//
//        List<HdssIndividual> individualsByHouseholdId =
//            hdssRepository.getIndividualsByHouseholdId(capturedHouseholdValue);
//
//        for (HdssIndividual hdssIndividual : individualsByHouseholdId) {
//          HdssHouseholdIndividual hdssHouseholdIndividual =
//              HdssHouseholdIndividual.builder()
//                  .householdId(capturedHouseholdValue)
//                  .individualId(hdssIndividual.getIndividualId())
//                  .serverVersion(maxServerVersion++)
//                  .build();
//
//          hdssRepository.addOrUpdateHouseholdIndividual(List.of(hdssHouseholdIndividual));
//        }
//
//        taskUtils.generateTask(
//            this,
//            geoStructureValue,
//            geoStructureValue,
//            INDEX_CASE_NOT_VISITED,
//            INDEX_CASE,
//            R.string.index_case);
//      }
//    }
//  }

  private void handleIndexCaseToNewStructure(
      String planId, String geoStructureValue, String currentHouseholdIdOfIndexCaseString)
      throws JSONException {

    HdssCompoundHousehold householdIdCompoundIdInSelectedStructure =
        hdssRepository.getHouseholdIdCompoundIdByHouseholdId(currentHouseholdIdOfIndexCaseString);

    List<StructureTaskForCompound> structuresAndTasksForCompound = new ArrayList<>();

    if (householdIdCompoundIdInSelectedStructure != null) {
      structuresAndTasksForCompound =
          hdssRepository.getStructuresAndTasksForHousehold(
              householdIdCompoundIdInSelectedStructure.getHouseholdId(), planId);
    }

    List<StructureTaskForCompound> potentialStructuresForRCDTaskGeneration = new ArrayList<>();

    StructureTaskForCompound existingRCDTaskToConvert = null;
    StructureTaskForCompound existingIndexCase = null;
    StructureTaskForCompound existingSecondaryIndexCase = null;
    for (StructureTaskForCompound structureCompoundTask : structuresAndTasksForCompound) {
      if (structureCompoundTask.getTaskId() != null
          && !structureCompoundTask.getStatus().equals(Task.TaskStatus.CANCELLED.toString())) {
        if (structureCompoundTask.getCode().equals(INDEX_CASE)) {
          existingIndexCase = structureCompoundTask;
        } else if (structureCompoundTask.getCode().equals(SECONDARY_INDEX_CASE)) {
          existingSecondaryIndexCase = structureCompoundTask;
        } else if (structureCompoundTask.getCode().equals(RCD)) {
          if (structureCompoundTask.getStructureId().equals(geoStructureValue)) {
            existingRCDTaskToConvert = structureCompoundTask;
          }
        }
      } else {
//        if (structureCompoundTask.getStructureId().equals(geoStructureValue)) {
//
//        } else {
//          potentialStructuresForRCDTaskGeneration.add(structureCompoundTask);
//        }
      }
    }

    if (existingRCDTaskToConvert != null) {
      taskRepository.cancelTaskByIdentifier(existingRCDTaskToConvert.getTaskId());
    }

    if (existingIndexCase == null && existingSecondaryIndexCase == null) {
      taskUtils.generateTask(
          this,
          geoStructureValue,
          geoStructureValue,
          INDEX_CASE_NOT_VISITED,
          INDEX_CASE,
          R.string.index_case);

      Map<String, HdssIndividual> individualsByStructureId =
          hdssRepository.getIndividualsByStructureId(geoStructureValue);

      for (Map.Entry<String, HdssIndividual> entry : individualsByStructureId.entrySet()) {
        taskUtils.generateTask(
            this, entry.getKey(), geoStructureValue, NOT_VISITED, RCD_MEMBER, R.string.rcd_member);
      }
    } else {
      Timber.tag("RevealMap").i("Dont create any index cases");
    }

//    for (StructureTaskForCompound task : potentialStructuresForRCDTaskGeneration) {
//      taskUtils.generateTask(
//          this, task.getStructureId(), task.getStructureId(), NOT_VISITED, RCD, R.string.rcd);
//      Map<String, HdssIndividual> individualsByStructureId =
//          hdssRepository.getIndividualsByStructureId(task.getStructureId());
//      for (Map.Entry<String, HdssIndividual> entry : individualsByStructureId.entrySet()) {
//        taskUtils.generateTask(
//            this,
//            entry.getKey(),
//            task.getStructureId(),
//            NOT_VISITED,
//            RCD_MEMBER,
//            R.string.rcd_member);
//      }
//    }

    List<String> tasksForCompoundLinkedToHouseholdId =
        hdssRepository.getStructureTasksForHouseholdId(currentHouseholdIdOfIndexCaseString, planId);

    if (tasksForCompoundLinkedToHouseholdId != null
        && !tasksForCompoundLinkedToHouseholdId.isEmpty()) {
      for (String id : tasksForCompoundLinkedToHouseholdId) {
        taskRepository.cancelTaskByIdentifier(id);
      }
    }
    if (currentHouseholdIdOfIndexCaseString != null) {

      hdssRepository.removeHouseholdFromStructure(currentHouseholdIdOfIndexCaseString);
      hdssRepository.removeHouseholdFromCompound(currentHouseholdIdOfIndexCaseString);

//      List<String> individualTasksForCompoundExcludingHousehold =
//          hdssRepository.getIndividualTasksForCompoundExcludingHousehold(
//              thisCompoundId, currentHouseholdIdOfIndexCaseString, planId);
//
//      if (individualTasksForCompoundExcludingHousehold != null
//          && !individualTasksForCompoundExcludingHousehold.isEmpty()) {
//        for (String id : individualTasksForCompoundExcludingHousehold) {
//          taskRepository.cancelTaskByIdentifier(id);
//        }
//      }

      int maxServerVersion = hdssRepository.getMaxServerVersion();
      maxServerVersion++;

      HdssHouseholdStructure householdStructure =
          HdssHouseholdStructure.builder()
              .structureId(geoStructureValue)
              .householdId(currentHouseholdIdOfIndexCaseString)
              .serverVersion((long) maxServerVersion)
              .build();
      hdssRepository.addOrUpdateHouseholdStructure(List.of(householdStructure));

      HdssCompoundHousehold compoundHousehold =
          HdssCompoundHousehold.builder()
              .compoundId(householdIdCompoundIdInSelectedStructure.getCompoundId())
              .householdId(currentHouseholdIdOfIndexCaseString)
              .serverVersion(maxServerVersion)
              .build();

      hdssRepository.addOrUpdateCompoundHouseholdsBatched(List.of(compoundHousehold));
    }
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

  public static @NonNull HdssLocation getHdssLocation(String locationUUID) {
    HdssLocation location = new HdssLocation();
    LocationProperty locationProperty = new LocationProperty();
    locationProperty.setType(Constants.StructureType.RESIDENTIAL);
    locationProperty.setUid(locationUUID);
    locationProperty.setVersion(0);
    location.setProperties(locationProperty);
    location.setId(locationUUID);
    return location;
  }

  public static @NonNull BaseTaskDetails getBaseTaskDetails(
      Task task, String taskIdentifier, String locationUUID) {
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
