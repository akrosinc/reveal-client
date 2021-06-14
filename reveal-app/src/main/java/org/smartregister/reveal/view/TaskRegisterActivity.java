package com.revealprecision.reveal.view;

import android.content.Intent;
import android.view.View;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;
import com.revealprecision.reveal.R;
import com.revealprecision.reveal.contract.CaseClassificationContract;
import com.revealprecision.reveal.contract.TaskRegisterContract;
import com.revealprecision.reveal.fragment.CaseClassificationFragment;
import com.revealprecision.reveal.fragment.TaskRegisterFragment;
import com.revealprecision.reveal.model.BaseTaskDetails;
import com.revealprecision.reveal.presenter.TaskRegisterPresenter;
import com.revealprecision.reveal.util.Constants.Properties;
import com.revealprecision.reveal.util.RevealJsonFormUtils;
import org.smartregister.view.contract.BaseRegisterContract;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Collections;
import java.util.List;

import timber.log.Timber;

import static com.revealprecision.reveal.util.Constants.JSON_FORM_PARAM_JSON;
import static com.revealprecision.reveal.util.Constants.RequestCode.REQUEST_CODE_GET_JSON;
import static com.revealprecision.reveal.util.Constants.TaskRegister;
import static com.revealprecision.reveal.util.FamilyConstants.Intent.START_REGISTRATION;

/**
 * Created by samuelgithengi on 3/11/19.
 */
public class TaskRegisterActivity extends BaseRevealRegisterActivity implements BaseRegisterContract.View {

    private RevealJsonFormUtils jsonFormUtils;

    private CaseClassificationFragment caseClassificationFragment = new CaseClassificationFragment();

    @Override
    protected void initializePresenter() {
        presenter = new TaskRegisterPresenter(this);
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        jsonFormUtils = new RevealJsonFormUtils();
        TaskRegisterFragment fragment = new TaskRegisterFragment();
        fragment.setJsonFormUtils(jsonFormUtils);
        return fragment;
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[]{caseClassificationFragment};
    }

    @Override
    public void startFormActivity(JSONObject jsonObject) {
        jsonFormUtils.startJsonForm(jsonObject, this);
    }

    @Override
    protected void onActivityResultExtended(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GET_JSON && resultCode == RESULT_OK && data.hasExtra(JSON_FORM_PARAM_JSON)) {
            String json = data.getStringExtra(JSON_FORM_PARAM_JSON);
            Timber.d(json);
            getPresenter().saveJsonForm(json);
        } else {
            mBaseFragment.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public List<String> getViewIdentifiers() {
        return Collections.singletonList(TaskRegister.VIEW_IDENTIFIER);
    }

    @Override
    protected void registerBottomNavigation() {
        //not used for task register
        findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
    }

    @VisibleForTesting
    public TaskRegisterContract.Presenter getPresenter() {
        return (TaskRegisterContract.Presenter) presenter;
    }

    public void startFamilyRegistration(BaseTaskDetails taskDetails) {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        intent.putExtra(START_REGISTRATION, true);
        intent.putExtra(Properties.LOCATION_UUID, taskDetails.getStructureId());
        intent.putExtra(Properties.TASK_IDENTIFIER, taskDetails.getTaskId());
        intent.putExtra(Properties.TASK_BUSINESS_STATUS, taskDetails.getBusinessStatus());
        intent.putExtra(Properties.TASK_STATUS, taskDetails.getTaskStatus());
        startActivity(intent);
    }

    public void displayIndexCaseFragment(JSONObject indexCase) {
        ((CaseClassificationContract.View) caseClassificationFragment).displayIndexCase(indexCase);
        switchToFragment(1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        switchToBaseFragment();
        return true;
    }
}
