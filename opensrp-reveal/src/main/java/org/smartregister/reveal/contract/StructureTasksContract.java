package org.smartregister.reveal.contract;

import android.content.Context;

import org.json.JSONObject;
import org.smartregister.domain.Event;
import org.smartregister.domain.Task;
import org.smartregister.reveal.model.FamilySummaryModel;
import org.smartregister.reveal.model.StructureTaskDetails;

import java.util.List;
import java.util.Set;


public interface StructureTasksContract {

    interface Presenter extends BaseContract.BasePresenter, BaseFormFragmentContract.Presenter {

        void findTasks(String structureId);

        void refreshTasks();

        void onTasksFound(List<StructureTaskDetails> taskDetailsList, StructureTaskDetails incompleteIndexCase);

        void onTaskSelected(StructureTaskDetails details, boolean isEdit, boolean isUndo);

        void saveJsonForm(String json);

        void onDetectCase();

        void onIndexConfirmationFormSaved(String taskID, Task.TaskStatus taskStatus, String businessStatus, Set<Task> removedTasks);

        void onEventFound(Event event);

        void resetTaskInfo(StructureTaskDetails taskDetails);

        void onTaskInfoReset(String structureId);

        void onFetchedMembersCount(FamilySummaryModel summary);

        void onTotalSMCDosageCountsFound(StructureTaskDetails taskDetails, JSONObject formJSON);
    }

    interface Interactor extends BaseContract.BaseInteractor {

        void findTasks(String structureId, String currentPlanId, String operationalAreaId);

        void getStructure(StructureTaskDetails details);

        void findLastEvent(StructureTaskDetails taskDetails);

        void resetTaskInfo(Context context, StructureTaskDetails taskDetails);

        void findCompletedDispenseTasks(StructureTaskDetails taskDetails, List<StructureTaskDetails> taskDetailsList);

        void findTotalSMCDosageCounts(StructureTaskDetails taskDetails, JSONObject formJSON);

    }

    interface View extends UserLocationContract.UserLocationView, BaseFormFragmentContract.View {

        void setStructure(String structureId);

        void showProgressDialog(int title, int message);

        void hideProgressDialog();

        android.location.Location getUserCurrentLocation();

        Context getContext();

        void setTaskDetailsList(List<StructureTaskDetails> taskDetailsList);

        List<StructureTaskDetails> getTaskDetailsList();

        void updateTask(String taskID, Task.TaskStatus taskStatus, String businessStatus);

        void displayDetectCaseButton();

        void hideDetectCaseButton();

        void updateNumberOfTasks();

        void updateTasks(String taskID, Task.TaskStatus taskStatus, String businessStatus, Set<Task> removedTasks);

        void displayResetTaskInfoDialog(StructureTaskDetails taskDetails);
    }
}
