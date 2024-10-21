package org.smartregister.reveal.presenter;

import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.customviews.NativeEditText;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;
import com.vijay.jsonwizard.widgets.CheckBoxFactory;
import com.vijay.jsonwizard.widgets.EditTextFactory;
import com.vijay.jsonwizard.widgets.GpsFactory;
import com.vijay.jsonwizard.widgets.ImagePickerFactory;
import com.vijay.jsonwizard.widgets.MultiSelectListFactory;
import com.vijay.jsonwizard.widgets.NativeEditTextFactory;
import com.vijay.jsonwizard.widgets.NativeRadioButtonFactory;
import com.vijay.jsonwizard.widgets.NumberSelectorFactory;
import com.vijay.jsonwizard.widgets.SpinnerFactory;

import io.ona.kujaku.listeners.BaseLocationListener;
import timber.log.Timber;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.reveal.R;
import org.smartregister.reveal.activity.RevealJsonFormActivity;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.PasswordRequestCallback;
import org.smartregister.reveal.contract.UserLocationContract.UserLocationCallback;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.Constants.CONFIGURATION;
import org.smartregister.reveal.util.Constants.JsonForm;
import org.smartregister.reveal.util.LocationUtils;
import org.smartregister.reveal.util.PasswordDialogUtils;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import org.smartregister.reveal.util.Utils;
import org.smartregister.reveal.view.RevealMapView;
import org.smartregister.reveal.widget.GeoWidgetFactory;
import org.smartregister.reveal.widget.RevealMultiSelectListFactory;
import org.smartregister.reveal.widget.RevealToasterNotesFactory;
import org.smartregister.util.JsonFormUtils;

/**
 * Created by samuelgithengi on 1/30/19.
 */
public class RevealJsonFormFragmentPresenter extends JsonFormFragmentPresenter implements PasswordRequestCallback, UserLocationCallback {

    private JsonFormFragment formFragment;

    private AlertDialog passwordDialog;

    private RevealJsonFormActivity jsonFormView;

    private RevealMapView mapView;

    private ValidateUserLocationPresenter locationPresenter;

    private LocationUtils locationUtils;

    private Location lastLocation;

    private BaseLocationListener locationListener;

    private RevealJsonFormUtils jsonFormUtils;

    private String mstepDup;

    private Stack<String> incorrectlyFormattedFields;

    private Map<String, ValidationStatus> invalidFields;

    public RevealJsonFormFragmentPresenter(JsonFormFragment formFragment, JsonFormInteractor jsonFormInteractor) {
        super(formFragment, jsonFormInteractor);
        this.formFragment = formFragment;
        passwordDialog = PasswordDialogUtils.initPasswordDialog(formFragment.getActivity(), this);
        jsonFormView = (RevealJsonFormActivity) formFragment.getActivity();
        locationPresenter = new ValidateUserLocationPresenter(jsonFormView, this);
        locationUtils = new LocationUtils(jsonFormView);
        locationListener = new BaseLocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;
            }
        };
        locationUtils.requestLocationUpdates(locationListener);
        jsonFormUtils = new RevealJsonFormUtils();


    }

    private static void setSpinnerError(MaterialSpinner spinner, String spinnerError) {
        try {
            spinner.setError(spinnerError);
        } catch (IllegalArgumentException e) {
            Timber.e(e);
        }
    }
    public static ValidationStatus validate(JsonFormFragmentView formFragmentView, View childAt,
                                            boolean requestFocus) {
        if (childAt instanceof RadioGroup) {
            RadioGroup radioGroup = (RadioGroup) childAt;
            ValidationStatus validationStatus = NativeRadioButtonFactory
                    .validate(formFragmentView, radioGroup);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        } else if (childAt instanceof NativeEditText) {
            NativeEditText editText = (NativeEditText) childAt;
            ValidationStatus validationStatus = NativeEditTextFactory
                    .validate(formFragmentView, editText);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        } else if (childAt instanceof MaterialEditText) {
            MaterialEditText editText = (MaterialEditText) childAt;
            ValidationStatus validationStatus = EditTextFactory.validate(formFragmentView, editText);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        } else if (childAt instanceof ImageView) {
            ValidationStatus validationStatus = ImagePickerFactory
                    .validate(formFragmentView, (ImageView) childAt);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        } else if (childAt instanceof Button) {
            String type = (String) childAt.getTag(R.id.type);
            if (!TextUtils.isEmpty(type) && type.equals(JsonFormConstants.GPS)) {
                ValidationStatus validationStatus = GpsFactory.validate(formFragmentView, (Button) childAt);
                if (!validationStatus.isValid()) {
                    if (requestFocus) {
                        validationStatus.requestAttention();
                    }
                    return validationStatus;
                }
            }
        } else if (childAt instanceof MaterialSpinner) {
            final MaterialSpinner spinner = (MaterialSpinner) childAt;
            final ValidationStatus validationStatus = SpinnerFactory.validate(formFragmentView, spinner);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                ((JsonFormActivity) formFragmentView.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSpinnerError(spinner, validationStatus.getErrorMessage());
                    }
                });
                return validationStatus;
            }
        } else if (childAt instanceof ViewGroup
                && childAt.getTag(R.id.is_checkbox_linear_layout) != null &&
                Boolean.TRUE.equals(childAt.getTag(R.id.is_checkbox_linear_layout))) {
            LinearLayout checkboxLinearLayout = (LinearLayout) childAt;
            ValidationStatus validationStatus = CheckBoxFactory
                    .validate(formFragmentView, checkboxLinearLayout);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }

        } else if (childAt instanceof ViewGroup
                && childAt.getTag(R.id.is_number_selector_linear_layout) != null &&
                Boolean.TRUE.equals(childAt.getTag(R.id.is_number_selector_linear_layout))) {
            ValidationStatus validationStatus = NumberSelectorFactory
                    .validate(formFragmentView, (ViewGroup) childAt);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        } else if (childAt instanceof RelativeLayout
                && childAt.getTag(R.id.is_multiselect_relative_layout) != null &&
                Boolean.TRUE.equals(childAt.getTag(R.id.is_multiselect_relative_layout))) {
            ValidationStatus validationStatus;
            if (Boolean.TRUE.equals(childAt.getTag(R.id.is_reveal_multiselect_relative_layout))){
                validationStatus = RevealMultiSelectListFactory
                        .validate(formFragmentView, (RelativeLayout) childAt);
            } else {
                validationStatus = MultiSelectListFactory
                        .validate(formFragmentView, (RelativeLayout) childAt);
            }

            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        }

        return new ValidationStatus(true, null, null, null);
    }

    private ValidationStatus validateView(View childAt) {
        return validate(getView(), childAt, true);
    }
    public String getStepTitle() {
        return mStepDetails.optString(JsonFormConstants.STEP_TITLE);
    }

    private void handleWrongFormatInputs(ValidationStatus validationStatus, String fieldKey,
                                         String rawValue) {
        if (!TextUtils.isEmpty(rawValue) && !validationStatus.isValid()) {
            if (!incorrectlyFormattedFields.contains(fieldKey)) {
                incorrectlyFormattedFields.push(fieldKey);
            }
        } else if (!TextUtils.isEmpty(rawValue) && validationStatus.isValid()) {
            incorrectlyFormattedFields.remove(fieldKey);
        } else if ((TextUtils.isEmpty(rawValue) && !validationStatus.isValid())) {
            incorrectlyFormattedFields.remove(fieldKey);
        }
    }
    public void validateAndWriteValuess() {
        mstepDup = getView().getArguments().getString("stepName");
        invalidFields = this.formFragment.getJsonApi().getInvalidFields();
        incorrectlyFormattedFields = new Stack<>();
        for (View childView : formFragment.getJsonApi().getFormDataViews()) {
            ValidationStatus validationStatus = validateView(childView);
            String key = (String) childView.getTag(R.id.key);
            String address = (String) childView.getTag(R.id.address);
            String openMrsEntityParent = (String) childView.getTag(R.id.openmrs_entity_parent);
            String openMrsEntity = (String) childView.getTag(R.id.openmrs_entity);
            String openMrsEntityId = (String) childView.getTag(R.id.openmrs_entity_id);
            Boolean popup = (Boolean) childView.getTag(R.id.extraPopup);
            String fieldKey = com.vijay.jsonwizard.utils.Utils.getFieldKeyPrefix(mstepDup, getStepTitle()) + key;

            if (StringUtils.isNotBlank(address) && mstepDup.equals(address.split(":")[0])) {

                if (childView instanceof MaterialEditText) {
                    MaterialEditText editText = (MaterialEditText) childView;
                    if (editText.getParent() != null && ((ViewGroup) editText.getParent()).isShown()) {

                        String rawValue = (String) editText.getTag(R.id.raw_value);
                        if (rawValue == null) {
                            rawValue = editText.getText().toString();
                        }

                        handleWrongFormatInputs(validationStatus, fieldKey, rawValue);

                        String type = (String) childView.getTag(R.id.type);
                        rawValue = JsonFormConstants.DATE_PICKER.equals(type) || JsonFormConstants.TIME_PICKER.equals(type) ? childView.getTag(R.id.locale_independent_value).toString() : rawValue;
                        getView().writeValue(mstepDup, key, rawValue, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);

                        //for repeating grp referenceEditText validation
//                        if (editText.getId() == R.id.reference_edit_text  && validationStatus.isValid()) {
//                            View doneButton = ((ViewGroup) editText.getParent()).findViewById(R.id.btn_repeating_group_done);
//                            Object o = doneButton.getTag(R.id.is_repeating_group_generated);
////                            if (o == null) {
////                                validationStatus.setIsValid(false);
////                                editText.setError(getFormFragment().getString(R.string.repeating_group_not_generated_error_message));
////                                validationStatus.setErrorMessage(getFormFragment().getString(R.string.repeating_group_not_generated_error_message));
////                            }
//                        }
                    } else {
                        validationStatus.setIsValid(true);
                    }
                } else if (childView instanceof NativeEditText) {
                    NativeEditText editText = (NativeEditText) childView;

                    String rawValue = (String) editText.getTag(R.id.raw_value);
                    if (rawValue == null) {
                        rawValue = editText.getText().toString();
                    }

                    handleWrongFormatInputs(validationStatus, fieldKey, rawValue);

                    getView().writeValue(mstepDup, key, rawValue, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
                } else if (childView instanceof ImageView) {
                    Object path = childView.getTag(R.id.imagePath);
                    if (path instanceof String) {
                        getView().writeValue(mstepDup, key, (String) path, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
                    }
                } else if (childView instanceof CheckBox) {
                    String parentKey = (String) childView.getTag(R.id.key);
                    String childKey = (String) childView.getTag(R.id.childKey);
                    getView().writeValue(mstepDup, parentKey, JsonFormConstants.OPTIONS_FIELD_NAME, childKey, String.valueOf(((CheckBox) childView).isChecked()), openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
                } else if (childView instanceof RadioButton) {
                    String parentKey = (String) childView.getTag(R.id.key);
                    String childKey = (String) childView.getTag(R.id.childKey);
                    if (((RadioButton) childView).isChecked()) {
                        getView().writeValue(mstepDup, parentKey, childKey, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
                    }
                } else if (childView instanceof Button) {
                    Button button = (Button) childView;
                    String rawValue = (String) button.getTag(R.id.raw_value);
                    getView().writeValue(mstepDup, key, rawValue, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
                }

                if (!validationStatus.isValid()) {
                    invalidFields.put(fieldKey, validationStatus);
                } else {
                    if (invalidFields.size() > 0) {
                        invalidFields.remove(fieldKey);
                    }
                }
            }
        }

        //remove invalid fields not belonging to current step since formdata view are cleared when view is created
        if (invalidFields != null && !invalidFields.isEmpty()) {
            for (Map.Entry<String, ValidationStatus> entry : invalidFields.entrySet()) {
                String key = entry.getKey();
                if (StringUtils.isNotBlank(key) && !key.startsWith(mstepDup)) {
                    invalidFields.remove(key);
                }
            }
        }
        formFragment.getOnFieldsInvalidCallback().passInvalidFields(invalidFields);
    }

    @Override
    public void validateAndWriteValues() {
        validateAndWriteValuess();
//        super.validateAndWriteValues();
        Boolean multiSelectFieldInvalid = false;
        String multiSelectFieldKey = null;
        for (View childAt : formFragment.getJsonApi().getFormDataViews()) {
            if (childAt instanceof RevealMapView) {
                RevealMapView mapView = (RevealMapView) childAt;
                ValidationStatus validationStatus = GeoWidgetFactory.validate(formFragment, mapView, this);
                String key = (String) childAt.getTag(com.vijay.jsonwizard.R.id.key);
                String mStepName = this.getView().getArguments().getString("stepName");
                String fieldKey = mStepName + " (" + mStepDetails.optString("title") + ") :" + key;
                if (!validationStatus.isValid()) {
                    getInvalidFields().put(fieldKey, validationStatus);
                } else {
                    getInvalidFields().remove(fieldKey);
                    if (isFormValid() && validateFarStructures()) {
                        validateUserLocation(mapView);
                        return;
                    }
                }
                this.mapView = mapView;
                break;//exit loop, assumption; there will be only 1 map per form.
            } else if (childAt instanceof TextView && !(childAt instanceof MaterialEditText)) {
                ValidationStatus validationStatus = RevealToasterNotesFactory.validate(formFragment, (TextView) childAt);
                String address = (String) childAt.getTag(com.vijay.jsonwizard.R.id.address);
                if (!validationStatus.isValid()) {
                    getInvalidFields().put(address, validationStatus);
                } else {
                    getInvalidFields().remove(address);
                }
            } else if(childAt instanceof RelativeLayout){
                ValidationStatus validationStatus = RevealMultiSelectListFactory.validate(formFragment,(RelativeLayout)childAt);
                String address = (String) childAt.getTag(com.vijay.jsonwizard.R.id.address);
                multiSelectFieldKey = (String) childAt.getTag(com.vijay.jsonwizard.R.id.key);
                if (!validationStatus.isValid()) {
                    getInvalidFields().put(address, validationStatus);
                    multiSelectFieldInvalid = true;
                } else {
                    multiSelectFieldInvalid = false;
                    getInvalidFields().remove(address);
                }
            }
        }
        Map<String, ValidationStatus> invalidFields = this.getInvalidFields();
        if (isFormValid()) {// if form is valid and did not have a map, if it had a map view it will be handled above
            onLocationValidated();

        } else {//if form is invalid whether having a map or not
            if (showErrorsOnSubmit()) {
                launchErrorDialog();
                getView().showToast(getView().getContext().getResources().getString(R.string.json_form_error_msg, this.getInvalidFields().size()));
            } else {
                if(multiSelectFieldInvalid){
                    getView().showSnackBar(String.format("%s is Required",multiSelectFieldKey));
                }else {
                    getView().showSnackBar(getView().getContext().getResources().getString(R.string.json_form_error_msg, this.getInvalidFields().size()));
                }

            }
        }
    }

    @VisibleForTesting
    protected boolean validateFarStructures() {
        return Utils.validateFarStructures();
    }


    private void validateUserLocation(RevealMapView mapView) {
        this.mapView = mapView;
        Location location = jsonFormView.getUserCurrentLocation();
        if (location != null) {
            locationPresenter.onGetUserLocation(location);
        } else {
            locationPresenter.requestUserLocation();
        }
    }

    @Override
    public void onSaveClick(LinearLayout mainView) {
        validateAndWriteValues();
    }

    @Override
    public void onLocationValidated() {
        jsonFormView.hideProgressDialog();
        Intent returnIntent = new Intent();
        getView().onFormFinish();
        returnIntent.putExtra("json", getView().getCurrentJsonState());
        Object skipValidation = formFragment.getMainView().getTag(com.vijay.jsonwizard.R.id.skip_validation);
        returnIntent.putExtra(JsonFormConstants.SKIP_VALIDATION, Boolean.valueOf(skipValidation == null ? Boolean.FALSE.toString() : skipValidation.toString()));
        getView().finishWithResult(returnIntent);

    }

    @Override
    public LatLng getTargetCoordinates() {
        return mapView.getMapboxMap().getCameraPosition().target;
    }

    @Override
    public void requestUserPassword() {
        if (passwordDialog != null) {
            passwordDialog.show();
        }
    }

    @Override
    public void onPasswordVerified() {
        onLocationValidated();
    }

    @Override
    public ValidateUserLocationPresenter getLocationPresenter() {
        return locationPresenter;
    }

    public LocationUtils getLocationUtils() {
        return locationUtils;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public BaseLocationListener getLocationListener() {
        return locationListener;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        super.onItemSelected(parent, view, position, id);
        String key = (String) parent.getTag(R.id.key);
        Map<String, JSONObject> fields = jsonFormUtils.getFields(jsonFormView.getmJSONObject());
        cascadeSelect(key, JsonForm.DISTRICT, Constants.CONFIGURATION.HEALTH_FACILITIES, fields.get(JsonForm.HFC_BELONG));
        cascadeSelect(key, JsonForm.DISTRICT, Constants.CONFIGURATION.HEALTH_FACILITIES, fields.get(JsonForm.HFC_SEEK));
        cascadeSelect(key, JsonForm.DATA_COLLECTOR, Constants.CONFIGURATION.SPRAY_OPERATORS, fields.get(JsonForm.SPRAY_OPERATOR_CODE));
        cascadeSelect(key, JsonForm.HFC_BELONG, Constants.CONFIGURATION.COMMUNITY_HEALTH_WORKERS, fields.get(JsonForm.CHW_NAME));
        cascadeSelect(key, JsonForm.CATCHMENT_AREA, Constants.CONFIGURATION.MDA_CORDINATORS, fields.get(JsonForm.COORDINATOR_NAME));
        cascadeSelect(key, JsonForm.CATCHMENT_AREA, Constants.CONFIGURATION.MDA_ENUMERATORS, fields.get(JsonForm.DATA_COLLECTOR));
        cascadeSelect(key, JsonForm.CATCHMENT_AREA, Constants.CONFIGURATION.MDA_COMMUNITY_HEALTH_WORKERS, fields.get(JsonForm.CHW_NAME));
        cascadeSelect(key, JsonForm.CATCHMENT_AREA, Constants.CONFIGURATION.MDA_ADHERENCE_OFFICERS, fields.get(JsonForm.ADHERENCE_NAME));
        cascadeSelect(key,JsonForm.LOCATION,Constants.CONFIGURATION.HEALTH_WORKER_SUPERVISORS,fields.get(JsonForm.HEALTH_WORKER_SUPERVISOR));
        cascadeSelect(key,JsonForm.HEALTH_WORKER_SUPERVISOR,Constants.CONFIGURATION.COMMUNITY_DRUG_DISTRIBUTORS,fields.get(JsonForm.COMMUNITY_DRUG_DISTRIBUTOR_NAME));
        cascadeSelect(key,JsonForm.SUPERVISOR, CONFIGURATION.SPRAY_OPERATORS,fields.get(JsonForm.SPRAY_OPERATOR_CODE));
        cascadeSelect(key,JsonForm.SUPERVISOR, CONFIGURATION.SPRAY_OPERATORS,fields.get(JsonForm.SPRAY_OPERATOR_CODE_CONFIRMATION));
        cascadeSelect(key,JsonForm.SUPERVISOR, CONFIGURATION.SPRAY_OPERATORS,fields.get(JsonForm.SPRAYOP_NAME));
    }

    private void cascadeSelect(String key, String parentWidget, String configurationKey, JSONObject childWidget) {
        if (parentWidget.equals(key)) {
            String value = JsonFormUtils.getFieldValue(getView().getCurrentJsonState(), key);
            if (!TextUtils.isEmpty(value)) {
                Pair<JSONArray, JSONArray> options = jsonFormUtils.populateServerOptions(RevealApplication.getInstance().getServerConfigs()
                        , configurationKey, childWidget, value.split(":")[0]);
                if (options != null) {
                    List<String> newAdapterValues = new Gson().fromJson(options.second.toString(), new TypeToken<List<String>>() {
                    }.getType());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getView().getContext(), R.layout.native_form_simple_list_item_1, newAdapterValues);
                    MaterialSpinner spinner = (MaterialSpinner) jsonFormView.getFormDataView(JsonFormConstants.STEP1 + ":" + childWidget.optString(JsonFormUtils.KEY));
                    if (spinner != null) {
                        Object selected;
                        String childPreviousValue = JsonFormUtils.getFieldValue(getView().getCurrentJsonState(),childWidget.optString(JsonFormUtils.KEY));
                        if (spinner.getAdapter().getCount() == spinner.getSelectedItemPosition()) {
                            selected = spinner.getAdapter().getItem(spinner.getSelectedItemPosition() - 1);
                        } else {
                            selected = spinner.getSelectedItem();
                        }
                        spinner.setAdapter(adapter);
                        spinner.setOnItemSelectedListener(formFragment.getCommonListener());
                        spinner.setTag(R.id.keys, options.first);
                        if (selected != null && newAdapterValues.contains(selected.toString())) {
                            spinner.setSelection(newAdapterValues.indexOf(selected.toString()));
                        }
                        if(childPreviousValue  != null && !childPreviousValue.isEmpty() && selected == null){
                            spinner.setSelection(newAdapterValues.indexOf(childPreviousValue) + 1);
                        }
                    }
                }
            }
        }
    }

    public void onGetUserLocation(Location location) {
        //empty
    }

}
