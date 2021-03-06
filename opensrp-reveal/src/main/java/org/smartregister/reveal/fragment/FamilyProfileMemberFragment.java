package org.smartregister.reveal.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.model.BaseFamilyProfileMemberModel;
import org.smartregister.family.presenter.BaseFamilyProfileMemberPresenter;
import org.smartregister.family.util.Constants;
import org.smartregister.reveal.R;
import org.smartregister.reveal.view.FamilyOtherMemberProfileActivity;
import org.smartregister.reveal.viewholder.FamilyMemberViewHolder;

import java.util.HashMap;
import java.util.Set;

import static org.smartregister.reveal.util.Constants.DatabaseKeys.STRUCTURE_ID;

public class FamilyProfileMemberFragment extends BaseFamilyProfileMemberFragment {

    private String structureId;

    public static FamilyProfileMemberFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        FamilyProfileMemberFragment fragment = new FamilyProfileMemberFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializePresenter() {
        String familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        String familyHead = getArguments().getString(Constants.INTENT_KEY.FAMILY_HEAD);
        String primaryCareGiver = getArguments().getString(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
        presenter = new BaseFamilyProfileMemberPresenter(this, new BaseFamilyProfileMemberModel(), null, familyBaseEntityId, familyHead, primaryCareGiver);
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns, String familyHead, String primaryCaregiver) {
        FamilyMemberViewHolder familyMemberRegisterProvider = new FamilyMemberViewHolder(getActivity(), registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, familyMemberRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {//do nothing
    }

    @Override
    protected void onViewClicked(View view) {
        super.onViewClicked(view);
        switch (view.getId()) {
            case R.id.patient_column:
                if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
                    goToOtherMemberProfileActivity((CommonPersonObjectClient) view.getTag());
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean onViewLongClicked(final View view) {
        return false;
    }

    public void goToOtherMemberProfileActivity(CommonPersonObjectClient patient) {
        Intent intent = new Intent(getActivity(), FamilyOtherMemberProfileActivity.class);
        intent.putExtras(getArguments());
        intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        intent.putExtra(STRUCTURE_ID, structureId);
        startActivity(intent);
    }


    public void setStructure(String structureId) {
        this.structureId = structureId;
    }
}
