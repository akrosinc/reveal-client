package org.smartregister.reveal.presenter;

import static org.smartregister.reveal.contract.StructureTasksContract.Interactor;
import static org.smartregister.reveal.contract.StructureTasksContract.Presenter;
import static org.smartregister.reveal.util.Constants.Intervention.BEDNET_DISTRIBUTION;
import static org.smartregister.reveal.util.Constants.Intervention.BLOOD_SCREENING;
import static org.smartregister.reveal.util.Constants.Intervention.MDA_ADHERENCE;
import static org.smartregister.reveal.util.Constants.Intervention.MDA_DISPENSE;
import static org.smartregister.reveal.util.Constants.Intervention.MDA_DRUG_RECON;
import static org.smartregister.util.JsonFormUtils.VALUE;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.mapbox.geojson.Feature;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Event;
import org.smartregister.domain.Task;
import org.smartregister.domain.Task.TaskStatus;
import org.smartregister.reveal.R;
import org.smartregister.reveal.contract.BaseFormFragmentContract;
import org.smartregister.reveal.contract.StructureTasksContract;
import org.smartregister.reveal.interactor.BaseFormFragmentInteractor;
import org.smartregister.reveal.interactor.StructureTasksInteractor;
import org.smartregister.reveal.model.FamilySummaryModel;
import org.smartregister.reveal.model.StructureTaskDetails;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.Utils;
import timber.log.Timber;

/**
 * Created by samuelgithengi on 4/12/19.
 */
public class StructureTasksPresenter extends BaseFormFragmentPresenter implements Presenter {

    private WeakReference<StructureTasksContract.View> view;

    private Interactor interactor;

    private PreferencesUtil prefsUtil;

    private StructureTaskDetails indexCase;

    private String structureId;

    private BaseFormFragmentContract.Interactor formInteractor;


    public StructureTasksPresenter(StructureTasksContract.View view, Context context) {
        this(view, context, null, PreferencesUtil.getInstance());
        interactor = new StructureTasksInteractor(this);
        formInteractor = new BaseFormFragmentInteractor(this);
    }

    @VisibleForTesting
    public StructureTasksPresenter(StructureTasksContract.View view, Context context, Interactor interactor, PreferencesUtil prefsUtil) {
        super(view, context);
        this.view = new WeakReference<>(view);
        this.interactor = interactor;
        this.prefsUtil = prefsUtil;
    }

    @Override
    public void findTasks(String structureId) {
        this.structureId = structureId;
        interactor.findTasks(structureId, prefsUtil.getCurrentPlanId(),
                Utils.getOperationalAreaLocation(prefsUtil.getCurrentOperationalArea()).getId());
    }

    @Override
    public void refreshTasks() {
        if (structureId != null)
            findTasks(structureId);
    }


    @Override
    public void onTasksFound(List<StructureTaskDetails> taskDetailsList, StructureTaskDetails incompleteIndexCase) {
        indexCase = incompleteIndexCase;
        getView().setTaskDetailsList(taskDetailsList);
        if (incompleteIndexCase != null) {
            getView().displayDetectCaseButton();
        }
    }

    @Override
    public void onTaskSelected(StructureTaskDetails details, boolean isEdit, boolean isUndo) {
        if (details != null) {
            if (TaskStatus.COMPLETED.name().equals(details.getTaskStatus())) {
                if (isEdit) {
                    details.setEdit(true);
                    getView().showProgressDialog(R.string.opening_form_title, R.string.opening_form_message);
                    interactor.getStructure(details);
                } else if(isUndo) {
                    getView().displayResetTaskInfoDialog(details);
                } else if (MDA_DISPENSE.equals(details.getTaskCode()) || MDA_ADHERENCE.equals(details.getTaskCode()) || MDA_DRUG_RECON.equals(details.getTaskCode())) {
                    // HEADS UP
                    getView().showProgressDialog(R.string.opening_form_title, R.string.opening_form_message);
                    interactor.getStructure(details);
                } else {
                    getView().displayToast("Task Completed");
                }
            } else {
                getView().showProgressDialog(R.string.opening_form_title, R.string.opening_form_message);
                interactor.getStructure(details);
            }
        }
    }


    @Override
    public void onLocationValidated() {
        StructureTaskDetails taskDetails = (StructureTaskDetails) getTaskDetails();
        if (taskDetails.isEdit() && (Constants.Intervention.BEDNET_DISTRIBUTION.equals(taskDetails.getTaskCode()) || BLOOD_SCREENING.equals(taskDetails.getTaskCode()))) {
            interactor.findLastEvent(taskDetails);
        } else if (MDA_DISPENSE.equals(taskDetails.getTaskCode()) || MDA_ADHERENCE.equals(taskDetails.getTaskCode()) || MDA_DRUG_RECON.equals(taskDetails.getTaskCode())) {
            // HEADS UP
            if (taskDetails.isEdit() || TaskStatus.COMPLETED.name().equals(taskDetails.getTaskStatus())) {
                interactor.findLastEvent(taskDetails);
            } else if (MDA_DRUG_RECON.equals(taskDetails.getTaskCode())) {
                interactor.findCompletedDispenseTasks(taskDetails, getView().getTaskDetailsList());
            } else {
                super.onLocationValidated();
            }
        } else {
            super.onLocationValidated();
        }
    }

    public StructureTasksContract.View getView() {
        return view.get();
    }

    @Override
    public void saveJsonForm(String json) {
        getView().showProgressDialog(R.string.saving_title, R.string.saving_message);
        interactor.saveJsonForm(json);
    }

    @Override
    public void onDetectCase() {
        indexCase.setStructureId(structureId);
        interactor.getStructure(indexCase);
    }

    @Override
    public void onIndexConfirmationFormSaved(String taskID, TaskStatus taskStatus, String businessStatus, Set<Task> removedTasks) {
        getView().updateTasks(taskID, taskStatus, businessStatus, removedTasks);
        if (taskStatus.equals(TaskStatus.COMPLETED)) {
            getView().hideDetectCaseButton();
        }
        if (!removedTasks.isEmpty()) {
            getView().updateNumberOfTasks();
        }
        getView().hideProgressDialog();
    }

    @Override
    public void onEventFound(Event event) {
        startTaskForm(event, null);
    }

    public void startTaskForm(Event event, FamilySummaryModel summary) {

        // Heads up
        String formName = getView().getJsonFormUtils().getFormName(null, getTaskDetails().getTaskCode());
        if (StringUtils.isBlank(formName)) {
            getView().displayError(R.string.opening_form_title, R.string.form_not_found);
        } else {
            StructureTaskDetails taskDetails = (StructureTaskDetails) getTaskDetails();
            boolean readOnly = !taskDetails.isEdit() && taskDetails.getTaskStatus().equals(TaskStatus.COMPLETED.name());

            JSONObject formJSON = getView().getJsonFormUtils().getFormJSON(getView().getContext(), formName, getTaskDetails(), getStructure());

            if (event != null) {
                getView().getJsonFormUtils().populateForm(event, formJSON);
            }

            if(summary != null) {
                getView().getJsonFormUtils().populateForm(summary, formJSON);
            }

            if (summary == null && BEDNET_DISTRIBUTION.equals(getTaskDetails().getTaskCode())) {
                formInteractor.findNumberOfMembers(getTaskDetails().getTaskEntity(), formJSON);
            } else if (getBuildCountry() == Country.NIGERIA && MDA_DRUG_RECON.equals(getTaskDetails().getTaskCode())) {
                interactor.findTotalSMCDosageCounts(taskDetails, formJSON);
            } else {
                getView().startForm(formJSON);
            }
        }
        getView().hideProgressDialog();
    }

    private Country getBuildCountry() {
        return PreferencesUtil.getInstance().getBuildCountry();
    }

    @Override
    public void onFetchedMembersCount(FamilySummaryModel summary) {
        startTaskForm(null, summary);
    }

    @Override
    public void resetTaskInfo(StructureTaskDetails taskDetails) {
        interactor.resetTaskInfo(getView().getContext(), taskDetails);
    }

    @Override
    public void onTaskInfoReset(String structureId) {
        findTasks(structureId);
    }

    @Override
    public void onFormSaved(@NonNull String structureId, String taskID, @NonNull TaskStatus taskStatus, @NonNull String businessStatus, String interventionType) {
        getView().hideProgressDialog();
        getView().updateTask(taskID, taskStatus, businessStatus);
    }

    @Override
    public void onStructureAdded(Feature feature, JSONArray featureCoordinates, double zoomlevel) {//not used
    }

    @Override
    public void onFormSaveFailure(String eventType) {
        getView().hideProgressDialog();//register will refresh on resume
    }

    @Override
    public void onFamilyFound(CommonPersonObjectClient finalFamily) {//not used
    }

    @Override
    public void onTotalSMCDosageCountsFound(StructureTaskDetails taskDetails, JSONObject formJSON) {

        try {
            getView().getJsonFormUtils().populateField(formJSON, Constants.JsonForm.TOTAL_ADMINISTERED_SPAQ, taskDetails.getTotalAdministeredSpaq() + "", VALUE);
            getView().getJsonFormUtils().populateField(formJSON, Constants.JsonForm.TOTAL_NUMBER_OF_ADDITIONAL_DOSES, taskDetails.getTotalNumberOfAdditionalDoses() + "", VALUE);
        } catch (JSONException e) {
            Timber.e(e);
        }

        boolean readOnly = !taskDetails.isEdit() && taskDetails.getTaskStatus().equals(TaskStatus.COMPLETED.name());
        getView().startForm(formJSON);
    }

}
