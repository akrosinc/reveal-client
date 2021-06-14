package com.revealprecision.reveal.model;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.model.BaseFamilyProfileModel;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import com.revealprecision.reveal.BuildConfig;
import com.revealprecision.reveal.application.RevealApplication;
import com.revealprecision.reveal.util.Constants;
import com.revealprecision.reveal.util.PreferencesUtil;

import static com.revealprecision.reveal.util.Constants.DatabaseKeys.LAST_NAME;
import static com.revealprecision.reveal.util.FamilyConstants.RELATIONSHIP.RESIDENCE;

/**
 * Created by samuelgithengi on 4/12/19.
 */
public class FamilyProfileModel extends BaseFamilyProfileModel {

    private String structureId;

    private FamilyEventClient eventClient;

    private CommonPersonObject familyHeadPersonObject;

    private com.vijay.jsonwizard.utils.FormUtils formUtils = new com.vijay.jsonwizard.utils.FormUtils();


    public FamilyProfileModel(String familyName) {
        super(familyName);
    }

    @Override
    public FamilyEventClient processMemberRegistration(String jsonString, String familyBaseEntityId) {
        eventClient = super.processMemberRegistration(jsonString, familyBaseEntityId);
        tagEventClientDetails(eventClient);
        return eventClient;
    }

    @Override
    public FamilyEventClient processFamilyRegistrationForm(String jsonString, String familyBaseEntityId) {
        eventClient = super.processFamilyRegistrationForm(jsonString, familyBaseEntityId);
        tagEventClientDetails(eventClient);
        return eventClient;
    }

    @Override
    public FamilyEventClient processUpdateMemberRegistration(String jsonString, String familyBaseEntityId) {
        eventClient = super.processUpdateMemberRegistration(jsonString, familyBaseEntityId);
        tagEventClientDetails(eventClient);
        return eventClient;
    }

    private void tagEventClientDetails(FamilyEventClient eventClient) {
        if (eventClient == null)
            return;
        if (structureId != null) {
            eventClient.getClient().addAttribute(RESIDENCE, structureId);
            eventClient.getEvent().addDetails(Constants.Properties.LOCATION_ID, structureId);
        }
        String planIdentifier = PreferencesUtil.getInstance().getCurrentPlanId();
        eventClient.getEvent().addDetails(Constants.Properties.PLAN_IDENTIFIER, planIdentifier);
        eventClient.getEvent().addDetails(Constants.Properties.APP_VERSION_NAME, BuildConfig.VERSION_NAME);
        String locationId = com.revealprecision.reveal.util.Utils.getOperationalAreaLocation(PreferencesUtil.getInstance().getCurrentOperationalArea()).getId();
        eventClient.getClient().setLocationId(locationId);
        eventClient.getEvent().setLocationId(locationId);
    }

    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }


    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        String formattedFormName = formName.replace(Constants.JsonForm.JSON_FORM_FOLDER, "").replace(JsonFormConstants.JSON_FILE_EXTENSION, "");
        JSONObject form = formUtils.getFormJsonFromRepositoryOrAssets(RevealApplication.getInstance().getApplicationContext(), formattedFormName);
        if (form == null) {
            return null;
        }
        form = JsonFormUtils.getFormAsJson(form, formName, entityId, currentLocationId);

        if (formName.equals(Utils.metadata().familyMemberRegister.formName)) {
            JsonFormUtils.updateJsonForm(form, familyHeadPersonObject.getColumnmaps().get(LAST_NAME));
        }

        return form;
    }

    public void setFamilyHeadPersonObject(CommonPersonObject familyHeadPersonObject) {
        this.familyHeadPersonObject = familyHeadPersonObject;
    }

    public CommonPersonObject getFamilyHeadPersonObject() {
        return familyHeadPersonObject;
    }
}
