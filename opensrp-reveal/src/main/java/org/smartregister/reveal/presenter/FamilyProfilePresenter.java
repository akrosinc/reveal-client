package org.smartregister.reveal.presenter;

import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.presenter.BaseFamilyProfilePresenter;
import org.smartregister.reveal.BuildConfig;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.FamilyProfileContract;
import org.smartregister.reveal.interactor.RevealFamilyProfileInteractor;
import org.smartregister.reveal.model.FamilyProfileModel;
import org.smartregister.reveal.util.AppExecutors;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.FamilyConstants.DatabaseKeys;
import org.smartregister.reveal.util.FamilyConstants.JSON_FORM;
import org.smartregister.reveal.util.FamilyJsonFormUtils;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.Utils;

import static org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STRUCTURE_ID;
import static org.smartregister.reveal.util.FamilyConstants.TABLE_NAME.FAMILY;

/**
 * Created by samuelgithengi on 4/10/19.
 */
public class FamilyProfilePresenter extends BaseFamilyProfilePresenter implements FamilyProfileContract.Presenter {
    private AppExecutors appExecutors;
    private SQLiteDatabase database;
    private String structureId;
    private PreferencesUtil preferencesUtil;

    private FamilyJsonFormUtils familyJsonFormUtils;

    public FamilyProfilePresenter(FamilyProfileContract.View view, FamilyProfileContract.Model model, String familyBaseEntityId, String familyHead, String primaryCaregiver, String familyName) {
        super(view, model, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
        appExecutors = RevealApplication.getInstance().getAppExecutors();
        database = RevealApplication.getInstance().getRepository().getReadableDatabase();
        preferencesUtil = PreferencesUtil.getInstance();
        getStructureId(familyBaseEntityId);
        setInteractor(new RevealFamilyProfileInteractor(this));
        try {
            familyJsonFormUtils = new FamilyJsonFormUtils(getView().getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "error Intitializing FamilyJsonFormUtils ");
        }
    }

    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient client) {
        super.refreshProfileTopSection(client);
        getView().setProfileDetailOne(preferencesUtil.getCurrentOperationalArea());
        getView().setProfileDetailTwo(preferencesUtil.getCurrentDistrict());
    }


    private void getStructureId(String familyId) {
        appExecutors.diskIO().execute(() -> {
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(String.format("SELECT DISTINCT %s FROM %S WHERE %s = ?",
                        STRUCTURE_ID, FAMILY, BASE_ENTITY_ID), new String[]{familyId});
                if (cursor.moveToNext()) {
                    structureId = cursor.getString(0);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting residence for" + familyId, e);
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            if (structureId != null) {
                appExecutors.mainThread().execute(() -> {
                    getModel().setStructureId(structureId);
                    getView().setStructureId(structureId);
                });
            }
        });
    }

    private FamilyProfileModel getModel() {
        return (FamilyProfileModel) model;
    }

    public String getStructureId() {
        return structureId;
    }

    @Override
    public FamilyProfileContract.View getView() {
        return (FamilyProfileContract.View) super.getView();
    }

    @Override
    public void onRegistrationSaved(boolean isEdit) {
        if (!isEdit && Utils.getInterventionLabel() == R.string.focus_investigation) {
            getInteractor().generateTasks(getView().getApplicationContext(),
                    getModel().getEventClient().getEvent().getBaseEntityId());
            return;
        } else {
            FamilyEventClient eventClient = getModel().getEventClient();
            for (Obs obs : eventClient.getEvent().getObs()) {
                if (obs.getFieldCode().equals(DatabaseKeys.OLD_FAMILY_NAME)) {
                    String oldSurname = obs.getValue().toString();
                    if (!eventClient.getClient().getFirstName().equals(oldSurname)) {  //family name was changed
                        getInteractor().updateFamilyMemberSurname(eventClient.getClient(), eventClient.getEvent(), oldSurname);
                        return;
                    }
                }
            }
        }
        super.onRegistrationSaved(isEdit);
    }

    @Override
    public void onTasksGenerated() {
        super.onRegistrationSaved(false);
        getView().refreshTasks(structureId);

    }

    @Override
    public void onMembersUpdated() {
        onTasksGenerated();
    }

    private FamilyProfileContract.Interactor getInteractor() {
        return (FamilyProfileContract.Interactor) interactor;
    }

    @Override
    public void startFormForEdit(CommonPersonObjectClient client) {
        String formName = BuildConfig.BUILD_COUNTRY == Country.THAILAND ? JSON_FORM.THAILAND_FAMILY_UPDATE : JSON_FORM.FAMILY_UPDATE;
        JSONObject form = familyJsonFormUtils.getAutoPopulatedJsonEditFormString(formName,
                getView().getApplicationContext(), client, RevealApplication.getInstance().getMetadata().familyRegister.updateEventType);
        try {
            getView().startFormActivity(form);

        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
    }
}