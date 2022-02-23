package org.smartregister.family.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import org.smartregister.reveal.R;
import org.smartregister.family.activity.BaseFamilyProfileActivity;
import org.smartregister.family.adapter.FamilyRecyclerViewCustomAdapter;
import org.smartregister.family.contract.FamilyProfileActivityContract;
import org.smartregister.family.provider.FamilyActivityRegisterProvider;
import org.smartregister.family.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Set;

/**
 * Created by ndegwamartin on 12/07/2018.
 */
public abstract class BaseFamilyProfileActivityFragment extends BaseRegisterFragment implements FamilyProfileActivityContract.View {

    public static final String CLICK_VIEW_NORMAL = "click_view_normal";
    public static final String CLICK_VIEW_NEXT_ARROW = "click_next_arrow";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_activity, container, false);
        rootView = view;//handle to the root

        setupViews(view);
        return view;
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        FamilyActivityRegisterProvider familyActivityRegisterProvider = new FamilyActivityRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new FamilyRecyclerViewCustomAdapter(null, familyActivityRegisterProvider, context().commonrepository(this.tablename), Utils.metadata().familyActivityRegister.showPagination);
        clientAdapter.setCurrentlimit(Utils.metadata().familyActivityRegister.currentLimit);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void onViewClicked(View view) {
        if (getActivity() == null) {
            return;
        }
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
    public FamilyProfileActivityContract.Presenter presenter() {
        return ( FamilyProfileActivityContract.Presenter) presenter;
    }
}
