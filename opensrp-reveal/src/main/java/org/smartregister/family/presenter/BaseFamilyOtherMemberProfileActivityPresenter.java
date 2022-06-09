package org.smartregister.family.presenter;

import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.reveal.R;
import org.smartregister.family.contract.FamilyOtherMemberContract;
import org.smartregister.family.interactor.FamilyOtherMemberProfileInteractor;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.TreeSet;

import static org.smartregister.util.Utils.getName;

public class BaseFamilyOtherMemberProfileActivityPresenter implements FamilyOtherMemberContract.Presenter, FamilyOtherMemberContract.InteractorCallBack {

    protected WeakReference<FamilyOtherMemberContract.View> viewReference;

    protected FamilyOtherMemberContract.Model model;

    protected RegisterConfiguration config;

    protected String baseEntityId;
    protected String familyHead;
    protected String primaryCaregiver;
    protected String villageTown;

    protected Set<org.smartregister.configurableviews.model.View> visibleColumns = new TreeSet<>();

    private FamilyOtherMemberProfileInteractor interactor;

    private String viewConfigurationIdentifier;

    public BaseFamilyOtherMemberProfileActivityPresenter(FamilyOtherMemberContract.View view, FamilyOtherMemberContract.Model model, String viewConfigurationIdentifier, String baseEntityId, String familyHead, String primaryCaregiver, String villageTown) {
        this.viewReference = new WeakReference<>(view);
        this.model = model;
        this.viewConfigurationIdentifier = viewConfigurationIdentifier;
        this.config = model.defaultRegisterConfiguration();

        this.baseEntityId = baseEntityId;
        this.familyHead = familyHead;
        this.primaryCaregiver = primaryCaregiver;
        this.villageTown = villageTown;

        this.interactor = new FamilyOtherMemberProfileInteractor();
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {

        viewReference = null;//set to null on destroy

        // Inform interactor
        interactor.onDestroy(isChangingConfiguration);

        // Activity destroyed set interactor to null
        if (!isChangingConfiguration) {
            interactor = null;
        }
    }

    protected FamilyOtherMemberContract.View getView() {
        if (viewReference != null)
            return viewReference.get();
        else
            return null;
    }


    @Override
    public void fetchProfileData() {
        interactor.refreshProfileView(baseEntityId, this);
    }

    @Override
    public void refreshProfileView() {
        interactor.refreshProfileView(baseEntityId, this);
    }


    private void setVisibleColumns(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        this.visibleColumns = visibleColumns;
    }

    public void setModel(FamilyOtherMemberContract.Model model) {
        this.model = model;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }


    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient client) {

        if (client == null || client.getColumnmaps() == null) {
            return;
        }

        String firstName = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String lastName = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);

        getView().setProfileName(getName(firstName, lastName));

        String gender = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.GENDER, true);
        getView().setProfileDetailOne(gender);

        getView().setProfileDetailTwo(villageTown);

        String uniqueId = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false);
        getView().setProfileDetailThree(String.format(getView().getString(R.string.id_with_value), uniqueId));

        String entityType = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.ENTITY_TYPE, false);

        if (baseEntityId.equals(familyHead)) {
            getView().toggleFamilyHead(true);
        } else {
            getView().toggleFamilyHead(false);
        }

        if (baseEntityId.equals(primaryCaregiver)) {
            getView().togglePrimaryCaregiver(true);
        } else {
            getView().togglePrimaryCaregiver(false);
        }

        /*String dobString = Utils.getDuration(Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false));
        dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;
        dobString = String.format(getView().getString(R.string.age_text), dobString);
        getView().setProfileDetailTwo(dobString);

        String phoneNumber = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.PHONE_NUMBER, false);
        getView().setProfileDetailThree(phoneNumber);*/

        getView().setProfileImage(client.getCaseId(), entityType);
    }
}