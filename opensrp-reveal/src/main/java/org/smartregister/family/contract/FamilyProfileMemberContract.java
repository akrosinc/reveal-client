package org.smartregister.family.contract;

import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.view.contract.BaseRegisterFragmentContract;

import java.util.Set;

public interface FamilyProfileMemberContract {

    interface View extends BaseRegisterFragmentContract.View {

        void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns, String familyHead, String primaryCaregiver);

        Presenter presenter();

        void setFamilyHead(String familyHead);

        void setPrimaryCaregiver(String primaryCaregiver);
    }

    interface Presenter extends BaseRegisterFragmentContract.Presenter {

        String getMainCondition();

        String getDefaultSortQuery();

        void setFamilyHead(String familyHead);

        void setPrimaryCaregiver(String primaryCaregiver);

        String getQueryTable();

    }

    interface Model {

        RegisterConfiguration defaultRegisterConfiguration();

        ViewConfiguration getViewConfiguration(String viewConfigurationIdentifier);

        Set<org.smartregister.configurableviews.model.View> getRegisterActiveColumns(String viewConfigurationIdentifier);

        String countSelect(String tableName, String mainCondition);

        String mainSelect(String tableName, String mainCondition);

    }

}