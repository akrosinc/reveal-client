package org.smartregister.family.interactor;

import androidx.annotation.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.UniqueId;
import org.smartregister.domain.db.EventClient;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * Created by keyman on 19/11/2018.
 */
public class FamilyProfileInteractor implements FamilyProfileContract.Interactor {

    private AppExecutors appExecutors;

    @VisibleForTesting
    FamilyProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public FamilyProfileInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {// TODO implement this
    }

    @Override
    public void refreshProfileView(final String baseEntityId, final boolean isForEdit, final FamilyProfileContract.InteractorCallBack callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final CommonPersonObject personObject = getCommonRepository(Utils.metadata().familyRegister.tableName).findByBaseEntityId(baseEntityId);
                final CommonPersonObjectClient pClient = new CommonPersonObjectClient(personObject.getCaseId(),
                        personObject.getDetails(), "");
                String familyHeadId = personObject.getColumnmaps().get(DBConstants.KEY.FAMILY_HEAD);
                final CommonPersonObject familyHeadObject = getCommonRepository(Utils.metadata().familyMemberRegister.tableName).findByBaseEntityId(familyHeadId);

                String familyHeadName = "";
                if (familyHeadObject != null && familyHeadObject.getColumnmaps() != null)
                    familyHeadName = familyHeadObject.getColumnmaps().get(DBConstants.KEY.FIRST_NAME);

                personObject.getColumnmaps().put(Constants.KEY.FAMILY_HEAD_NAME, familyHeadName);
                pClient.setColumnmaps(personObject.getColumnmaps());

                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isForEdit) {
                            callback.startFormForEdit(pClient);
                        } else {
                            callback.refreshProfileTopSection(pClient);
                        }
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void getNextUniqueId(final Triple<String, String, String> triple, final FamilyProfileContract.InteractorCallBack callBack) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                UniqueId uniqueId = getUniqueIdRepository().getNextUniqueId();
                final String entityId = uniqueId != null ? uniqueId.getOpenmrsId() : "";
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (StringUtils.isBlank(entityId)) {
                            callBack.onNoUniqueId();
                        } else {
                            callBack.onUniqueIdFetched(triple, entityId);
                        }
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }


    @Override
    public void saveRegistration(final FamilyEventClient familyEventClient, final String jsonString, final boolean isEditMode, final FamilyProfileContract.InteractorCallBack callBack) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final boolean isSaved = saveRegistration(familyEventClient, jsonString, isEditMode);
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onRegistrationSaved(isEditMode,isSaved,familyEventClient);
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    private boolean saveRegistration(FamilyEventClient familyEventClient, String jsonString, boolean isEditMode) {

        try {

            Client baseClient = familyEventClient.getClient();
            Event baseEvent = familyEventClient.getEvent();
            JSONObject eventJson = null;
            JSONObject clientJson = null;


            if (baseClient != null) {
                clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
                if (isEditMode) {
                    JsonFormUtils.mergeAndSaveClient(getSyncHelper(), baseClient);
                } else {
                    getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
                }
            }

            if (baseEvent != null) {
                eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
            }

            if (isEditMode) {
                // Unassign current OPENSRP ID
                if (baseClient != null) {
                    String newOpenSRPId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey);
                    if (newOpenSRPId != null) {
                        newOpenSRPId.replace("-", "");
                        String currentOpenSRPId = JsonFormUtils.getString(jsonString, JsonFormUtils.CURRENT_OPENSRP_ID).replace("-", "");
                        if (!newOpenSRPId.equals(currentOpenSRPId)) {
                            //OPENSRP ID was changed
                            getUniqueIdRepository().open(currentOpenSRPId);
                        }
                    }
                }

            } else {
                if (baseClient != null) {
                    String opensrpId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey);

                    //mark OPENSRP ID as used
                    getUniqueIdRepository().close(opensrpId);
                }
            }

            if (baseClient != null || baseEvent != null) {
                String imageLocation = JsonFormUtils.getFieldValue(jsonString, Constants.KEY.PHOTO);
                JsonFormUtils.saveImage(baseEvent.getProviderId(), baseClient.getBaseEntityId(), imageLocation);
            }

            long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            org.smartregister.domain.Event domainEvent = JsonFormUtils.gson.fromJson(eventJson.toString(), org.smartregister.domain.Event.class);
            org.smartregister.domain.Client domainClient = JsonFormUtils.gson.fromJson(clientJson.toString(), org.smartregister.domain.Client.class);
            processClient(Collections.singletonList(new EventClient(domainEvent, domainClient)));
            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
            return true;
        } catch (Exception e) {
            Timber.e(e);
            return false;
        }
    }

    protected void processClient(List<EventClient> eventClientList) {
        try {
            getClientProcessorForJava().processClient(eventClientList);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public UniqueIdRepository getUniqueIdRepository() {
        return FamilyLibrary.getInstance().getUniqueIdRepository();
    }


    public AllSharedPreferences getAllSharedPreferences() {
        return Utils.context().allSharedPreferences();
    }

    public ECSyncHelper getSyncHelper() {
        return FamilyLibrary.getInstance().getEcSyncHelper();
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        return FamilyLibrary.getInstance().getClientProcessorForJava();
    }

    public CommonRepository getCommonRepository(String tableName) {
        return Utils.context().commonrepository(tableName);
    }
}
