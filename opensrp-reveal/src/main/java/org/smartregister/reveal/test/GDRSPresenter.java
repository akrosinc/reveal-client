package org.smartregister.reveal.test;

import static org.smartregister.repository.BaseRepository.TYPE_Unsynced;
import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE;
import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE_MEMBER;
import static org.smartregister.reveal.util.Constants.Action.RCD;
import static org.smartregister.reveal.util.Constants.Action.RCD_MEMBER;
import static org.smartregister.reveal.util.Constants.Action.SECONDARY_INDEX_CASE_MEMBER;
import static org.smartregister.reveal.util.Constants.Action.STRUCTURE_TASK_SYNCED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.INDEX_CASE_COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.SECONDARY_INDEX_CASE_COMPLETE;

import android.widget.Toast;
import androidx.annotation.NonNull;
import com.mapbox.geojson.Feature;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.HdssIndividual;
import org.smartregister.domain.Task;
import org.smartregister.domain.Task.TaskStatus;
import org.smartregister.reveal.contract.BaseContract;

import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.PreferencesUtil;
import timber.log.Timber;

public class GDRSPresenter implements BaseContract.BasePresenter {

  private final GDRSActivity gdrsActivity;
  private final GDRSInteractor gdrsInteractor;
  private final String thisCompoundId;
  private final String locationUUID;
  private final String taskIdentifier;
  @Getter
  private ActionAdapter actionAdapter;

  public GDRSPresenter(GDRSActivity gdrsActivity, String thisCompoundId, String locationUUID, String taskIdentifier) {
    this.gdrsActivity = gdrsActivity;
    this.gdrsInteractor = new GDRSInteractor(this,this.gdrsActivity, thisCompoundId,locationUUID);
    this.thisCompoundId = thisCompoundId;
    this.locationUUID = locationUUID;
    this.taskIdentifier = taskIdentifier;
  }

  public void saveJsonForm(String json) {
    gdrsInteractor.saveJsonForm(json);
  }

  @Override
  public void onFormSaved(
      @NonNull String structureId,
      String taskID,
      @NonNull Task.TaskStatus taskStatus,
      @NonNull String businessStatus,
      String interventionType) {

    Timber.tag("RevealMap").i("FormSaved 1 taskIdentifier %s",taskIdentifier);

    Task task = gdrsActivity.getTaskRepository().getTaskByIdentifier(taskIdentifier);
    Set<HdssTask> tasksByStructure =
        gdrsActivity.getHdssRepository().getTasksByStructure(
            locationUUID, PreferencesUtil.getInstance().getCurrentPlanId());

    if (!task.getStatus().equals(Task.TaskStatus.CANCELLED)) {

      if (RCD.equals(task.getCode())) {
        String businessStatusIndexCase = Constants.BusinessStatus.NOT_VISITED;
        boolean allRCDComplete = false;
        boolean allRCDInComplete = false;

        allRCDComplete =
            tasksByStructure.stream()
                .allMatch(innerTask -> COMPLETE.equals(innerTask.getBusinessStatus()));

        allRCDInComplete =
            tasksByStructure.stream()
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
        gdrsActivity.getTaskRepository().addOrUpdate(task);
      } else if (INDEX_CASE.equals(task.getCode())) {
        Timber.tag("RevealMap").i("Is index case");
        String businessStatusIndexCase;
        boolean allIndexCaseComplete = false;
        boolean allRCDComplete = false;

        allIndexCaseComplete =
            tasksByStructure.stream()
                .filter(innerTask -> INDEX_CASE_MEMBER.equals(innerTask.getCode()))
                .allMatch(innerTask -> COMPLETE.equals(innerTask.getBusinessStatus()));

        allRCDComplete =
            tasksByStructure.stream()
                .filter(innerTask -> RCD_MEMBER.equals(innerTask.getCode()))
                .allMatch(innerTask -> COMPLETE.equals(innerTask.getBusinessStatus()));

        businessStatusIndexCase = Constants.BusinessStatus.INDEX_CASE_NOT_VISITED;

        if (allIndexCaseComplete) {
          if (allRCDComplete) {
            businessStatusIndexCase = COMPLETE;
          } else {
            businessStatusIndexCase = INDEX_CASE_COMPLETE;
          }
        }
        Timber.tag("RevealMap").i("setBusinessStatus %s",businessStatusIndexCase);
        task.setBusinessStatus(businessStatusIndexCase);
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setLastModified(new DateTime());
        task.setSyncStatus(TYPE_Unsynced);
        gdrsActivity.getTaskRepository().addOrUpdate(task);
        Timber.tag("RevealMap").i("FormSaved 3");
      } else {
        String businessStatusIndexCase;
        boolean allIndexCaseComplete = false;
        boolean allRCDComplete = false;
        boolean allRCDInComplete = false;

        allIndexCaseComplete =
            tasksByStructure.stream()
                .filter(innerTask -> SECONDARY_INDEX_CASE_MEMBER.equals(innerTask.getCode()))
                .allMatch(innerTask -> COMPLETE.equals(innerTask.getBusinessStatus()));

        allRCDComplete =
            tasksByStructure.stream()
                .filter(innerTask -> RCD_MEMBER.equals(innerTask.getCode()))
                .allMatch(innerTask -> COMPLETE.equals(innerTask.getBusinessStatus()));

        businessStatusIndexCase = Constants.BusinessStatus.SECONDARY_INDEX_CASE_NOT_VISITED;

        if (allIndexCaseComplete) {
          if (allRCDComplete) {
            businessStatusIndexCase = COMPLETE;
          } else {
            businessStatusIndexCase = SECONDARY_INDEX_CASE_COMPLETE;
          }
        }

        task.setBusinessStatus(businessStatusIndexCase);
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setLastModified(new DateTime());
        gdrsActivity.getTaskRepository().addOrUpdate(task);
      }
    }
    gdrsActivity.populateActionList("Saving task");
  }

  @Override
  public void onStructureAdded(Feature feature, JSONArray featureCoordinates, double zoomlevel) {}

  @Override
  public void onFormSaveFailure(String eventType) {
    Toast.makeText(gdrsActivity, "Failure to save form data", Toast.LENGTH_LONG).show();
  }

  @Override
  public void onFamilyFound(CommonPersonObjectClient finalFamily) {}

  public void findLastEvent(String baseEntityId, String eventType) {
    gdrsInteractor.findLastEvent(baseEntityId, eventType);
  }

}
