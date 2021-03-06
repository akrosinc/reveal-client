package org.smartregister.reveal.contract;

import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;

import org.smartregister.domain.PlanDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public interface BaseDrawerContract {

    interface DrawerActivity {
        void onDrawerClosed();

        Activity getActivity();
    }

    interface View {

        Activity getContext();

        void initializeDrawerLayout();

        void setPlan(String campaign);

        void setOperationalArea(String operationalArea);

        String getPlan();

        String getOperationalArea();

        void setDistrict(String district);

        void setFacility(String facility, String facilityLevel);

        void setOperator();

        void unlockNavigationDrawer();

        void lockNavigationDrawerForSelection();

        void lockNavigationDrawerForSelection(int title, int message);

        void showOperationalAreaSelector(Pair<String, ArrayList<String>> locationHierarchy);

        void showPlanSelector(List<String> campaigns, String entireTreeString);

        void displayNotification(int title, @StringRes int message, Object... formatArgs);

        void openDrawerLayout();

        Presenter getPresenter();

        void onResume();

        void openOfflineMapsView();

        void checkSynced();

        void toggleProgressBarView(boolean syncing);

        @Nullable
        String getManifestVersion();
    }

    interface Presenter {

        void onDrawerClosed();

        void onShowOperationalAreaSelector();

        void onOperationalAreaSelectorClicked(ArrayList<String> name);

        void onShowPlanSelector();

        void onPlanSelectorClicked(ArrayList<String> value, ArrayList<String> name);

        void onPlansFetched(Set<PlanDefinition> planDefinitionSet);

        boolean isChangedCurrentSelection();

        void setChangedCurrentSelection(boolean changedCurrentSelection);

        View getView();

        void onViewResumed();

        void onShowOfflineMaps();

        boolean isPlanAndOperationalAreaSelected();


        void updateSyncStatusDisplay(boolean synced);

        void onShowFilledForms();
    }

    interface Interactor {

        void fetchPlans();

        void checkSynced();
    }
}
