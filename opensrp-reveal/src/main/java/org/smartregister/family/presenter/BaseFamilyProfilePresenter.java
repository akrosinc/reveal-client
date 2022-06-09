package org.smartregister.family.presenter;

import android.content.Intent;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.reveal.R;
import org.smartregister.family.activity.BaseFamilyProfileActivity;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyProfileInteractor;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by keyman on 19/11/2018.
 */
public class BaseFamilyProfilePresenter implements FamilyProfileContract.Presenter, FamilyProfileContract.InteractorCallBack {

    protected WeakReference<FamilyProfileContract.View> view;
    protected FamilyProfileContract.Interactor interactor;
    protected FamilyProfileContract.Model model;

    protected String familyBaseEntityId;
    protected String familyHead;
    protected String primaryCaregiver;
    protected String familyName;

    public BaseFamilyProfilePresenter(FamilyProfileContract.View loginView, FamilyProfileContract.Model model, String familyBaseEntityId, String familyHead, String primaryCaregiver, String familyName) {
        this.view = new WeakReference<>(loginView);
        this.interactor = new FamilyProfileInteractor();
        this.model = model;
        this.familyBaseEntityId = familyBaseEntityId;
        this.familyHead = familyHead;
        this.primaryCaregiver = primaryCaregiver;
        this.familyName = familyName;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {

        view = null;//set to null on destroy

        // Inform interactor
        interactor.onDestroy(isChangingConfiguration);

        // Activity destroyed set interactor to null
        if (!isChangingConfiguration) {
            interactor = null;
        }

    }

    @Override
    public void fetchProfileData() {
        interactor.refreshProfileView(familyBaseEntityId, true, this);
    }

    @Override
    public void refreshProfileView() {
        interactor.refreshProfileView(familyBaseEntityId, false, this);
    }

    @Override
    public FamilyProfileContract.View getView() {
        if (view != null) {
            return view.get();
        } else {
            return null;
        }
    }

    @Override
    public void processFormDetailsSave(Intent data, AllSharedPreferences allSharedPreferences) {
        try {
            String jsonString = data.getStringExtra(Constants.INTENT_KEY.JSON);
            Timber.d(jsonString);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient client) {

        if (client == null || client.getColumnmaps() == null) {
            return;
        }

        String firstName = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String famName;

        if (Utils.getBooleanProperty(Constants.Properties.FAMILY_HEAD_FIRSTNAME_ENABLED)) {

            String familyHeadFirstName = Utils.getValue(client.getColumnmaps(), Constants.KEY.FAMILY_HEAD_NAME, true);
            famName = (( BaseFamilyProfileActivity ) getView()).getString(R.string.family_profile_title_with_firstname, familyHeadFirstName, firstName);

        } else {

            famName = (( BaseFamilyProfileActivity ) getView()).getString(R.string.family_profile_title, firstName);
        }

        getView().setProfileName(famName);

        String villageTown = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, false);
        getView().setProfileDetailOne(villageTown);

        /*String dobString = Utils.getDuration(Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false));
        dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;
        dobString = String.format(getView().getString(R.string.age_text), dobString);
        getView().setProfileDetailTwo(dobString);

        String phoneNumber = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.PHONE_NUMBER, false);
        getView().setProfileDetailThree(phoneNumber);*/

        getView().setProfileImage(client.getCaseId());

    }

    @Override
    public void startFormForEdit(CommonPersonObjectClient client) {
        JSONObject form = JsonFormUtils.getAutoPopulatedJsonEditFormString(getView().getApplicationContext(), client);
        try {
            getView().startFormActivity(form);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void startForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {

//        if (StringUtils.isBlank(entityId)) {
//            Triple<String, String, String> triple = Triple.of(formName, metadata, currentLocationId);
//            interactor.getNextUniqueId(triple, this);
//            return;
//        }

        JSONObject form = model.getFormAsJson(formName, entityId, currentLocationId);
        getView().startFormActivity(form);

    }

    @Override
    public void onNoUniqueId() {
        getView().displayShortToast(R.string.no_unique_id);
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId) {
        try {
            startForm(triple.getLeft(), entityId, triple.getMiddle(), triple.getRight());
        } catch (Exception e) {
            Timber.e(e);
            getView().displayToast(R.string.error_unable_to_start_form);
        }
    }

    @Override
    public void saveFamilyMember(String jsonString) {

        try {
            getView().showProgressDialog(R.string.saving_dialog_title);

            FamilyEventClient familyEventClient = model.processMemberRegistration(jsonString, familyBaseEntityId);
            if (familyEventClient == null) {
                return;
            }

            interactor.saveRegistration(familyEventClient, jsonString, false, this);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void updateFamilyRegister(String jsonString) {

        try {
            getView().showProgressDialog(R.string.saving_dialog_title);

            FamilyEventClient familyEventClient = model.processFamilyRegistrationForm(jsonString, familyBaseEntityId);
            if (familyEventClient == null) {
                return;
            }

            interactor.saveRegistration(familyEventClient, jsonString, true, this);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void onRegistrationSaved(boolean editMode, boolean isSaved, FamilyEventClient familyEventClient) {
        getView().refreshMemberList(FetchStatus.fetched);
        getView().hideProgressDialog();
    }

    public String familyBaseEntityId() {
        return familyBaseEntityId;
    }

    public void setInteractor(FamilyProfileContract.Interactor interactor) {
        this.interactor = interactor;
    }
}
