package org.smartregister.reveal.view;

import static org.smartregister.reveal.interactor.BaseInteractor.gson;
import static org.smartregister.reveal.test.GDRSActivity.HOUSEHOLD;
import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE;
import static org.smartregister.reveal.util.Constants.Action.RCD;
import static org.smartregister.reveal.util.Constants.Action.RCD_MEMBER;
import static org.smartregister.reveal.util.Constants.Action.SECONDARY_INDEX_CASE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.INDEX_CASE_NOT_VISITED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;
import static org.smartregister.reveal.util.Constants.JSON_FORM_PARAM_JSON;
import static org.smartregister.reveal.util.Constants.JsonForm.ENCOUNTER_TYPE;
import static org.smartregister.reveal.util.Constants.JsonForm.GDRS_INDEX_CASE_FLOATING_HOUSEHOLD;
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.HdssCompoundHousehold;
import org.smartregister.domain.HdssHousehold;
import org.smartregister.domain.HdssHouseholdStructure;
import org.smartregister.domain.HdssIndividual;
import org.smartregister.domain.Location;
import org.smartregister.domain.PhysicalLocation;
import org.smartregister.domain.StructureTaskForCompound;
import org.smartregister.domain.Task;
import org.smartregister.repository.HdssRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.BaseContract;
import org.smartregister.reveal.contract.OtherFormsContract;
import org.smartregister.reveal.interactor.BaseInteractor;
import org.smartregister.reveal.model.BaseTaskDetails;
import org.smartregister.reveal.model.StructureDetails;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.GeoJsonUtils;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import org.smartregister.reveal.util.TaskUtils;
import org.smartregister.reveal.view.OutstandingIndexCaseHouseholdActivity.HouseholdIndexCaseAdapter.HouseholdIndexCaseViewHolder;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.activity.MultiLanguageActivity;
import timber.log.Timber;

/** Created by Richard Kareko on 9/22/20. */
public class OutstandingIndexCaseHouseholdActivity extends MultiLanguageActivity {

  private RecyclerView recyclerView;

  private HouseholdIndexCaseAdapter householdIndexCaseAdapter;

  private HdssRepository hdssRepository;

  private RevealJsonFormUtils formUtils;

  private TaskRepository taskRepository;
  private OutstandingHouseholdIndexCasePresenter presenter;

  private TaskUtils taskUtils;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    hdssRepository = RevealApplication.getInstance().getHdssRepository();
    formUtils = new RevealJsonFormUtils();
    taskRepository = RevealApplication.getInstance().getTaskRepository();
    presenter = new OutstandingHouseholdIndexCasePresenter(this);
    taskUtils = TaskUtils.getInstance();

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
    setupClearButton();
    setupSearchfield();
    DividerItemDecoration dividerItemDecoration =
        new DividerItemDecoration(
            recyclerView.getContext(),
            ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation());
    recyclerView.addItemDecoration(dividerItemDecoration);
    populateActionList();
  }

  private void setupClearButton() {
    Button clearButton = findViewById(R.id.clearSearchButton);
    EditText editText = findViewById(R.id.searchField);
    clearButton.setOnClickListener(
        v -> {
          editText.setText("");
          filterList("");
        });
  }

  private void setupSearchfield() {
    EditText editText = findViewById(R.id.searchField);

    editText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            Timber.tag("Reveal").i("text changes %s", s.toString());
            filterList(s.toString());
            Button clearButton = findViewById(R.id.clearSearchButton);
            clearButton.setEnabled(!s.toString().isEmpty());
          }
        });
  }

  public void filterList(String query) {
    if (householdIndexCaseAdapter == null) {
      Timber.tag("RevealMap").i("householdIndexCaseAdapter is null");
      return;
    }
    if (!query.isEmpty()) {
      List<HouseholdIndexCase> filtered = new ArrayList<>();
      for (HouseholdIndexCase action : householdIndexCaseAdapter.getFullHouseholdIndexCases()) {
        // You can filter based on any fields of the Action object

        if (action.getHouseholdId().toLowerCase().contains(query.toLowerCase())) {
          filtered.add(action);
        }
        Timber.tag("Reveal").i("results %s", filtered);
        householdIndexCaseAdapter.setHouseholdIndexCases(filtered);
        householdIndexCaseAdapter
            .notifyDataSetChanged(); // Notify the adapter that the data has changed
      }
    } else {
      householdIndexCaseAdapter.setHouseholdIndexCases(householdIndexCaseAdapter.getFullHouseholdIndexCases());
      householdIndexCaseAdapter.notifyDataSetChanged();
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
        JSONObject compound = JsonFormUtils.getFieldJSONObject(fields, "compound");
        JSONObject household = JsonFormUtils.getFieldJSONObject(fields, "household");

        if ("index_case_household_floating".equals(encounter)) {
          String geoStructureValue = null;
          if (geoStructure != null) {
            try {
              geoStructureValue = geoStructure.getString("value");
            } catch (Exception e) {
              Timber.tag("Reveal Exception").i("GDRSActivity: Err 3");
            }
          }
          String householdValue = null;
          if (household != null) {

            try {
              householdValue = household.getString("value");
            } catch (Exception e) {
              Timber.tag("Reveal Exception").i("GDRSActivity: Err 3");
            }
          }
          if (geoStructureValue != null && householdValue != null) {

            handleIndexCaseToNewStructure(planId, geoStructureValue, householdValue);
          }
        }
      } catch (JSONException e) {

      }

      presenter.saveJsonForm(json);
      populateActionList();
    }
  }

  public void populateActionList() {

    List<HouseholdIndexCase> householdIndexCases = new ArrayList<>();

    List<HdssHousehold> householdsForOperationalArea =
        hdssRepository.getHouseholdsForOperationalArea(
            PreferencesUtil.getInstance().getCurrentOperationalArea());

    for (HdssHousehold hdssHousehold : householdsForOperationalArea) {

      HouseholdIndexCase householdIndexCase =
          new HouseholdIndexCase(hdssHousehold.getHouseholdId());

      householdIndexCases.add(householdIndexCase);
    }

    if (householdIndexCaseAdapter != null) {
      householdIndexCaseAdapter.setHouseholdIndexCases(householdIndexCases);
      householdIndexCaseAdapter.setFullHouseholdIndexCases(householdIndexCases);
      householdIndexCaseAdapter.notifyDataSetChanged();
    } else {
      householdIndexCaseAdapter = new HouseholdIndexCaseAdapter(householdIndexCases);
      recyclerView.setAdapter(householdIndexCaseAdapter);
    }
  }

  private static class HouseholdIndexCase {
    @Getter private final String householdId;

    HouseholdIndexCase(String householdId) {
      this.householdId = householdId;
    }
  }

  private void handleIndexCaseToNewStructure(
      String planId, String geoStructureValue, String currentHouseholdIdOfIndexCaseString)
      throws JSONException {

    HdssCompoundHousehold householdIdCompoundIdInSelectedStructure =
        hdssRepository.getHouseholdIdCompoundIdByStructureId(geoStructureValue);

    List<StructureTaskForCompound> structuresAndTasksForCompound = new ArrayList<>();

    if (householdIdCompoundIdInSelectedStructure != null) {
      structuresAndTasksForCompound =
          hdssRepository.getStructuresAndTasksForCompound(
              householdIdCompoundIdInSelectedStructure.getCompoundId(), planId);
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
        if (structureCompoundTask.getStructureId().equals(geoStructureValue)) {

        } else {
          potentialStructuresForRCDTaskGeneration.add(structureCompoundTask);
        }
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

    for (StructureTaskForCompound task : potentialStructuresForRCDTaskGeneration) {
      taskUtils.generateTask(
          this, task.getStructureId(), task.getStructureId(), NOT_VISITED, RCD, R.string.rcd);
      Map<String, HdssIndividual> individualsByStructureId =
          hdssRepository.getIndividualsByStructureId(task.getStructureId());
      for (Map.Entry<String, HdssIndividual> entry : individualsByStructureId.entrySet()) {
        taskUtils.generateTask(
            this,
            entry.getKey(),
            task.getStructureId(),
            NOT_VISITED,
            RCD_MEMBER,
            R.string.rcd_member);
      }
    }

    if (householdIdCompoundIdInSelectedStructure != null
        && householdIdCompoundIdInSelectedStructure.getHouseholdId() != null) {
      int maxServerVersion = hdssRepository.getMaxServerVersion();

      HdssHouseholdStructure householdStructure =
          HdssHouseholdStructure.builder()
              .structureId(geoStructureValue)
              .householdId(currentHouseholdIdOfIndexCaseString)
              .serverVersion((long) maxServerVersion)
              .build();
      hdssRepository.addOrUpdateHouseholdStructure(List.of(householdStructure));

      HdssHousehold householdObj =
          HdssHousehold.builder()
              .householdId(currentHouseholdIdOfIndexCaseString)
              .floatingHouseholdLocationName(null)
              .serverVersion((long) maxServerVersion)
              .build();
      hdssRepository.addOrUpdateHousehold(List.of(householdObj));
    }

    if (householdIdCompoundIdInSelectedStructure != null
        && householdIdCompoundIdInSelectedStructure.getCompoundId() != null) {
      int maxServerVersion = hdssRepository.getMaxServerVersion();

      HdssCompoundHousehold compoundHousehold =
          HdssCompoundHousehold.builder()
              .compoundId(householdIdCompoundIdInSelectedStructure.getCompoundId())
              .householdId(currentHouseholdIdOfIndexCaseString)
              .serverVersion(maxServerVersion)
              .build();
      hdssRepository.addOrUpdateCompoundHouseholds(List.of(compoundHousehold));
    }
  }

  private JSONObject createFeatureCollection() throws JSONException {
    JSONObject featureCollection = new JSONObject();
    featureCollection.put(Constants.GeoJSON.TYPE, Constants.GeoJSON.FEATURE_COLLECTION);
    return featureCollection;
  }

  @Setter
  class HouseholdIndexCaseAdapter extends RecyclerView.Adapter<HouseholdIndexCaseViewHolder> {

    private List<HouseholdIndexCase> householdIndexCases;

    @Getter private List<HouseholdIndexCase> fullHouseholdIndexCases;

    public HouseholdIndexCaseAdapter(List<HouseholdIndexCase> householdIndexCases) {
      this.householdIndexCases = householdIndexCases;
      this.fullHouseholdIndexCases = householdIndexCases;
    }

    @NonNull
    @Override
    public HouseholdIndexCaseViewHolder onCreateViewHolder(
        @NonNull ViewGroup parent, int viewType) {
      View view = getLayoutInflater().inflate(R.layout.gdrs_item_action_household, parent, false);
      return new HouseholdIndexCaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HouseholdIndexCaseViewHolder holder, int position) {
      HouseholdIndexCase householdIndexCase = getIndexCase(holder, position);

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
                      OutstandingIndexCaseHouseholdActivity.this,
                      GDRS_INDEX_CASE_FLOATING_HOUSEHOLD,
                      null,
                      null);

              formUtils.populateField(
                  formJSON,
                  HOUSEHOLD,
                  householdIndexCase.getHouseholdId(),
                  JsonFormConstants.VALUE);
              formUtils.startJsonForm(formJSON, OutstandingIndexCaseHouseholdActivity.this);
            } catch (Exception e) {
              Toast.makeText(
                      OutstandingIndexCaseHouseholdActivity.this,
                      "Cannot open geo widget to capture structure",
                      Toast.LENGTH_LONG)
                  .show();
            }
          });
    }

    private @NonNull HouseholdIndexCase getIndexCase(
        HouseholdIndexCaseViewHolder holder, int position) {
      HouseholdIndexCase action = householdIndexCases.get(position);
      holder.householdId.setText(action.getHouseholdId());
      return action;
    }

    @Override
    public int getItemCount() {
      return householdIndexCases.size();
    }

    class HouseholdIndexCaseViewHolder extends RecyclerView.ViewHolder {

      TextView householdId;
      Button actionButton;

      HouseholdIndexCaseViewHolder(View itemView) {
        super(itemView);
        householdId = itemView.findViewById(R.id.householdId);
        actionButton = itemView.findViewById(R.id.actionButton);
      }
    }
  }

  public class OutstandingHouseholdIndexCasePresenter implements OtherFormsContract.Presenter {

    private final OutstandingHouseholdIndexCaseInteractor outstandingHouseholdIndexCaseInteractor;

    private final OutstandingIndexCaseHouseholdActivity activity;

    public OutstandingHouseholdIndexCasePresenter(OutstandingIndexCaseHouseholdActivity activity) {
      this.outstandingHouseholdIndexCaseInteractor =
          new OutstandingHouseholdIndexCaseInteractor(this);
      this.activity = activity;
    }

    public void saveJsonForm(String json) {
      outstandingHouseholdIndexCaseInteractor.saveJsonForm(json);
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
              OutstandingIndexCaseHouseholdActivity.this,
              "Failure to save form data",
              Toast.LENGTH_LONG)
          .show();
    }


    public void findLastEvent(String baseEntityId, String eventType) {
      outstandingHouseholdIndexCaseInteractor.findLastEvent(baseEntityId, eventType);
    }
  }

  public class OutstandingHouseholdIndexCaseInteractor extends BaseInteractor {

    public OutstandingHouseholdIndexCaseInteractor(BaseContract.BasePresenter presenter) {
      super(presenter);
    }

    @Override
    public void handleLasteventFound(org.smartregister.domain.Event event) {
      Timber.tag("RevealMap").i(event.toString());
    }
  }
}
