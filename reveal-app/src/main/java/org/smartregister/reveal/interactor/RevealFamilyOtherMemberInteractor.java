package com.revealprecision.reveal.interactor;


import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.interactor.FamilyOtherMemberProfileInteractor;
import com.revealprecision.reveal.contract.FamilyOtherMemberProfileContract;
import com.revealprecision.reveal.contract.FamilyOtherMemberProfileContract.Interactor;
import com.revealprecision.reveal.sync.RevealClientProcessor;
import com.revealprecision.reveal.util.AppExecutors;
import com.revealprecision.reveal.util.InteractorUtils;

import static com.revealprecision.reveal.application.RevealApplication.getInstance;

public class RevealFamilyOtherMemberInteractor extends FamilyOtherMemberProfileInteractor implements Interactor {

    private CommonRepository commonRepository;

    private AppExecutors appExecutors;

    private InteractorUtils interactorUtils;

    public RevealFamilyOtherMemberInteractor() {
        commonRepository = getInstance().getContext().commonrepository(getInstance().getMetadata().familyMemberRegister.tableName);
        appExecutors = getInstance().getAppExecutors();
        interactorUtils = new InteractorUtils(getInstance().getTaskRepository(), getInstance().getContext().getEventClientRepository(), (RevealClientProcessor) getInstance().getClientProcessor());
    }

    @Override
    public void getFamilyHead(FamilyOtherMemberProfileContract.BasePresenter presenter, String familyHeadId) {
        appExecutors.diskIO().execute(() -> {
            CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(familyHeadId);
            appExecutors.mainThread().execute(() -> {
                presenter.onFetchFamilyHead(commonPersonObject);
            });
        });
    }

    @Override
    public void archiveFamilyMember(FamilyOtherMemberProfileContract.BasePresenter presenter, CommonPersonObjectClient client) {
        appExecutors.diskIO().execute(() -> {
            getInstance().getRepository().getWritableDatabase().beginTransaction();
            boolean saved;
            try {
                saved = interactorUtils.archiveClient(client.getCaseId(), false);
                getInstance().getRepository().getWritableDatabase().setTransactionSuccessful();
            } finally {
                getInstance().getRepository().getWritableDatabase().endTransaction();
            }
            appExecutors.mainThread().execute(() -> {
                presenter.onArchiveMemberCompleted(saved);
            });

        });
    }
}
