package org.smartregister.family.presenter;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.domain.FetchStatus;
import org.smartregister.reveal.R;
import org.smartregister.family.contract.FamilyRegisterContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyRegisterInteractor;
import org.smartregister.repository.AllSharedPreferences;

import java.lang.ref.WeakReference;
import java.util.List;

import timber.log.Timber;

/**
 * Created by keyman on 12/11/2018.
 */
public class BaseFamilyRegisterPresenter implements FamilyRegisterContract.Presenter, FamilyRegisterContract.InteractorCallBack {

    protected WeakReference<FamilyRegisterContract.View> viewReference;
    protected FamilyRegisterContract.Interactor interactor;
    protected FamilyRegisterContract.Model model;

    public BaseFamilyRegisterPresenter(FamilyRegisterContract.View view, FamilyRegisterContract.Model model) {
        viewReference = new WeakReference<>(view);
        interactor = new FamilyRegisterInteractor();
        this.model = model;
    }

    public void setModel(FamilyRegisterContract.Model model) {
        this.model = model;
    }

    public void setInteractor(FamilyRegisterContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void registerViewConfigurations(List<String> viewIdentifiers) {
        model.registerViewConfigurations(viewIdentifiers);
    }

    @Override
    public void unregisterViewConfiguration(List<String> viewIdentifiers) {
        model.unregisterViewConfiguration(viewIdentifiers);
    }

    @Override
    public void saveLanguage(String language) {
        model.saveLanguage(language);
        if (getView() != null)
            getView().displayToast(language + " selected");
    }

    @Override
    public void startForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {

        if (StringUtils.isBlank(entityId)) {
            Triple<String, String, String> triple = Triple.of(formName, metadata, currentLocationId);
            interactor.getNextUniqueId(triple, this);
            return;
        }

        JSONObject form = model.getFormAsJson(formName, entityId, currentLocationId);
        if (getView() != null)
            getView().startFormActivity(form);

    }

    @Override
    public void closeFamilyRecord(String jsonString) {

        try {
            if (getView() != null) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

                Timber.d(jsonString);
                //getView().showProgressDialog(jsonString.contains(Constants.EventType.CLOSE) ? R.string.removing_dialog_title : R.string.saving_dialog_title);

                interactor.removeFamilyFromRegister(jsonString, allSharedPreferences.fetchRegisteredANM());
            }
        } catch (Exception e) {
            Timber.e(e);

        }
    }

    @Override
    public void saveForm(String jsonString, boolean isEditMode) {

        try {

            if (getView() != null)
                getView().showProgressDialog(R.string.saving_dialog_title);

            List<FamilyEventClient> familyEventClientList = model.processRegistration(jsonString);
            if (familyEventClientList == null || familyEventClientList.isEmpty()) {
                return;
            }

            interactor.saveRegistration(familyEventClientList, jsonString, isEditMode, this);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void onNoUniqueId() {
        if (getView() != null)
            getView().displayShortToast(R.string.no_unique_id);
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId) {
        try {
            startForm(triple.getLeft(), entityId, triple.getMiddle(), triple.getRight());
        } catch (Exception e) {
            Timber.e(Log.getStackTraceString(e));
            if (getView() != null)
                getView().displayToast(R.string.error_unable_to_start_form);
        }
    }

    @Override
    public void onRegistrationSaved(boolean isEditMode, boolean isSaved, List<FamilyEventClient> familyEventClientList) {
        if (getView() != null) {
            getView().refreshList(FetchStatus.fetched);
            getView().hideProgressDialog();
        }
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {

        viewReference = null;//set to null on destroy
        // Inform interactor
        if (interactor != null)
            interactor.onDestroy(isChangingConfiguration);
        // Activity destroyed set interactor to null
        if (!isChangingConfiguration) {
            interactor = null;
            model = null;
        }
    }

    @Override
    public void updateInitials() {
        String initials = model.getInitials();
        if (initials != null && getView() != null) {
            getView().updateInitialsText(initials);
        }
    }

    private FamilyRegisterContract.View getView() {
        if (viewReference != null)
            return viewReference.get();
        else
            return null;
    }
}
