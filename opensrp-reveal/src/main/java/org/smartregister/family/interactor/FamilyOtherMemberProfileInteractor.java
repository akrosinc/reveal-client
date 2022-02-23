package org.smartregister.family.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.contract.FamilyOtherMemberContract;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Utils;

/**
 * Created by keyman on 15/01/2019.
 */
public class FamilyOtherMemberProfileInteractor implements FamilyOtherMemberContract.Interactor {

    private AppExecutors appExecutors;

    @VisibleForTesting
    FamilyOtherMemberProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public FamilyOtherMemberProfileInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {// TODO implement this
    }

    @Override
    public void refreshProfileView(final String baseEntityId, final FamilyOtherMemberContract.InteractorCallBack callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final CommonPersonObject personObject = getCommonRepository(Utils.metadata().familyMemberRegister.tableName).findByBaseEntityId(baseEntityId);
                final CommonPersonObjectClient pClient = new CommonPersonObjectClient(personObject.getCaseId(),
                        personObject.getDetails(), "");
                pClient.setColumnmaps(personObject.getColumnmaps());

                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.refreshProfileTopSection(pClient);
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    public CommonRepository getCommonRepository(String tableName) {
        return Utils.context().commonrepository(tableName);
    }
}
