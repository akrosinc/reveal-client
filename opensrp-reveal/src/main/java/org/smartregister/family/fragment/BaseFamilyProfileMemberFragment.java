package org.smartregister.family.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.reveal.R;
import org.smartregister.family.activity.BaseFamilyProfileActivity;
import org.smartregister.family.contract.FamilyProfileMemberContract;
import org.smartregister.family.provider.FamilyMemberRegisterProvider;
import org.smartregister.family.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Set;

/**
 * Created by keyman on 23/11/2018.
 */
public abstract class BaseFamilyProfileMemberFragment extends BaseRegisterFragment implements FamilyProfileMemberContract.View {

    public static final String CLICK_VIEW_NORMAL = "click_view_normal";
    public static final String CLICK_VIEW_NEXT_ARROW = "click_next_arrow";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_member, container, false);
        rootView = view;//handle to the root

        setupViews(view);
        return view;
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns, String familyHead, String primaryCaregiver) {
        FamilyMemberRegisterProvider familyMemberRegisterProvider = new FamilyMemberRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler, familyHead, primaryCaregiver);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, familyMemberRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected String getMainCondition() {
        return presenter().getMainCondition();
    }

    @Override
    protected String getDefaultSortQuery() {
        return presenter().getDefaultSortQuery();
    }

    @Override
    protected void startRegistration() {
        (( BaseFamilyProfileActivity ) getActivity()).startFormActivity(Utils.metadata().familyMemberRegister.formName, null, (String)null);
    }

    @Override
    protected void onViewClicked(View view) {
        if (getActivity() == null) {
            return;
        }
    }

    @Override
    public void setUniqueID(String s) {
        if (getSearchView() != null) {
            getSearchView().setText(s);
        }
    }

    @Override
    public void showNotFoundPopup(String uniqueId) {
        if (getActivity() == null) {
            return;
        }
        NoMatchDialogFragment.launchDialog(( BaseRegisterActivity ) getActivity(), DIALOG_TAG, uniqueId);
    }

    @Override
    public void setFamilyHead(String familyHead) {
        presenter().setFamilyHead(familyHead);
    }

    @Override
    public void setPrimaryCaregiver(String primaryCaregiver) {
        presenter().setPrimaryCaregiver(primaryCaregiver);
    }

    @Override
    public FamilyProfileMemberContract.Presenter presenter() {
        return ( FamilyProfileMemberContract.Presenter) presenter;
    }
}
