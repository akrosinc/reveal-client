package org.smartregister.reveal.view;

import static org.smartregister.reveal.interactor.BaseInteractor.gson;
import static org.smartregister.reveal.test.GDRSActivity.INDIVIDUAL;
import static org.smartregister.reveal.test.GDRSActivity.NAME;
import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE;
import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE_MEMBER;
import static org.smartregister.reveal.util.Constants.Action.RCD;
import static org.smartregister.reveal.util.Constants.Action.RCD_MEMBER;
import static org.smartregister.reveal.util.Constants.Action.SECONDARY_INDEX_CASE;
import static org.smartregister.reveal.util.Constants.Action.SECONDARY_INDEX_CASE_MEMBER;
import static org.smartregister.reveal.util.Constants.BusinessStatus.INDEX_CASE_NOT_VISITED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.SECONDARY_INDEX_CASE_NOT_VISITED;
import static org.smartregister.reveal.util.Constants.JSON_FORM_PARAM_JSON;
import static org.smartregister.reveal.util.Constants.JsonForm.ENCOUNTER_TYPE;
import static org.smartregister.reveal.util.Constants.JsonForm.GDRS_INDEX_CASE_FLOATING;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_GET_JSON;
import static org.smartregister.reveal.util.Utils.getOperationalAreaLocation;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.HdssCompound;
import org.smartregister.domain.HdssCompoundHousehold;
import org.smartregister.domain.HdssHouseholdIndividual;
import org.smartregister.domain.HdssHouseholdStructure;
import org.smartregister.domain.HdssIndividual;
import org.smartregister.domain.IndividualsAndTasksForCompound;
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
import org.smartregister.reveal.test.GDRSActivity;
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

/**
 * Created by Richard Kareko on 9/22/20.
 */

public class OutstandingIndexCaseActivity extends MultiLanguageActivity {

    private RecyclerView recyclerView;

    private OutstandingIndexCaseActivity.IndexCaseAdapter indexCaseAdapter;

    private HdssRepository hdssRepository;

    private RevealJsonFormUtils formUtils;

    private TaskRepository taskRepository;
    private OutstandingIndexCasePresenter presenter;

    private TaskUtils taskUtils;


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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hdssRepository = RevealApplication.getInstance().getHdssRepository();
        formUtils = new RevealJsonFormUtils();
        taskRepository = RevealApplication.getInstance().getTaskRepository();
        presenter = new OutstandingIndexCasePresenter(this);
        taskUtils = TaskUtils.getInstance();

        setContentView(R.layout.activity_index_cases);

        TextView textView = findViewById(R.id.textViewCenter);
        textView.setText(PreferencesUtil.getInstance().getCurrentOperationalArea());

        Toolbar toolbar = this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        populateActionList();
        recyclerView.setAdapter(indexCaseAdapter);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GET_JSON && resultCode == RESULT_OK && data != null
                && data.hasExtra(JSON_FORM_PARAM_JSON)) {
            String json = data.getStringExtra(JSON_FORM_PARAM_JSON);
            String planId = PreferencesUtil.getInstance().getCurrentPlanId();
            try {
                JSONObject jsonForm = new JSONObject(json);
                String encounter = jsonForm.optString(ENCOUNTER_TYPE);
                JSONArray fields = JsonFormUtils.fields(jsonForm);
                JSONObject individualObj = JsonFormUtils.getFieldJSONObject(fields, "individual");
                JSONObject geoStructure = JsonFormUtils.getFieldJSONObject(fields, "geo_structure");
                JSONObject compound = JsonFormUtils.getFieldJSONObject(fields,"compound");
                JSONObject household = JsonFormUtils.getFieldJSONObject(fields,"household");

                if ("index_case_member_floating".equals(encounter)){
                    String individualIdValue = null;
                    if (individualObj != null) {
                        try {
                            individualIdValue = individualObj.getString("value");
                        } catch (Exception e) {
                            Timber.tag("Reveal Exception").i("GDRSActivity: Err 1");
                        }


                        if (individualIdValue != null) {
                            if (geoStructure != null) {

                                String geoStructureValue = null;
                                try {
                                    geoStructureValue = geoStructure.getString("value");
                                } catch (Exception e) {
                                    Timber.tag("Reveal Exception").i("GDRSActivity: Err 3");

                                }

                                if (geoStructureValue != null) {
                                        if (household!=null){

                                            hdssRepository.removeHouseholdFromStructure(geoStructureValue);

                                            String householdIdValue = household.getString("value");

                                            HdssCompoundHousehold householdIdCompoundIdByStructureId = hdssRepository.getHouseholdIdCompoundIdByStructureId(geoStructureValue);

                                            if (householdIdCompoundIdByStructureId != null && householdIdValue!=null){
                                                int maxServerVersion = hdssRepository.getMaxServerVersion();
                                                maxServerVersion++;

                                                HdssHouseholdStructure householdStructure = HdssHouseholdStructure.builder()
                                                        .structureId(geoStructureValue)
                                                        .householdId(householdIdCompoundIdByStructureId.getHouseholdId())
                                                        .serverVersion((long) maxServerVersion)
                                                        .build();
                                                hdssRepository.addOrUpdateHouseholdStructure(List.of(householdStructure));

                                                hdssRepository.removeHouseholdFromCompound(householdIdValue);

                                                HdssCompoundHousehold compoundHousehold = HdssCompoundHousehold
                                                        .builder()
                                                        .compoundId(householdIdCompoundIdByStructureId.getCompoundId())
                                                        .householdId(householdIdValue)
                                                        .serverVersion(maxServerVersion)
                                                        .build();

                                                hdssRepository.addOrUpdateCompoundHouseholdsBatched(List.of(compoundHousehold));
                                            }
                                        }


//                                    String household ByStructureId = hdssRepository.getHouseholdIdByStructureId(finalGeoStructureValue);
//                                    if (householdIdByStructureId != null && !householdIdByStructureId.isEmpty()) {
//
//                                        List<StructureTaskForCompound> structuresAndTasksForCompoundByHouseholdId
//                                                = hdssRepository.getStructuresAndTasksForCompoundByHouseholdId(householdIdByStructureId, planId);
//
//                                        boolean hasExistingIndexCase = false;
//                                        List<StructureTaskForCompound> potentialStructuresForTaskGeneration = new ArrayList<>();
//                                        for (StructureTaskForCompound structure : structuresAndTasksForCompoundByHouseholdId) {
//
//                                            if (structure.getBusinessStatus() != null && structure.getStatus() != null && structure.getCode() != null &&
//                                                    !structure.getCode().equals(RCD)) {
//                                                if (structure.getCode().equals(INDEX_CASE)) {
//                                                    if (!structure.getStatus().equals(Task.TaskStatus.CANCELLED.toString())
//                                                            && structure.getBusinessStatus().equals(INDEX_CASE_NOT_VISITED)) {
//                                                        hasExistingIndexCase = true;
//                                                    }
//                                                }
//                                            } else {
//                                                potentialStructuresForTaskGeneration.add(structure);
//                                            }
//                                        }
//
//                                        for (StructureTaskForCompound taskForCompound : potentialStructuresForTaskGeneration) {
//                                            if (taskForCompound.getBusinessStatus() != null && taskForCompound.getStatus() != null && taskForCompound.getCode() != null) {
//                                                if (taskForCompound.getStructureId().equals(finalGeoStructureValue) && taskForCompound.getCode().equals(RCD)) {
//                                                    taskRepository.cancelTaskByIdentifier(taskForCompound.getTaskId());
//                                                }
//                                            } else {
//                                                if (!taskForCompound.getStructureId().equals(finalGeoStructureValue)) {
//                                                    taskUtils.generateTask(this, taskForCompound.getStructureId(), finalGeoStructureValue, NOT_VISITED, RCD, R.string.rcd);
//                                                }
//                                            }
//                                        }
//
//                                        List<IndividualsAndTasksForCompound> individualsAndTasksForCompoundByHouseholdId = hdssRepository.getIndividualsAndTasksForCompoundByHouseholdId(householdIdByStructureId, planId);
//
//                                        for (IndividualsAndTasksForCompound individualsAndTasksForCompound :
//                                                individualsAndTasksForCompoundByHouseholdId) {
//                                            if (individualsAndTasksForCompound.getBusinessStatus() == null) {
//                                                taskUtils.generateTask(this, individualsAndTasksForCompound.getIndividualIdentifier(), finalGeoStructureValue, NOT_VISITED, RCD_MEMBER, R.string.rcd);
//                                            }
//                                        }
//
//                                        hdssRepository.addIndividualToHousehold(householdIdByStructureId, individualIdValue);
//                                        long hdssMaxServerVersion = hdssRepository.getMaxServerVersion();
//
//                                        hdssMaxServerVersion++;
//
//                                        HdssIndividual individualsByIndividualId = hdssRepository.getIndividualByIndividualId(individualIdValue);
//
//                                        individualsByIndividualId.setServerVersion(hdssMaxServerVersion);
//                                        individualsByIndividualId.setFloatingLocationName(null);
//                                        Timber.tag("RevealMap").i("OutstandingIndexCaseActivity: operationalValue 2");
//                                        hdssRepository.addOrUpdateIndividual(List.of(individualsByIndividualId));
//                                        Timber.tag("RevealMap").i("OutstandingIndexCaseActivity: operationalValue 4");
//
//                                        if (!hasExistingIndexCase) {
//                                            taskUtils.generateTask(this, finalGeoStructureValue, finalGeoStructureValue, INDEX_CASE_NOT_VISITED, INDEX_CASE, R.string.index_case);
//                                            taskUtils.generateTask(this, individualIdValue, finalGeoStructureValue, NOT_VISITED, INDEX_CASE_MEMBER, R.string.index_case);
//                                        } else {
//                                            taskUtils.generateTask(this, finalGeoStructureValue, finalGeoStructureValue, SECONDARY_INDEX_CASE_NOT_VISITED, SECONDARY_INDEX_CASE, R.string.index_case);
//                                            taskUtils.generateTask(this, individualIdValue, finalGeoStructureValue, NOT_VISITED, SECONDARY_INDEX_CASE_MEMBER, R.string.index_case);
//                                        }
//
//                                        Timber.tag("RevealMap").i("on  onActivityResult after generate task");
//                                    } else {
//                                        Timber.tag("RevealMap").i("no existing household / compound");
//
//                                        if (compound!=null && household!=null){
//                                            String householdValue = household.getString("value");
//                                            String compoundValue = compound.getString("value");
//                                            if (householdValue != null && !householdValue.isEmpty() && compoundValue!=null && !compoundValue.isEmpty()){
//
//                                                int maxServerVersion = hdssRepository.getMaxServerVersion();
//
//                                                HdssCompound hdssCompound = HdssCompound.builder().compoundId(compoundValue)
//                                                        .serverVersion(maxServerVersion).build();
//                                                hdssRepository.addOrUpdateCompounds(List.of(hdssCompound));
//
//                                                HdssCompoundHousehold compoundHousehold = HdssCompoundHousehold.builder()
//                                                        .householdId(householdValue)
//                                                        .compoundId(compoundValue)
//                                                        .serverVersion(maxServerVersion)
//                                                        .build();
//                                                hdssRepository.addOrUpdateCompoundHouseholds(List.of(compoundHousehold));
//
//                                                HdssHouseholdStructure hdssHouseholdStructure =
//                                                        HdssHouseholdStructure.builder()
//                                                                .householdId(householdValue)
//                                                                .structureId(finalGeoStructureValue)
//                                                                .serverVersion(maxServerVersion)
//                                                                .build();
//                                                hdssRepository.addOrUpdateHouseholdStructure(List.of(hdssHouseholdStructure));
//
//                                                HdssHouseholdIndividual hdssHouseholdIndividual = HdssHouseholdIndividual
//                                                        .builder()
//                                                        .householdId(householdValue)
//                                                        .individualId(individualIdValue)
//                                                        .serverVersion(maxServerVersion)
//                                                        .build();
//
//                                                hdssRepository.addOrUpdateHouseholdIndividual(List.of(hdssHouseholdIndividual));
//
//                                                HdssIndividual individualByIndividualId = hdssRepository.getIndividualByIndividualId(individualIdValue);
//                                                individualByIndividualId.setFloatingLocationName(null);
//                                                individualByIndividualId.setServerVersion(maxServerVersion);
//
//                                                hdssRepository.addOrUpdateIndividual(List.of(individualByIndividualId));
//
//                                                taskUtils.generateTask(this, finalGeoStructureValue, finalGeoStructureValue, INDEX_CASE_NOT_VISITED, INDEX_CASE, R.string.index_case);
//                                                taskUtils.generateTask(this, individualIdValue, finalGeoStructureValue, NOT_VISITED, INDEX_CASE_MEMBER, R.string.index_case);
//                                            }
//                                        }
//                                    }
                                }
                            }
                        }
                    }
                }

            } catch (JSONException e){

            }

            presenter.saveJsonForm(json);
            populateActionList();

        }
    }

    public void populateActionList() {

        List<IndexCase> indexCases = new ArrayList<>();

        List<HdssIndividual> individualsForOperationalArea = hdssRepository.getIndividualsForOperationalArea(PreferencesUtil.getInstance().getCurrentOperationalArea());

        for (HdssIndividual individual : individualsForOperationalArea) {
            Set<Task> tasks = taskRepository.getTasksByEntity(individual.getIdentifier());
            Task task1 = null;
            if (tasks != null && !tasks.isEmpty()){
                task1 = tasks.stream().findFirst().get();
            }
            IndexCase indexCase = new IndexCase(individual.getIdentifier(),individual.getIndividualId(), individual.getGender(), individual.getDob(),task1, individual.getName());

            indexCases.add(indexCase);
        }

        if (indexCaseAdapter != null) {
            indexCaseAdapter.setIndexCases(indexCases);
            indexCaseAdapter.notifyDataSetChanged();
        } else {
            indexCaseAdapter = new IndexCaseAdapter(indexCases);
        }
    }

    private static class IndexCase {
        @Getter
        private final String individualId;

        @Getter
        private final String id;
        @Getter
        private final String gender;
        @Getter
        private final String name;
        @Getter
        private final String dob;
//        @Getter
//        private final String createdDate;
        @Getter
        private final Task task;

        IndexCase(String id, String individualId, String gender, String dob
//                , String createdDate
                , Task task
                , String name) {
            this.individualId = individualId;
            this.id = id;
            this.gender = gender;
            this.dob = dob;
            this.task = task;
//            this.createdDate = createdDate;
            this.name = name;
        }

    }

    private JSONObject createFeatureCollection() throws JSONException {
        JSONObject featureCollection = new JSONObject();
        featureCollection.put(Constants.GeoJSON.TYPE, Constants.GeoJSON.FEATURE_COLLECTION);
        return featureCollection;
    }

    @Setter
    private class IndexCaseAdapter extends RecyclerView.Adapter<IndexCaseAdapter.IndexCaseViewHolder> {

        private List<IndexCase> indexCases;

        public IndexCaseAdapter(List<IndexCase> indexCases) {
            this.indexCases = indexCases;
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

            actionButton.setOnClickListener(v -> {
                try {
                    JSONObject featureCollection = createFeatureCollection();
                    Location operationalAreaLocation = getOperationalAreaLocation(PreferencesUtil.getInstance().getCurrentOperationalArea());
                    List<Location> structures = RevealApplication.getInstance().getContext().getStructureRepository().getLocationsByParentIdForGdrs(operationalAreaLocation.getId(), "structure");

                    Map<String, StructureDetails> collect = structures.stream().map(structure -> new AbstractMap.SimpleEntry<>(structure.getId()
                                    , new StructureDetails(structure.getProperties().getName(), structure.getProperties().getName())))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));

                    Map<String, Set<Task>> map = new HashMap<>();

                    for (Location structure: structures){
                        Set<Task> tasksByEntity = taskRepository.getTasksByEntity(structure.getId());
                        map.put(structure.getId(), tasksByEntity);
                    }
                    Map<String, Boolean> isAHouseholdByStructureList =  hdssRepository.getIsAHouseholdByStructureList(structures.stream().map(PhysicalLocation::getId).collect(Collectors.toList()));

                    String features = GeoJsonUtils
                            .getGeoJsonFromStructuresAndTasksForGdrs(structures, map, null, collect,null,isAHouseholdByStructureList);

                    featureCollection.put(Constants.GeoJSON.FEATURES, new JSONArray(features));
                    RevealApplication.getInstance().setFeatureCollection(FeatureCollection.fromJson(featureCollection.toString()));
                    RevealApplication.getInstance().setOperationalArea(
                            Feature.fromJson(gson.toJson(operationalAreaLocation)));

                    JSONObject formJSON = formUtils.getFormJSON(OutstandingIndexCaseActivity.this, GDRS_INDEX_CASE_FLOATING, null, null);

                    formUtils.populateField(formJSON, INDIVIDUAL, indexCase.getIndividualId(), JsonFormConstants.VALUE);
                    formUtils.populateField(formJSON, NAME, indexCase.getName(), JsonFormConstants.VALUE);
                    formUtils.startJsonForm(formJSON, OutstandingIndexCaseActivity.this);
                } catch (Exception e) {
                    Toast.makeText(OutstandingIndexCaseActivity.this, "Cannot open geo widget to capture structure", Toast.LENGTH_LONG).show();
                }
            });
        }

        private @NonNull IndexCase getIndexCase(IndexCaseViewHolder holder, int position) {
            IndexCase action = indexCases.get(position);
            holder.gender.setText(action.getGender());
            holder.individualId.setText(action.getIndividualId());
            holder.dob.setText(action.getDob());
//            holder.createdDate.setText(action.createdDate);
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
            TextView createdDate;
            TextView name;
            TextView oldTaskMessage;
            Button actionButton;

            IndexCaseViewHolder(View itemView) {
                super(itemView);
                individualId = itemView.findViewById(R.id.individualId);
                gender = itemView.findViewById(R.id.individualGender);
                dob = itemView.findViewById(R.id.individualDob);
                actionButton = itemView.findViewById(R.id.actionButton);
                createdDate = itemView.findViewById(R.id.createdDate);
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
        public void onFormSaved(@NonNull String structureId, String taskID, @NonNull Task.TaskStatus taskStatus, @NonNull String businessStatus, String interventionType) {
            Timber.tag("RevealMap").i("Structure ID  %s taskId %s" ,structureId, taskID );
        }

        @Override
        public void onStructureAdded(Feature feature, JSONArray featureCoordinates, double zoomlevel) {

        }

        @Override
        public void onFormSaveFailure(String eventType) {
            Toast.makeText(OutstandingIndexCaseActivity.this, "Failure to save form data", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFamilyFound(CommonPersonObjectClient finalFamily) {

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

