package org.smartregister.reveal.test;

import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE_MEMBER;
import static org.smartregister.reveal.util.Utils.getOperationalAreaLocation;

import android.widget.Toast;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.domain.Location;
import org.smartregister.domain.PhysicalLocation;
import org.smartregister.domain.Task;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.BaseContract;
import org.smartregister.reveal.interactor.BaseInteractor;
import org.smartregister.reveal.model.BaseTaskDetails;
import org.smartregister.reveal.model.StructureDetails;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.GeoJsonUtils;
import org.smartregister.reveal.util.PreferencesUtil;

public class GDRSInteractor extends BaseInteractor {

  private final GDRSActivity activity;
  private final String thisCompoundId;
  private final String locationUUID;
  public GDRSInteractor(BaseContract.BasePresenter presenter, GDRSActivity gdrsActivity, String thisCompoundId, String locationUUID) {
    super(presenter);
    this.activity = gdrsActivity;
    this.thisCompoundId = thisCompoundId;
    this.locationUUID = locationUUID;
  }

  @Override
  public void handleLasteventFound(org.smartregister.domain.Event event) {

    if (event != null) {

      String taskID = event.getDetails().get(Constants.Properties.TASK_IDENTIFIER);

      Task task = taskRepository.getTaskByIdentifier(taskID);

      HdssLocation location = GDRSActivity.getHdssLocation(locationUUID);

      BaseTaskDetails details = GDRSActivity.getBaseTaskDetails(task, task.getIdentifier(), locationUUID);
      //                PreferencesUtil.getInstance().setSelectedHouseholdID(houseHoldIds);
      JSONObject formJSON;

      if (task.getCode().equals(INDEX_CASE_MEMBER)
          || task.getCode().equals(Constants.Action.SECONDARY_INDEX_CASE_MEMBER)) {
        if (task.getCode().equals(Constants.Action.SECONDARY_INDEX_CASE_MEMBER)) {
          formJSON =
              activity.getFormUtils().getFormJSON(
                  activity,
                  Constants.JsonForm.GDRS_SECONDARY_INDEX_CASE,
                  details,
                  location);
        } else {
          formJSON =
              activity.getFormUtils().getFormJSON(
                  activity, Constants.JsonForm.GDRS_INDEX_CASE, details, location);
        }

      } else {
        formJSON =
            activity.getFormUtils().getFormJSON(
                activity, Constants.JsonForm.GDRS_RCD, details, location);
      }
      try {
        JSONObject featureCollection = activity.createFeatureCollection();
        Location operationalAreaLocation =
            getOperationalAreaLocation(PreferencesUtil.getInstance().getCurrentOperationalArea());
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
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));

        Map<String, Set<Task>> map = new HashMap<>();

        Set<Task> tasksForMap = new HashSet<>();
        for (Location structure : structures) {
          Set<Task> tasksByEntity = taskRepository.getTasksByEntity(structure.getId());
          //                                    tasksForMap.addAll(tasksByEntity);
          map.put(structure.getId(), tasksByEntity);
        }

        //                                Set<Task> set = Set.of(task);

        Map<String, Boolean> isAHouseholdByStructureList =
            activity.getHdssRepository().getIsAHouseholdByStructureList(
                structures.stream().map(PhysicalLocation::getId).collect(Collectors.toList()));

        String features =
            GeoJsonUtils.getGeoJsonFromStructuresAndTasksForGdrs(
                structures, map, null, collect, null, isAHouseholdByStructureList);

        featureCollection.put(Constants.GeoJSON.FEATURES, new JSONArray(features));
        RevealApplication.getInstance()
            .setFeatureCollection(FeatureCollection.fromJson(featureCollection.toString()));
        RevealApplication.getInstance()
            .setOperationalArea(Feature.fromJson(gson.toJson(operationalAreaLocation)));
      } catch (Exception e) {
        Toast.makeText(
                activity,
                "Cannot open geo widget to capture structure",
                Toast.LENGTH_LONG)
            .show();
      }

      activity.getFormUtils().populateForm(event, formJSON);
      activity.getFormUtils().startJsonForm(formJSON, activity);
    }
  }
}
