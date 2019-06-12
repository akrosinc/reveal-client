package org.smartregister.reveal.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.domain.FamilyMetadata;
import org.smartregister.family.util.Constants.JSON_FORM_KEY;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.util.FamilyConstants.DatabaseKeys;
import org.smartregister.reveal.util.FamilyConstants.FormKeys;
import org.smartregister.util.FormUtils;
import org.smartregister.view.LocationPickerView;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import timber.log.Timber;

/**
 * Created by samuelgithengi on 5/24/19.
 */
public class FamilyJsonFormUtils extends JsonFormUtils {

    private static final String TAG = FamilyJsonFormUtils.class.getName();

    private LocationPickerView locationPickerView;

    private FormUtils formUtils;

    private LocationHelper locationHelper;

    private Context context;

    private HashMap<String, String> jsonDbMap;

    public FamilyJsonFormUtils(LocationPickerView locationPickerView, FormUtils formUtils,
                               LocationHelper locationHelper, Context context) {
        this.locationPickerView = locationPickerView;
        this.formUtils = formUtils;
        this.locationHelper = locationHelper;
        this.context = context;
        locationPickerView.init();
        initMap();
    }

    public FamilyJsonFormUtils(Context context) throws Exception {
        this(new LocationPickerView(context), FormUtils.getInstance(context), LocationHelper.getInstance(), context);
    }

    private void initMap() {
        jsonDbMap = new HashMap<>();
        jsonDbMap.put(FormKeys.SEX, DBConstants.KEY.GENDER);
        jsonDbMap.put(DatabaseKeys.NATIONAL_ID, DatabaseKeys.NATIONAL_ID);
        jsonDbMap.put(DatabaseKeys.CITIZENSHIP, DatabaseKeys.CITIZENSHIP);
        jsonDbMap.put(DatabaseKeys.OCCUPATION, DatabaseKeys.OCCUPATION);
        jsonDbMap.put(DatabaseKeys.SLEEPS_OUTDOORS, DatabaseKeys.SLEEPS_OUTDOORS);
        jsonDbMap.put(DatabaseKeys.PHONE_NUMBER, DatabaseKeys.PHONE_NUMBER);

    }

    public JSONObject getAutoPopulatedJsonEditFormString(String formName, Context context, CommonPersonObjectClient client, String eventType) {
        try {
            JSONObject form = formUtils.getFormJson(formName);
            if (form != null) {
                form.put(ENTITY_ID, client.getCaseId());
                form.put(ENCOUNTER_TYPE, eventType);

                JSONObject metadata = form.getJSONObject(METADATA);
                String lastLocationId = locationHelper.getOpenMrsLocationId(locationPickerView.getSelectedItem());

                metadata.put(ENCOUNTER_LOCATION, lastLocationId);

                form.put(CURRENT_OPENSRP_ID, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false));

                //inject opensrp id into the form
                JSONObject stepOne = form.getJSONObject(STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    processPopulatableFields(client, jsonObject);

                }

                return form;
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    protected static void processPopulatableFields(CommonPersonObjectClient client, JSONObject jsonObject) throws JSONException {
        switch (jsonObject.getString(KEY)) {
            case DatabaseKeys.FAMILY_NAME:
            case DatabaseKeys.OLD_FAMILY_NAME:
                jsonObject.put(VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FIRST_NAME, false));
                break;
            case DBConstants.KEY.VILLAGE_TOWN:
                jsonObject.put(VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, false));
                break;
            case DatabaseKeys.HOUSE_NUMBER:
                jsonObject.put(VALUE, Utils.getValue(client.getColumnmaps(), DatabaseKeys.HOUSE_NUMBER, false));
                break;
            case DBConstants.KEY.STREET:
                jsonObject.put(VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.STREET, false));
                break;
            case DBConstants.KEY.LANDMARK:
                jsonObject.put(VALUE, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.LANDMARK, false));
                break;
            default:
                JsonFormUtils.processPopulatableFields(client, jsonObject);
                break;
        }
    }

    public JSONObject getAutoPopulatedJsonEditMemberFormString(@StringRes int formTitle, String formName,
                                                               CommonPersonObjectClient client, String updateEventType, String familyName) {
        try {

            // get the event and the client from ec model

            JSONObject form = formUtils.getFormJson(formName);
            if (form != null) {
                form.put(ENTITY_ID, client.getCaseId());
                form.put(ENCOUNTER_TYPE, updateEventType);

                JSONObject metadata = form.getJSONObject(METADATA);
                String lastLocationId = locationHelper.getOpenMrsLocationId(locationPickerView.getSelectedItem());

                metadata.put(ENCOUNTER_LOCATION, lastLocationId);

                form.put(CURRENT_OPENSRP_ID, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false));

                //inject opensrp id into the form
                JSONObject stepOne = form.getJSONObject(STEP1);


                stepOne.put(Constants.JsonForm.TITLE, context.getString(formTitle));


                JSONArray jsonArray = stepOne.getJSONArray(FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    try {
                        processFieldsForMemberEdit(client, jsonObject, jsonArray, familyName);
                    } catch (Exception e) {
                        Timber.e(Log.getStackTraceString(e));
                    }
                }

                return form;
            }
        } catch (Exception e) {
            Timber.e(Log.getStackTraceString(e));
        }

        return null;
    }


    private void processFieldsForMemberEdit(CommonPersonObjectClient client, JSONObject jsonObject,
                                            JSONArray jsonArray, String familyName) throws JSONException {


        switch (jsonObject.getString(KEY).toLowerCase()) {
            case JSON_FORM_KEY.DOB_UNKNOWN:
                computeDOBUnknown(jsonObject, client);
                break;

            case DatabaseKeys.AGE:
                computeAge(jsonObject, client);
                break;

            case DBConstants.KEY.DOB:
                computeDOB(jsonObject, client);
                break;

            case DBConstants.KEY.UNIQUE_ID:
                computeID(jsonObject, client);
                break;

            case DatabaseKeys.FAMILY_NAME:
                computeFamName(client, jsonObject, jsonArray, familyName);
                break;

            default:
                String db_key = jsonDbMap.get(jsonObject.getString(KEY).toLowerCase());
                if (StringUtils.isNotBlank(db_key)) {
                    jsonObject.put(VALUE, Utils.getValue(client.getColumnmaps(), db_key, false));
                } else {
                    jsonObject.put(VALUE, Utils.getValue(client.getColumnmaps(), jsonObject.getString(KEY), false));
                }
                break;

        }
    }

    private void computeID(JSONObject jsonObject, CommonPersonObjectClient client) throws JSONException {
        String uniqueId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false);
        jsonObject.put(VALUE, uniqueId.replace("-", ""));
    }

    private void computeAge(JSONObject jsonObject, CommonPersonObjectClient client) throws JSONException {
        String dobString = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
        dobString = Utils.getDuration(dobString);
        dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : "0";
        jsonObject.put(VALUE, Integer.valueOf(dobString));
    }

    private void computeDOBUnknown(JSONObject jsonObject, CommonPersonObjectClient client) throws JSONException {
        jsonObject.put(READ_ONLY, false);
        JSONObject optionsObject = jsonObject.getJSONArray(JSON_FORM_KEY.OPTIONS).getJSONObject(0);
        optionsObject.put(VALUE, Utils.getValue(client.getColumnmaps(), JSON_FORM_KEY.DOB_UNKNOWN, false));
    }

    private void computeDOB(JSONObject jsonObject, CommonPersonObjectClient client) throws JSONException {
        String dobString = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
        if (StringUtils.isNotBlank(dobString)) {
            Date dob = Utils.dobStringToDate(dobString);
            if (dob != null) {
                jsonObject.put(VALUE, dd_MM_yyyy.format(dob));
            }
        }
    }


    private void computeFamName(CommonPersonObjectClient client, JSONObject jsonObject, JSONArray jsonArray, String familyName) throws JSONException {

        jsonObject.put(VALUE, familyName);

        String lastName = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.LAST_NAME, false);

        JSONObject sameAsFamName = getFieldJSONObject(jsonArray, FormKeys.SAME_AS_FAM_NAME);
        JSONObject sameOptions = sameAsFamName.getJSONArray(JSON_FORM_KEY.OPTIONS).getJSONObject(0);

        if (familyName.equals(lastName)) {
            sameOptions.put(VALUE, true);
        } else {
            sameOptions.put(VALUE, false);
        }

        JSONObject surname = getFieldJSONObject(jsonArray, FormKeys.SURNAME);
        if (!familyName.equals(lastName)) {
            surname.put(VALUE, lastName);
        } else {
            surname.put(VALUE, "");
        }
    }

    public static Event createUpdateMemberSurnameEvent(String baseEntityId, Event updateFamilyEvent) {
        FamilyMetadata familyMetadata = RevealApplication.getInstance().getMetadata();
        Event updateMemberSurnameEvent =
                (Event) new Event().withBaseEntityId(baseEntityId).withEventDate(new Date()).withEventType(familyMetadata.familyMemberRegister.updateEventType)
                        .withLocationId(updateFamilyEvent.getLocationId()).withProviderId(updateFamilyEvent.getProviderId()).withEntityType(familyMetadata.familyMemberRegister.tableName)
                        .withFormSubmissionId(UUID.randomUUID().toString()).withDateCreated(new Date());
        tagSyncMetadata(RevealApplication.getInstance().getContext().allSharedPreferences(), updateMemberSurnameEvent);
        updateMemberSurnameEvent.setDetails(updateFamilyEvent.getDetails());
        return updateMemberSurnameEvent;
    }


}
