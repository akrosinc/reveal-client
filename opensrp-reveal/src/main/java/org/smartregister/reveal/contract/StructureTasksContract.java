package org.smartregister.reveal.contract;

import android.content.Context;

import org.smartregister.domain.Task;
import org.smartregister.reveal.model.StructureTaskDetails;

import java.util.List;

/**
 * Created by samuelgithengi on 4/12/19.
 */
public interface StructureTasksContract {

    interface Presenter extends BaseContract.BasePresenter, BaseFormFragmentContract.Presenter {

        void findTasks(String structureId);

        void onTasksFound(List<StructureTaskDetails> taskDetailsList, boolean incompeleteIndexCase);

        void onTaskSelected(StructureTaskDetails details);

        void saveJsonForm(String json);
    }

    interface Interactor extends BaseContract.BaseInteractor {

        void findTasks(String structureId, String currentPlanId, String operationalAreaId);

        void getStructure(StructureTaskDetails details);
    }

    interface View extends UserLocationContract.UserLocationView, BaseFormFragmentContract.View {

        void setStructure(String structureId);

        void showProgressDialog(int title, int message);

        void hideProgressDialog();

        android.location.Location getUserCurrentLocation();

        Context getContext();

        void setTaskDetailsList(List<StructureTaskDetails> taskDetailsList);

        void updateTask(String taskID, Task.TaskStatus taskStatus, String businessStatus);

        void displayDetectCaseButton();
    }
}
