package org.smartregister.reveal.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;


import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.vijay.jsonwizard.activities.FormConfigurationJsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import org.json.JSONException;
import org.smartregister.reveal.R;
import org.smartregister.reveal.contract.UserLocationContract.UserLocationView;
import org.smartregister.reveal.fragment.RevealJsonFormFragment;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.rule.RevealRuleEngineFactory;
import org.smartregister.util.Constants;


public class RevealJsonFormActivity extends FormConfigurationJsonFormActivity implements UserLocationView {

    private RevealJsonFormFragment formFragment;

    private boolean requestedLocation;

    private ProgressDialog progressDialog;
    private PreferencesUtil instance ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        rulesEngineFactory = new RevealRuleEngineFactory(this, globalValues);
        instance = PreferencesUtil.getInstance();
    }


    @Override
    public void initializeFormFragment() {

        RevealJsonFormFragment revealJsonFormFragment = RevealJsonFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, revealJsonFormFragment).commit();
    }

    public JsonFormFragment getFragment(){
        return  this.formFragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.RequestCode.LOCATION_SETTINGS && requestedLocation) {
            if (resultCode == RESULT_OK) {
                formFragment.getPresenter().getLocationUtils().requestLocationUpdates(formFragment.getPresenter().getLocationCallback());
//                formFragment.getPresenter().getLocationUtils().requestLocationUpdates(formFragment.getPresenter().getLocationListener());
                formFragment.getPresenter().getLocationPresenter().waitForUserLocation();
            } else if (resultCode == RESULT_CANCELED) {
                formFragment.getPresenter().getLocationPresenter().onGetUserLocationFailed();
            }
            requestedLocation = false;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof RevealJsonFormFragment) {
            formFragment = (RevealJsonFormFragment) fragment;
        }
    }

    @Override
    public Location getUserCurrentLocation() {
        return formFragment.getPresenter().getLastLocation();
    }

    @Override
    public void showProgressDialog(@StringRes int title, @StringRes int message) {
        if (progressDialog != null) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(getString(message));
            progressDialog.show();
        }
    }

    @Override
    public void updateProgressDialog(int percentage) {

    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void requestUserLocation() {
//        formFragment.getPresenter().getLocationUtils().checkLocationSettingsAndStartLocationServices(this, formFragment.getPresenter().getLocationListener());
        formFragment.getPresenter().getLocationUtils().checkLocationSettingsAndStartLocationServices(this, formFragment.getPresenter().getLocationCallback());
        requestedLocation = true;
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        formFragment.getPresenter().getLocationUtils().stopLocationClient();
//    }
@Override
protected void onStop() {
    super.onStop();
    if (formFragment != null && formFragment.getPresenter() != null) {
        formFragment.getPresenter().getLocationUtils()
                .stopLocationClient(formFragment.getPresenter().getLocationCallback());
    }
}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (formFragment != null && formFragment.getPresenter() != null) {
            formFragment.getPresenter().getLocationUtils()
                    .stopLocationClient(formFragment.getPresenter().getLocationCallback());
        }
    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        formFragment.getPresenter().getLocationUtils().destroy();
//    }

    @Override
    protected void widgetsWriteValue(String stepName, String key, String value, String openMrsEntityParent,
                                     String openMrsEntity, String openMrsEntityId, boolean popup) throws JSONException {
        super.widgetsWriteValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
    }


    @Override
    public void writeValue(String stepName, String key, String value, String openMrsEntityParent, String openMrsEntity,
                           String openMrsEntityId, boolean popup) throws JSONException {

        super.writeValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
    }
}
