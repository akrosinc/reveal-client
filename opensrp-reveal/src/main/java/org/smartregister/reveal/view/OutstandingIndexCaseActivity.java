package org.smartregister.reveal.view;

import static org.smartregister.reveal.interactor.BaseInteractor.gson;
import static org.smartregister.reveal.test.GDRSActivity.INDIVIDUAL;
import static org.smartregister.reveal.test.GDRSActivity.NAME;
import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE;
import static org.smartregister.reveal.util.Constants.Action.RCD;
import static org.smartregister.reveal.util.Constants.Action.RCD_MEMBER;
import static org.smartregister.reveal.util.Constants.Action.SECONDARY_INDEX_CASE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.INDEX_CASE_NOT_VISITED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;
import static org.smartregister.reveal.util.Constants.JSON_FORM_PARAM_JSON;
import static org.smartregister.reveal.util.Constants.JsonForm.ENCOUNTER_TYPE;
import static org.smartregister.reveal.util.Constants.JsonForm.GDRS_INDEX_CASE_FLOATING;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_GET_JSON;
import static org.smartregister.reveal.util.Utils.getOperationalAreaLocation;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.HdssCompound;
import org.smartregister.domain.HdssCompoundHousehold;
import org.smartregister.domain.HdssHousehold;
import org.smartregister.domain.HdssHouseholdStructure;
import org.smartregister.domain.HdssIndividual;
import org.smartregister.domain.Location;
import org.smartregister.domain.PhysicalLocation;
import org.smartregister.domain.StructureTaskForCompound;
import org.smartregister.domain.Task;
import org.smartregister.repository.HdssRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.BaseContract;
import org.smartregister.reveal.contract.OtherFormsContract;
import org.smartregister.reveal.interactor.BaseInteractor;
import org.smartregister.reveal.model.BaseTaskDetails;
import org.smartregister.reveal.model.StructureDetails;
import org.smartregister.reveal.test.GDRSActivity.MultiSelectValue;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.GeoJsonUtils;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import org.smartregister.reveal.util.TaskUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.activity.MultiLanguageActivity;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import timber.log.Timber;

/** Created by Richard Kareko on 9/22/20. */
public class OutstandingIndexCaseActivity extends MultiLanguageActivity {

  private RecyclerView recyclerView;

  private IndexCaseAdapter indexCaseAdapter;

  private HdssRepository hdssRepository;

  private RevealJsonFormUtils formUtils;

  private TaskRepository taskRepository;
  private OutstandingIndexCasePresenter presenter;

  private LocationRepository locationRepository;
  private TaskUtils taskUtils;

  private static @NonNull BaseTaskDetails getBaseTaskDetails(
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

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    hdssRepository = RevealApplication.getInstance().getHdssRepository();
    formUtils = new RevealJsonFormUtils();
    taskRepository = RevealApplication.getInstance().getTaskRepository();
    presenter = new OutstandingIndexCasePresenter(this);
    taskUtils = TaskUtils.getInstance();
    locationRepository = RevealApplication.getInstance().getLocationRepository();
    setContentView(R.layout.activity_index_cases);

    TextView textView = findViewById(R.id.textViewCenter);
    textView.setText(PreferencesUtil.getInstance().getCurrentOperationalArea());

    Toolbar toolbar = this.findViewById(R.id.toolbar);
    this.setSupportActionBar(toolbar);
    this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    toolbar.setNavigationOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            onBackPressed();
          }
        });

    recyclerView = findViewById(R.id.recyclerView);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    setupSearchfield();
    setupClearButton();
    DividerItemDecoration dividerItemDecoration =
        new DividerItemDecoration(
            recyclerView.getContext(),
            ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation());
    recyclerView.addItemDecoration(dividerItemDecoration);
    populateActionList();

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
    if (indexCaseAdapter==null) {
      Timber.tag("RevealMap").i("indexCaseAdapter is null");
      return;
    }
    if (!query.isEmpty()) {
      List<IndexCase> filtered = new ArrayList<>();

      for (IndexCase action : indexCaseAdapter.getFullIndexCases()) {
        if (action.getName().toLowerCase().contains(query.toLowerCase())){
          filtered.add(action);
        }
        if (action.getIndividualId().toLowerCase().contains(query.toLowerCase())){
          filtered.add(action);
        }
        Timber.tag("Reveal").i("results %s",filtered);
        indexCaseAdapter.setIndexCases(filtered);
        indexCaseAdapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
      }
    } else {
      indexCaseAdapter.setIndexCases(indexCaseAdapter.getFullIndexCases());
      indexCaseAdapter.notifyDataSetChanged();
    }
  }
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_CODE_GET_JSON
        && resultCode == RESULT_OK
        && data != null
        && data.hasExtra(JSON_FORM_PARAM_JSON)) {
      String json = data.getStringExtra(JSON_FORM_PARAM_JSON);
      String planId = PreferencesUtil.getInstance().getCurrentPlanId();
      try {
        JSONObject jsonForm = new JSONObject(json);
        String encounter = jsonForm.optString(ENCOUNTER_TYPE);
        JSONArray fields = JsonFormUtils.fields(jsonForm);
        JSONObject individualObj = JsonFormUtils.getFieldJSONObject(fields, "individual");
        JSONObject geoStructure = JsonFormUtils.getFieldJSONObject(fields, "geo_structure");

        JSONObject correctHouseholdCompoundObj =
            JsonFormUtils.getFieldJSONObject(fields, "household_compound");
        JSONObject operational = JsonFormUtils.getFieldJSONObject(fields, "operational");
        JSONObject capturedCompound = JsonFormUtils.getFieldJSONObject(fields, "captured_compound");
        JSONObject capturedHousehold =
            JsonFormUtils.getFieldJSONObject(fields, "captured_household");

        if ("index_case_member_floating".equals(encounter)) {
          String individualIdValue = null;
          if (individualObj != null) {
            try {
              individualIdValue = individualObj.getString("value");
            } catch (Exception e) {
              Timber.tag("Reveal Exception").i("GDRSActivity: Err 1");
            }

            if (individualIdValue != null) {
              if (geoStructure != null) {
                handleStructureSelection(
                    geoStructure, individualIdValue, planId, capturedHousehold, capturedCompound);
              }
              if (correctHouseholdCompoundObj != null) {
                handleHouseholdIdSelection(correctHouseholdCompoundObj, planId, individualIdValue);
              }
              if (operational != null) {
                handleOperationalAreaSelection(operational, individualIdValue);
              }
            }
          }
        }
      } catch (JSONException e) {

      }

      presenter.saveJsonForm(json);
      populateActionList();
    }
  }

  private void handleStructureSelection(
      JSONObject geoStructure,
      String individualIdValue,
      String planId,
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

    if (geoStructureValue != null) {
      Timber.tag("RevealMap").i("GDRSActivity: here 7");

      handleIndexCaseToNewStructure(
          planId, geoStructureValue, individualIdValue, capturedHousehold, capturedCompound);
    }
  }

  private void handleIndexCaseToNewStructure(
      String planId,
      String geoStructureValue,
      String individualIdValue,
      JSONObject capturedHousehold,
      JSONObject capturedCompound)
      throws JSONException {

    HdssCompoundHousehold householdIdCompoundIdInSelectedStructure =
        hdssRepository.getHouseholdIdCompoundIdByStructureId(geoStructureValue);

    List<StructureTaskForCompound> structuresAndTasksForCompound = new ArrayList<>();

    if (householdIdCompoundIdInSelectedStructure != null) {
      structuresAndTasksForCompound =
          hdssRepository.getStructuresAndTasksForHousehold(
              householdIdCompoundIdInSelectedStructure.getHouseholdId(), planId);
    }

    StructureTaskForCompound existingIndexCase = null;
    StructureTaskForCompound existingSecondaryIndexCase = null;
    for (StructureTaskForCompound structureCompoundTask : structuresAndTasksForCompound) {
      if (structureCompoundTask.getTaskId() != null
          && !structureCompoundTask.getStatus().equals(Task.TaskStatus.CANCELLED.toString())) {
        if (structureCompoundTask.getCode().equals(INDEX_CASE)) {
          existingIndexCase = structureCompoundTask;
        } else if (structureCompoundTask.getCode().equals(SECONDARY_INDEX_CASE)) {
          existingSecondaryIndexCase = structureCompoundTask;
        }
      }
    }

    int maxServerVersion = hdssRepository.getMaxServerVersion();
    maxServerVersion++;

    HdssIndividual individualByIndividualId =
        hdssRepository.getIndividualByIndividualId(individualIdValue);

    if (individualByIndividualId != null && individualByIndividualId.getIdentifier() != null) {
      if (existingIndexCase != null || existingSecondaryIndexCase != null) {
        taskUtils.generateTask(
            this,
            individualByIndividualId.getIdentifier(),
            geoStructureValue,
            NOT_VISITED,
            RCD_MEMBER,
            R.string.rcd_member);
      }
      individualByIndividualId.setFloatingLocationName(null);
      individualByIndividualId.setServerVersion(maxServerVersion);

      hdssRepository.addOrUpdateIndividual(List.of(individualByIndividualId));

      if (householdIdCompoundIdInSelectedStructure != null
          && householdIdCompoundIdInSelectedStructure.getHouseholdId() != null) {
        hdssRepository.addIndividualToHousehold(
            individualByIndividualId.getIndividualId(),
            householdIdCompoundIdInSelectedStructure.getHouseholdId());
      } else {
        if (capturedHousehold != null) {
          String capturedHouseholdString = capturedHousehold.getString("value");
          if (capturedHouseholdString != null) {

            if (capturedCompound != null) {
              String capturedCompoundString = capturedCompound.getString("value");

              if (capturedCompoundString != null) {
                HdssCompound hdssCompound =
                    HdssCompound.builder()
                        .compoundId(capturedCompoundString)
                        .serverVersion(maxServerVersion)
                        .build();

                HdssCompoundHousehold compoundHousehold =
                    HdssCompoundHousehold.builder()
                        .householdId(capturedHouseholdString)
                        .compoundId(capturedCompoundString)
                        .serverVersion(maxServerVersion)
                        .build();

                hdssRepository.addOrUpdateCompounds(List.of(hdssCompound));
                HdssHousehold hdssHousehold =
                    HdssHousehold.builder()
                        .householdId(capturedHouseholdString)
                        .serverVersion(maxServerVersion)
                        .build();

                hdssRepository.addOrUpdateHousehold(List.of(hdssHousehold));
                hdssRepository.addOrUpdateCompoundHouseholds(List.of(compoundHousehold));

                HdssHouseholdStructure householdStructure = HdssHouseholdStructure
                    .builder()
                    .structureId(geoStructureValue)
                    .householdId(capturedHouseholdString)
                    .serverVersion(maxServerVersion)
                    .build();

                hdssRepository.addOrUpdateHouseholdStructure(List.of(householdStructure));

                hdssRepository.addIndividualToHousehold(
                    individualByIndividualId.getIndividualId(),
                    capturedHouseholdString);
              }
            }
          }
        }
      }

    } else {
      Timber.tag("RevealMap").i("Dont create any rcd tasks");
    }
  }

  private void handleOperationalAreaSelection(JSONObject operational, String individualIdValue) {
    String operationalValue = null;
    try {
      operationalValue = operational.getString("value");
    } catch (Exception e) {
      Timber.tag("RevealMap").i("GDRSActivity: Err 4");
    }
    if (operationalValue != null) {

      handleIndexCaseMoveToFloatingOperationalArea(individualIdValue, operationalValue);
    }
  }

  private void handleIndexCaseMoveToFloatingOperationalArea(
      String individualIdValue, String operationalValue) {

    HdssIndividual individualByIndividualId =
        hdssRepository.getIndividualByIndividualId(individualIdValue);

    long hdssMaxServerVersion = hdssRepository.getMaxServerVersion();

    if (individualByIndividualId != null) {
      individualByIndividualId.setFloatingLocationName(operationalValue);
      individualByIndividualId.setServerVersion(hdssMaxServerVersion);
      hdssRepository.addOrUpdateIndividual(List.of(individualByIndividualId));
    }
  }

  private void handleHouseholdIdSelection(
      JSONObject correctHouseholdCompoundObj, String planId, String individualIdValue) {
    String correctHouseholdCompoundString = null;
    try {
      correctHouseholdCompoundString = correctHouseholdCompoundObj.getString("value");
    } catch (Exception e) {
      Timber.tag("RevealMap").i("GDRSActivity: Err 2");
    }

    if (correctHouseholdCompoundString != null) {
      handleIndexCaseChangeHousehold(correctHouseholdCompoundString, planId, individualIdValue);
    }
  }

  private void handleIndexCaseChangeHousehold(
      String correctHouseholdCompoundString, String planId, String individualIdValue) {
    Timber.tag("RevealMap").i("GDRSActivity: here 6");

    try {
      List<MultiSelectValue> maps =
          new Gson()
              .<List<MultiSelectValue>>fromJson(
                  correctHouseholdCompoundString,
                  new TypeToken<List<MultiSelectValue>>() {}.getType());

      if (maps != null && !maps.isEmpty() && maps.get(0).getKey() != null) {
        String correctHousehold = maps.get(0).getKey();

        if (correctHousehold != null) {
          String structureIdByHouseholdId =
              hdssRepository.getStructureIdByHouseholdId(correctHousehold);
          List<StructureTaskForCompound> structuresAndTasksForCompoundByHouseholdId =
              hdssRepository.getStructuresAndTasksForHouseholdId(
                  correctHousehold, planId);

          boolean hasExistingIndexCase = false;
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
            }
          }

          String groupNameForSelectedStructure =
              locationRepository.getLocationsParentName(structureIdByHouseholdId);

          if (groupNameForSelectedStructure == null) {
            groupNameForSelectedStructure =
                PreferencesUtil.getInstance().getCurrentOperationalArea();
          }
          HdssIndividual individualByIndividualId =
              hdssRepository.getIndividualByIndividualId(individualIdValue);

          hdssRepository.addIndividualToHousehold(correctHousehold, individualIdValue);

          if (individualByIndividualId != null
              && individualByIndividualId.getIdentifier() != null) {

            long hdssMaxServerVersion = hdssRepository.getMaxServerVersion();

            individualByIndividualId.setFloatingLocationName(null);
            individualByIndividualId.setServerVersion(hdssMaxServerVersion);

            hdssRepository.addOrUpdateIndividual(List.of(individualByIndividualId));

            if (hasExistingIndexCase) {
              taskUtils.generateTaskWithGroupName(
                  this,
                  individualByIndividualId.getIdentifier(),
                  structureIdByHouseholdId,
                  NOT_VISITED,
                  RCD_MEMBER,
                  R.string.rcd_member,
                  groupNameForSelectedStructure);
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

  public void populateActionList() {

    List<IndexCase> indexCases = new ArrayList<>();

    List<HdssIndividual> individualsForOperationalArea =
        hdssRepository.getIndividualsForOperationalArea(
            PreferencesUtil.getInstance().getCurrentOperationalArea());

    for (HdssIndividual individual : individualsForOperationalArea) {

      IndexCase indexCase =
          new IndexCase(
              individual.getIdentifier(),
              individual.getIndividualId(),
              individual.getGender(),
              individual.getDob(),
              individual.getName());

      indexCases.add(indexCase);
    }

    if (indexCaseAdapter != null) {
      indexCaseAdapter.setIndexCases(indexCases);
      indexCaseAdapter.setFullIndexCases(indexCases);
      indexCaseAdapter.notifyDataSetChanged();
    } else {
      indexCaseAdapter = new IndexCaseAdapter(indexCases);
      recyclerView.setAdapter(indexCaseAdapter);

    }
  }

  private static class IndexCase {
    @Getter private final String individualId;
    @Getter private final String id;
    @Getter private final String gender;
    @Getter private final String name;
    @Getter private final String dob;

    IndexCase(String id, String individualId, String gender, String dob, String name) {
      this.individualId = individualId;
      this.id = id;
      this.gender = gender;
      this.dob = dob;
      this.name = name;
    }
  }

  private JSONObject createFeatureCollection() throws JSONException {
    JSONObject featureCollection = new JSONObject();
    featureCollection.put(Constants.GeoJSON.TYPE, Constants.GeoJSON.FEATURE_COLLECTION);
    return featureCollection;
  }

  @Setter
  private class IndexCaseAdapter
      extends RecyclerView.Adapter<IndexCaseAdapter.IndexCaseViewHolder> {

    private List<IndexCase> indexCases;

    @Getter
    private List<IndexCase> fullIndexCases;

    public IndexCaseAdapter(List<IndexCase> indexCases) {
      this.indexCases = indexCases;
      this.fullIndexCases = indexCases;
    }

    @NonNull
    @Override
    public IndexCaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = getLayoutInflater().inflate(R.layout.gdrs_item_action, parent, false);
      return new IndexCaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IndexCaseViewHolder holder, int position) {
      IndexCase indexCase = getIndexCase(holder, position);

      Button actionButton = holder.actionButton;
      actionButton.setText(R.string.select_structure);
      Drawable background = holder.actionButton.getBackground();

      if (background instanceof GradientDrawable) {
        ((GradientDrawable) background).setColor(getResources().getColor(R.color.cyan, null));
      } else {
        actionButton.setBackgroundColor(getResources().getColor(R.color.cyan, null));
      }

      actionButton.setOnClickListener(
          v -> {
            try {
              JSONObject featureCollection = createFeatureCollection();
              Location operationalAreaLocation =
                  getOperationalAreaLocation(
                      PreferencesUtil.getInstance().getCurrentOperationalArea());
              List<Location> structures =
                  RevealApplication.getInstance()
                      .getContext()
                      .getStructureRepository()
                      .getLocationsByParentIdForGdrs(operationalAreaLocation.getId(), "structure");

              Map<String, StructureDetails> collect =
                  structures.stream()
                      .map(
                          structure ->
                              new AbstractMap.SimpleEntry<>(
                                  structure.getId(),
                                  new StructureDetails(
                                      structure.getProperties().getName(),
                                      structure.getProperties().getName())))
                      .collect(
                          Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));

              Map<String, Set<Task>> map = new HashMap<>();

              for (Location structure : structures) {
                Set<Task> tasksByEntity = taskRepository.getTasksByEntity(structure.getId());
                map.put(structure.getId(), tasksByEntity);
              }
              Map<String, Boolean> isAHouseholdByStructureList =
                  hdssRepository.getIsAHouseholdByStructureList(
                      structures.stream()
                          .map(PhysicalLocation::getId)
                          .collect(Collectors.toList()));

              String features =
                  GeoJsonUtils.getGeoJsonFromStructuresAndTasksForGdrs(
                      structures, map, null, collect, null, isAHouseholdByStructureList);

              featureCollection.put(Constants.GeoJSON.FEATURES, new JSONArray(features));
              RevealApplication.getInstance()
                  .setFeatureCollection(FeatureCollection.fromJson(featureCollection.toString()));
              RevealApplication.getInstance()
                  .setOperationalArea(Feature.fromJson(gson.toJson(operationalAreaLocation)));

              JSONObject formJSON =
                  formUtils.getFormJSON(
                      OutstandingIndexCaseActivity.this, GDRS_INDEX_CASE_FLOATING, null, null);

              formUtils.populateField(
                  formJSON, INDIVIDUAL, indexCase.getIndividualId(), JsonFormConstants.VALUE);
              formUtils.populateField(formJSON, NAME, indexCase.getName(), JsonFormConstants.VALUE);
              formUtils.startJsonForm(formJSON, OutstandingIndexCaseActivity.this);
            } catch (Exception e) {
              Toast.makeText(
                      OutstandingIndexCaseActivity.this,
                      "Cannot open geo widget to capture structure",
                      Toast.LENGTH_LONG)
                  .show();
            }
          });
    }

    private @NonNull IndexCase getIndexCase(IndexCaseViewHolder holder, int position) {
      IndexCase action = indexCases.get(position);
      holder.gender.setText(action.getGender());
      holder.individualId.setText(action.getIndividualId());
      holder.dob.setText(action.getDob());
      holder.name.setText(action.getName());

      return action;
    }

    @Override
    public int getItemCount() {
      return indexCases.size();
    }

    class IndexCaseViewHolder extends RecyclerView.ViewHolder {

      TextView individualId;
      TextView gender;
      TextView dob;
      LinearLayout createdDateLayout;
      TextView name;
      TextView oldTaskMessage;
      TextView individualHousehold;
      Button actionButton;

      IndexCaseViewHolder(View itemView) {
        super(itemView);
        individualId = itemView.findViewById(R.id.individualId);
        individualHousehold = itemView.findViewById(R.id.individualHousehold);
        individualHousehold.setVisibility(View.GONE);
        gender = itemView.findViewById(R.id.individualGender);
        dob = itemView.findViewById(R.id.individualDob);
        actionButton = itemView.findViewById(R.id.actionButton);
        createdDateLayout = itemView.findViewById(R.id.createdDateLayout);
        createdDateLayout.setVisibility(View.GONE);
        name = itemView.findViewById(R.id.individualName);
        oldTaskMessage = itemView.findViewById(R.id.oldTaskMessage);
      }
    }
  }

  public class OutstandingIndexCasePresenter implements OtherFormsContract.Presenter {

    private final OutstandingIndexCaseInteractor outstandingIndexCaseInteractor;

    private final OutstandingIndexCaseActivity activity;

    public OutstandingIndexCasePresenter(OutstandingIndexCaseActivity activity) {
      this.outstandingIndexCaseInteractor = new OutstandingIndexCaseInteractor(this);
      this.activity = activity;
    }

    public void saveJsonForm(String json) {
      outstandingIndexCaseInteractor.saveJsonForm(json);
    }

    @Override
    public void onFormSaved(
        @NonNull String structureId,
        String taskID,
        @NonNull Task.TaskStatus taskStatus,
        @NonNull String businessStatus,
        String interventionType) {
      Timber.tag("RevealMap").i("Structure ID  %s taskId %s", structureId, taskID);
    }

    @Override
    public void onStructureAdded(Feature feature, JSONArray featureCoordinates, double zoomlevel) {}

    @Override
    public void onFormSaveFailure(String eventType) {
      Toast.makeText(
              OutstandingIndexCaseActivity.this, "Failure to save form data", Toast.LENGTH_LONG)
          .show();
    }


    public void findLastEvent(String baseEntityId, String eventType) {
      outstandingIndexCaseInteractor.findLastEvent(baseEntityId, eventType);
    }
  }

  public class OutstandingIndexCaseInteractor extends BaseInteractor {

    public OutstandingIndexCaseInteractor(BaseContract.BasePresenter presenter) {
      super(presenter);
    }

    @Override
    public void handleLasteventFound(org.smartregister.domain.Event event) {
      Timber.tag("RevealMap").i(event.toString());
    }
  }
}
