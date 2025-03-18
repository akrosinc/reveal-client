package org.smartregister.reveal.test;

import static org.smartregister.reveal.interactor.BaseInteractor.gson;
import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE_MEMBER;
import static org.smartregister.reveal.util.Constants.Action.RCD_MEMBER;
import static org.smartregister.reveal.util.Constants.BusinessStatus.COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;
import static org.smartregister.reveal.util.Utils.getOperationalAreaLocation;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.Location;
import org.smartregister.domain.PhysicalLocation;
import org.smartregister.domain.Task;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.model.BaseTaskDetails;
import org.smartregister.reveal.model.StructureDetails;

import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.GeoJsonUtils;
import org.smartregister.reveal.util.PreferencesUtil;

class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ActionViewHolder> {

  private final GDRSActivity gdrsActivity;
  @Setter @Getter private List<Action> actions;
  @Setter @Getter private List<Action> fullactions;
  private final String thisCompoundId;

  private final GDRSPresenter gdrsPresenter;

  private final String locationUUID;

  ActionAdapter(
      GDRSActivity gdrsActivity,
      List<Action> actions,
      GDRSPresenter gdrsPresenter,
      String thisCompoundId,
      String locationUUID) {
    this.gdrsActivity = gdrsActivity;
    this.actions = actions;
    this.fullactions = actions;
    this.gdrsPresenter = gdrsPresenter;
    this.thisCompoundId = thisCompoundId;
    this.locationUUID = locationUUID;
  }

  @Override
  public ActionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = gdrsActivity.getLayoutInflater().inflate(R.layout.gdrs_item_action, parent, false);
    return new ActionViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ActionViewHolder holder, int position) {
    Action action = getAction(holder, position);
    Task task = action.getTask();
    Drawable background = holder.actionButton.getBackground();

    holder.householdId.setText(action.getHouseholdId());

    if (task.getAuthoredOn().isBefore(DateTime.now().minusDays(3))) {
      holder.createdDate.setTextColor(
          gdrsActivity.getResources().getColor(android.R.color.holo_red_dark, null));
      holder.oldTaskMessage.setVisibility(View.VISIBLE);
    }

    if (task.getCode().equals(INDEX_CASE_MEMBER)
        || task.getCode().equals(Constants.Action.SECONDARY_INDEX_CASE_MEMBER)) {

      handleIndexCaseOnAdapter(holder, task, background);
    } else {

      handleRcdOnAdapter(holder, task, background);
    }

    holder.actionButton.setOnClickListener(v -> openTaskFromAdapterList(task, action));
  }

  private void openTaskFromAdapterList(Task task, Action action) {
    if (!NOT_VISITED.equals(task.getBusinessStatus())) {
      openNotVisitedTask(task);
    } else {

      openNotVisitedTask(task, action);
    }
  }

  private void openNotVisitedTask(Task task, Action action) {
    BaseTaskDetails details = GDRSActivity.getBaseTaskDetails(task, task.getIdentifier(), null);

    JSONObject formJSON;

    if (task.getCode().equals(INDEX_CASE_MEMBER)
        || task.getCode().equals(Constants.Action.SECONDARY_INDEX_CASE_MEMBER)) {
      if (task.getCode().equals(Constants.Action.SECONDARY_INDEX_CASE_MEMBER)) {
        formJSON =
            gdrsActivity
                .getFormUtils()
                .getFormJSON(
                    gdrsActivity, Constants.JsonForm.GDRS_SECONDARY_INDEX_CASE, details, null);
      } else {
        formJSON =
            gdrsActivity
                .getFormUtils()
                .getFormJSON(gdrsActivity, Constants.JsonForm.GDRS_INDEX_CASE, details, null);
      }
    } else {
      formJSON =
          gdrsActivity
              .getFormUtils()
              .getFormJSON(gdrsActivity, Constants.JsonForm.GDRS_RCD, details, null);
    }

    populateGeneralTaskDetails(formJSON);

    if (task.getCode().equals(INDEX_CASE_MEMBER)
        || task.getCode().equals(Constants.Action.SECONDARY_INDEX_CASE_MEMBER)) {
      formJSON = populateIndexCaseTaskDetails(task, action, details);

    } else {
      formJSON = populateRCDTaskDetails(action, details);
    }
    gdrsActivity.getFormUtils().startJsonForm(formJSON, gdrsActivity);
  }

  private JSONObject populateRCDTaskDetails(Action action, BaseTaskDetails details) {
    JSONObject formJSON;
    formJSON =
        gdrsActivity
            .getFormUtils()
            .getFormJSON(gdrsActivity, Constants.JsonForm.GDRS_RCD, details, null);
    try {
      gdrsActivity
          .getFormUtils()
          .populateField(
              formJSON, GDRSActivity.INDIVIDUAL, action.getIndividualId(), JsonFormConstants.VALUE);
      gdrsActivity
          .getFormUtils()
          .populateField(formJSON, GDRSActivity.DOB, action.getDob(), JsonFormConstants.VALUE);
      gdrsActivity
          .getFormUtils()
          .populateField(
              formJSON, GDRSActivity.GENDER, action.getGender(), JsonFormConstants.VALUE);
      gdrsActivity
          .getFormUtils()
          .populateField(formJSON, GDRSActivity.NAME, action.getName(), JsonFormConstants.VALUE);
      gdrsActivity
          .getFormUtils()
          .setDefaultValue(formJSON, GDRSActivity.DATE, LocalDate.now().toString("dd-MM-yyyy"));
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return formJSON;
  }

  private JSONObject populateIndexCaseTaskDetails(
      Task task, Action action, BaseTaskDetails details) {
    JSONObject formJSON;
    if (task.getCode().equals(Constants.Action.SECONDARY_INDEX_CASE_MEMBER)) {
      formJSON =
          gdrsActivity
              .getFormUtils()
              .getFormJSON(
                  gdrsActivity, Constants.JsonForm.GDRS_SECONDARY_INDEX_CASE, details, null);
    } else {
      formJSON =
          gdrsActivity
              .getFormUtils()
              .getFormJSON(gdrsActivity, Constants.JsonForm.GDRS_INDEX_CASE, details, null);
    }

    try {
      gdrsActivity
          .getFormUtils()
          .populateField(
              formJSON, GDRSActivity.INDIVIDUAL, action.getIndividualId(), JsonFormConstants.VALUE);
      gdrsActivity
          .getFormUtils()
          .populateField(
              formJSON,
              GDRSActivity.HOUSEHOLD,
              action.getTask().getHouseholdId(),
              JsonFormConstants.VALUE);
      gdrsActivity
          .getFormUtils()
          .populateField(formJSON, GDRSActivity.COMPOUND, thisCompoundId, JsonFormConstants.VALUE);
      gdrsActivity
          .getFormUtils()
          .populateField(formJSON, GDRSActivity.STRUCTURE, locationUUID, JsonFormConstants.VALUE);
      gdrsActivity
          .getFormUtils()
          .populateField(formJSON, GDRSActivity.NAME, action.getName(), JsonFormConstants.VALUE);
      gdrsActivity
          .getFormUtils()
          .populateFieldWithOperationalAreas(formJSON, GDRSActivity.OPERATIONAL);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return formJSON;
  }

  private void populateGeneralTaskDetails(JSONObject formJSON) {
    try {
      JSONObject featureCollection = gdrsActivity.createFeatureCollection();
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

      for (Location structure : structures) {
        Set<Task> tasksByEntity =
            gdrsActivity.getTaskRepository().getTasksByEntity(structure.getId());
        map.put(structure.getId(), tasksByEntity);
      }
      Map<String, Boolean> isAHouseholdByStructureList =
          gdrsActivity
              .getHdssRepository()
              .getIsAHouseholdByStructureList(
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
      Toast.makeText(gdrsActivity, "Cannot open geo widget to capture structure", Toast.LENGTH_LONG)
          .show();
    }
    gdrsActivity
        .getFormUtils()
        .setDefaultValue(
            formJSON,
            Constants.JsonForm.HEALTH_WORKER_SUPERVISOR,
            RevealApplication.getInstance()
                .getContext()
                .allSharedPreferences()
                .fetchRegisteredANM());
  }

  private void openNotVisitedTask(Task task) {
    String eventType;
    if (RCD_MEMBER.equals(task.getCode())) {
      eventType = Constants.EventType.RCD_EVENT;
    } else if (INDEX_CASE_MEMBER.equals(task.getCode())) {
      eventType = Constants.EventType.INDEX_CASE_MEMBER_EVENT;
    } else {
      eventType = Constants.EventType.SECONDARY_INDEX_CASE_MEMBER_EVENT;
    }
    gdrsPresenter.findLastEvent(task.getForEntity(), eventType);
  }

  private void handleRcdOnAdapter(ActionViewHolder holder, Task task, Drawable background) {
    if (COMPLETE.equals(task.getBusinessStatus())) {
      if (background instanceof GradientDrawable) {
        ((GradientDrawable) background)
            .setColor(gdrsActivity.getResources().getColor(R.color.pnc_circle_green, null));
      }
      holder.actionButton.setText(R.string.edit_racd);
    } else {
      if (background instanceof GradientDrawable) {
        ((GradientDrawable) background)
            .setColor(gdrsActivity.getResources().getColor(R.color.not_visited_yellow, null));
      }
      holder.actionButton.setText(R.string.action_racd);
    }
    holder.actionButton.setTextColor(gdrsActivity.getResources().getColor(R.color.black, null));
  }

  private void handleIndexCaseOnAdapter(ActionViewHolder holder, Task task, Drawable background) {
    if (task.getCode().equals(INDEX_CASE_MEMBER)) {
      if (COMPLETE.equals(task.getBusinessStatus())) {
        if (background instanceof GradientDrawable) {
          ((GradientDrawable) background)
              .setColor(gdrsActivity.getResources().getColor(R.color.purple, null));
        }

        holder.actionButton.setTextColor(gdrsActivity.getResources().getColor(R.color.cyan, null));
        holder.actionButton.setText(R.string.edit_index_case);
      } else {
        if (background instanceof GradientDrawable) {
          ((GradientDrawable) background)
              .setColor(gdrsActivity.getResources().getColor(R.color.cyan, null));
        }
        holder.actionButton.setText(R.string.confirm_index_case);
      }
    } else {
      if (COMPLETE.equals(task.getBusinessStatus())) {
        if (background instanceof GradientDrawable) {
          ((GradientDrawable) background)
              .setColor(gdrsActivity.getResources().getColor(R.color.pnc_circle_green, null));
        }
        holder.actionButton.setTextColor(
            gdrsActivity.getResources().getColor(R.color.purple, null));
        holder.actionButton.setText(R.string.edit_secondary);
      } else {
        if (background instanceof GradientDrawable) {
          ((GradientDrawable) background)
              .setColor(gdrsActivity.getResources().getColor(R.color.not_visited_yellow, null));
        }
        holder.actionButton.setTextColor(gdrsActivity.getResources().getColor(R.color.cyan, null));
        holder.actionButton.setText(R.string.confirm_secondary);
      }
    }
  }

  private @NonNull Action getAction(ActionViewHolder holder, int position) {
    Action action = actions.get(position);
    holder.gender.setText(action.getGender());
    holder.individualId.setText(action.getIndividualId());
    holder.dob.setText(action.getDob());
    holder.createdDate.setText(action.getCreatedDate());
    holder.name.setText(action.getName());
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
    TextView createdDate;
    TextView name;
    TextView oldTaskMessage;
    TextView householdId;
    Button actionButton;

    ActionViewHolder(View itemView) {
      super(itemView);
      individualId = itemView.findViewById(R.id.individualId);
      gender = itemView.findViewById(R.id.individualGender);
      dob = itemView.findViewById(R.id.individualDob);
      actionButton = itemView.findViewById(R.id.actionButton);
      createdDate = itemView.findViewById(R.id.createdDate);
      name = itemView.findViewById(R.id.individualName);
      oldTaskMessage = itemView.findViewById(R.id.oldTaskMessage);
      householdId = itemView.findViewById(R.id.individualHousehold);
      individualId.setOnLongClickListener(
          view -> {
            gdrsActivity.copyToClipboard(individualId.getText().toString());
            return true; // Indicates that the long click was handled
          });
    }
  }
}
