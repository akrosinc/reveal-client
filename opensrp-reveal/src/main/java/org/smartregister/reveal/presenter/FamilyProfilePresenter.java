package org.smartregister.reveal.presenter;

import static org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STRUCTURE_ID;
import static org.smartregister.reveal.util.FamilyConstants.TABLE_NAME.FAMILY;
import static org.smartregister.reveal.util.FamilyConstants.TABLE_NAME.FAMILY_MEMBER;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.gson.Gson;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Event;
import org.smartregister.domain.Task;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.presenter.BaseFamilyProfilePresenter;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.FamilyOtherMemberProfileContract;
import org.smartregister.reveal.contract.FamilyProfileContract;
import org.smartregister.reveal.interactor.RevealFamilyOtherMemberInteractor;
import org.smartregister.reveal.interactor.RevealFamilyProfileInteractor;
import org.smartregister.reveal.model.FamilyProfileModel;
import org.smartregister.reveal.util.AlertDialogUtils;
import org.smartregister.reveal.util.AppExecutors;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.FamilyConstants.DatabaseKeys;
import org.smartregister.reveal.util.FamilyConstants.JSON_FORM;
import org.smartregister.reveal.util.FamilyJsonFormUtils;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.Utils;
import timber.log.Timber;

/**
 * Created by samuelgithengi on 4/10/19.
 */
public class FamilyProfilePresenter extends BaseFamilyProfilePresenter implements FamilyProfileContract.Presenter, FamilyOtherMemberProfileContract.BasePresenter {
    public static final String UPDATE_FAMILY_NAME = ".UpdateFamilyName";
    public static final String EVENT = "event";
    public static final String CLIENT = "client";
    public static final String OLD_FAMILY_NAME = "oldFamilyName";
    public static final String NEW_FAMILY_NAME = "newFamilyName";
    private AppExecutors appExecutors;
    private SQLiteDatabase database;
    private String structureId;
    private PreferencesUtil preferencesUtil;

    private FamilyJsonFormUtils familyJsonFormUtils;

    private FamilyOtherMemberProfileContract.Interactor otherMemberInteractor;

    private Boolean skipUpdate = false;

    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;
    public FamilyProfilePresenter(FamilyProfileContract.View view, FamilyProfileContract.Model model, String familyBaseEntityId, String familyHead, String primaryCaregiver, String familyName) {
        super(view, model, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
        appExecutors = RevealApplication.getInstance().getAppExecutors();
        database = RevealApplication.getInstance().getRepository().getReadableDatabase();
        preferencesUtil = PreferencesUtil.getInstance();
        getStructureId(familyBaseEntityId);
        getFamilyName(familyHead);
        setInteractor(new RevealFamilyProfileInteractor(this));
        try {
            familyJsonFormUtils = new FamilyJsonFormUtils(getView().getApplicationContext());
        } catch (Exception e) {
            Timber.e(e, "error Initializing FamilyJsonFormUtils ");
        }
        otherMemberInteractor = new RevealFamilyOtherMemberInteractor();
        FamilyProfilePresenter that = this;
        FamilyProfileContract.Interactor interactor = getInteractor();
        broadcastReceiver  = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String oldFamilyName = intent.getStringExtra(OLD_FAMILY_NAME);
                String newFamilyName = intent.getStringExtra(NEW_FAMILY_NAME);
                Gson gson = new Gson();
                org.smartregister.clientandeventmodel.Event event = gson.fromJson(intent.getStringExtra(EVENT), org.smartregister.clientandeventmodel.Event.class);
                Client client = gson.fromJson(intent.getStringExtra(CLIENT), Client.class);
                if(!oldFamilyName.equalsIgnoreCase(newFamilyName))
                     skipUpdate  = true;
                     interactor.updateFamilyMemberName(client,event,oldFamilyName);
                that.familyName = newFamilyName;
            }
        };
      localBroadcastManager = LocalBroadcastManager.getInstance(getView().getContext().getApplicationContext());
      localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(UPDATE_FAMILY_NAME));
    }

    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient client) {
        super.refreshProfileTopSection(client);
        getView().setProfileDetailOne(preferencesUtil.getCurrentOperationalArea());
        getView().setProfileDetailTwo(preferencesUtil.getCurrentDistrict());
        setFamilyName();
    }


    private void setFamilyName() {
        if (this.familyName != null && this.familyName.length() > 0) {
            getView().setProfileName(this.familyName + " Family");
        }
    }

    private void getFamilyName(String familyHead) {
        // HEADS UP
        if (StringUtils.isNotBlank(familyName)) {
            return;
        }

        appExecutors.diskIO().execute(() -> {
            Cursor cursor = null;

            String lastName = null;

            try {
                String sql = "SELECT last_name FROM " + FAMILY_MEMBER + " WHERE id = ?";
                cursor = database.rawQuery(sql, new String[]{familyHead});
                if (cursor.moveToNext()) {
                    lastName = cursor.getString(0);
                }
            } catch (Exception e) {
                Timber.e(e, "Error getting first name for" + familyHead);
            } finally {
                if (cursor != null)
                    cursor.close();
            }

            if (lastName != null && lastName.length() > 0) {
                familyName = lastName;

                appExecutors.mainThread().execute(() -> {
                    getModel().setFamilyName(familyName);
                    getView().updateFamilyName(familyName);
                });
            }
        });
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
                Timber.e(e, "Error getting residence for" + familyId);
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
    public void onRegistrationSaved(boolean editMode, boolean isSaved, FamilyEventClient eventClient) {
        if (!editMode && isSaved && Utils.isFocusInvestigationOrMDA()) {
            getInteractor().generateTasks(getView().getApplicationContext(),
                    eventClient.getEvent().getBaseEntityId(), structureId, eventClient.getClient().getBirthdate());
            return;
        } else if (editMode && isSaved) {
            for (Obs obs : eventClient.getEvent().getObs()) {
                if (obs.getFieldCode().equals(DatabaseKeys.OLD_FAMILY_NAME)) {
                    String oldSurname = obs.getValue().toString();
                    if (!eventClient.getClient().getFirstName().equals(oldSurname)) {  //family name was changed
                        getInteractor().updateFamilyMemberName(eventClient.getClient(), eventClient.getEvent(), oldSurname);
                        getView().updateFamilyName(eventClient.getClient().getFirstName());
                        break;
                    }
                }
            }
            familyName = eventClient.getClient().getFirstName();
            setFamilyName();
        }
        super.onRegistrationSaved(editMode, isSaved, eventClient);
    }

    @Override
    public void onTasksGenerated() {
        super.onRegistrationSaved(false, true, null);
        getView().refreshTasks(structureId);

    }

    @Override
    public void onMembersUpdated() {
        if(!skipUpdate)
          onTasksGenerated();
    }

    @Override
    public void onAddFamilyMember() {
        if (getModel().getFamilyHeadPersonObject() == null) {
            otherMemberInteractor.getFamilyHead(this, familyHead);
        } else {
            openAddMemberForm();
        }
    }

    @Override
    public void onArchiveFamilyClicked() {
        AlertDialogUtils.displayNotificationWithCallback(getView().getContext(),
                R.string.confirm_archive_family, R.string.confirm_archive_family_message, R.string.confirm, R.string.cancel, (dialog, buttonClicked) -> {
                    if (buttonClicked == DialogInterface.BUTTON_POSITIVE) {
                        archiveFamily();
                    }
                    dialog.dismiss();
                });
    }

    @Override
    public void onArchiveFamilyCompleted(boolean isSuccessfulSaved, Task task) {
        getView().hideProgressDialog();
        if (!isSuccessfulSaved) {
            AlertDialogUtils.displayNotification(getView().getContext(), R.string.archive_family,
                    R.string.archive_family_failed, familyName);
        } else {
            RevealApplication.getInstance().setRefreshMapOnEventSaved(true);
            getView().returnToMapView(structureId, task);
        }
    }

    private void archiveFamily() {
        getView().showProgressDialog(org.smartregister.reveal.R.string.saving_dialog_title);
        getInteractor().archiveFamily(familyBaseEntityId, structureId);
    }

    private FamilyProfileContract.Interactor getInteractor() {
        return (FamilyProfileContract.Interactor) interactor;
    }

    @Override
    public void startFormForEdit(CommonPersonObjectClient client) {
        if (getBuildCountry() == Country.NIGERIA) {
            this.getInteractor().getRegistrationEvent(client, this.familyBaseEntityId);
        } else {
            String formName;
            if (getBuildCountry() == Country.THAILAND) {
                formName = JSON_FORM.THAILAND_FAMILY_UPDATE;
            } else if (getBuildCountry() == Country.ZAMBIA) {
                formName = JSON_FORM.ZAMBIA_FAMILY_UPDATE;
            } else if (getBuildCountry() == Country.NIGERIA) {
                // HEADS UP
//            formName = JSON_FORM.NIGERIA_FAMILY_UPDATE;
                formName = JSON_FORM.NIGERIA_FAMILY_REGISTER;

            } else if (getBuildCountry() == Country.REFAPP) {
                formName = JSON_FORM.REFAPP_FAMILY_UPDATE;
            } else {
                formName = JSON_FORM.FAMILY_UPDATE;
            }
            JSONObject form = familyJsonFormUtils.getAutoPopulatedJsonEditFormString(formName,
                    client, RevealApplication.getInstance().getMetadata().familyRegister.updateEventType);
            try {
                getView().startFormActivity(form);

            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    @NonNull
    private Country getBuildCountry() {
        return PreferencesUtil.getInstance().getBuildCountry();
    }

    @Override
        public void onEventFound(Event structureEvent, CommonPersonObjectClient client) {
            String formName = JSON_FORM.NIGERIA_FAMILY_UPDATE;

            JSONObject form = familyJsonFormUtils.getAutoPopulatedJsonEditMemberFormString(
                    formName,
                    client, RevealApplication.getInstance().getMetadata().familyRegister.updateEventType,
                    familyName, true);

            // Update the values for the structure data and set all fields to read only
            familyJsonFormUtils.populateForm(structureEvent, form, false);

            try {
                getView().startFormActivity(form, false);

            } catch (Exception e) {
                Timber.e(e);
            }
        }
        @Override
        public void onFetchFamilyHead (CommonPersonObject familyHeadPersonObject){
            getModel().setFamilyHeadPersonObject(familyHeadPersonObject);
            openAddMemberForm();
        }

        @Override
        public void onArchiveMemberCompleted ( boolean isSuccessful){
            //not used
        }

        private void openAddMemberForm () {
            try {
                startForm(RevealApplication.getInstance().getMetadata().familyMemberRegister.formName, null, null, RevealApplication.getInstance().getContext().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID));
            } catch (Exception e) {
                Timber.e(e, "Error opening add member form");
            }
        }
}