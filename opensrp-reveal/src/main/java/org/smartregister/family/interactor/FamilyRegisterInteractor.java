package org.smartregister.family.interactor;

import androidx.annotation.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.UniqueId;
import org.smartregister.domain.db.EventClient;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.contract.FamilyRegisterContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * Created by keyman 12/11/2018.
 */
public class FamilyRegisterInteractor implements FamilyRegisterContract.Interactor {

    public enum type {SAVED, UPDATED}


    protected AppExecutors appExecutors;

    @VisibleForTesting
    FamilyRegisterInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public FamilyRegisterInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void getNextUniqueId(final Triple<String, String, String> triple, final FamilyRegisterContract.InteractorCallBack callBack) {

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
    public void saveRegistration(final List<FamilyEventClient> familyEventClientList, final String jsonString, final boolean isEditMode, final FamilyRegisterContract.InteractorCallBack callBack) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final boolean isSaved = saveRegistration(familyEventClientList, jsonString, isEditMode);
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onRegistrationSaved(isEditMode, isSaved, familyEventClientList);
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void removeFamilyFromRegister(final String closeFormJsonString, final String providerId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //TODO add functionality to remove family from register
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    private boolean saveRegistration(List<FamilyEventClient> familyEventClientList, String jsonString, boolean isEditMode) {

        try {

            List<EventClient> eventClientList = new ArrayList<>();
            for (int i = 0; i < familyEventClientList.size(); i++) {
                FamilyEventClient familyEventClient = familyEventClientList.get(i);
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
                        String newOpenSRPId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey).replace("-", "");
                        String currentOpenSRPId = JsonFormUtils.getString(jsonString, JsonFormUtils.CURRENT_OPENSRP_ID).replace("-", "");
                        if (!newOpenSRPId.equals(currentOpenSRPId)) {
                            //OPENSRP ID was changed
                            getUniqueIdRepository().open(currentOpenSRPId);
                        }
                    }

                } else {
                    if (baseClient != null) {
                        String opensrpId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey);
                        if (StringUtils.isNotBlank(opensrpId) && !opensrpId.contains(Constants.IDENTIFIER.FAMILY_SUFFIX)) {
                            //mark OPENSRP ID as used
                            getUniqueIdRepository().close(opensrpId);
                        }
                    }
                }

                if (baseClient != null || baseEvent != null) {
                    String imageLocation = null;
                    if (i == 0) {
                        String familyStep = Utils.getCustomConfigs(Constants.CustomConfig.FAMILY_FORM_IMAGE_STEP);

                        imageLocation = (StringUtils.isBlank(familyStep)) ?
                                JsonFormUtils.getFieldValue(jsonString, Constants.KEY.PHOTO) :
                                JsonFormUtils.getFieldValue(jsonString, familyStep, Constants.KEY.PHOTO);

                    } else if (i == 1) {
                        String familyMemberStep = Utils.getCustomConfigs(Constants.CustomConfig.FAMILY_MEMBER_FORM_IMAGE_STEP);

                        imageLocation = (StringUtils.isBlank(familyMemberStep)) ?
                                JsonFormUtils.getFieldValue(jsonString, JsonFormUtils.STEP2, Constants.KEY.PHOTO) :
                                JsonFormUtils.getFieldValue(jsonString, familyMemberStep, Constants.KEY.PHOTO);
                    }

                    if (StringUtils.isNotBlank(imageLocation)) {
                        JsonFormUtils.saveImage(baseEvent.getProviderId(), baseClient.getBaseEntityId(), imageLocation);
                    }
                }
                org.smartregister.domain.Event domainEvent = JsonFormUtils.gson.fromJson(eventJson.toString(), org.smartregister.domain.Event.class);
                org.smartregister.domain.Client domainClient = JsonFormUtils.gson.fromJson(clientJson.toString(), org.smartregister.domain.Client.class);
                eventClientList.add(new EventClient(domainEvent, domainClient));
            }

            long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);

            processClient(eventClientList);
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

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        //TODO set presenter or model to null
    }

    public AllSharedPreferences getAllSharedPreferences() {
        return Utils.context().allSharedPreferences();
    }

    public UniqueIdRepository getUniqueIdRepository() {
        return FamilyLibrary.getInstance().getUniqueIdRepository();
    }


    public ECSyncHelper getSyncHelper() {
        return FamilyLibrary.getInstance().getEcSyncHelper();
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        return FamilyLibrary.getInstance().getClientProcessorForJava();
    }
}
