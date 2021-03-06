package org.smartregister.reveal.contract;

import android.content.Context;

import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.contract.FamilyOtherMemberContract;


public interface FamilyOtherMemberProfileContract {

    interface BasePresenter {
        void onFetchFamilyHead(CommonPersonObject commonPersonObject);

        void onArchiveMemberCompleted(boolean isSuccessful);
    }

    interface Presenter extends BasePresenter, FamilyOtherMemberContract.Presenter {
        void onEditMemberDetails();

        void updateFamilyMember(String jsonString);

        void onArchiveFamilyMember();
    }

    interface View extends FamilyOtherMemberContract.View {

        void startFormActivity(JSONObject jsonForm);

        Context getContext();

        void refreshList();
    }


    interface Interactor extends FamilyOtherMemberContract.Interactor {

        void getFamilyHead(BasePresenter presenter, String familyHeadId);

        void archiveFamilyMember(BasePresenter presenter, CommonPersonObjectClient client);
    }
}
