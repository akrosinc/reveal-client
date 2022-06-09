package org.smartregister.family.domain;

import android.util.Pair;

import com.vijay.jsonwizard.activities.FormConfigurationJsonWizardFormActivity;
import com.vijay.jsonwizard.activities.JsonWizardFormActivity;

import org.jetbrains.annotations.Nullable;
import org.smartregister.view.activity.BaseProfileActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FamilyMetadata {

    public final Class familyFormActivity;
    public final Class familyMemberFormActivity;
    public final Class profileActivity;
    public final String uniqueIdentifierKey;
    public final boolean formWizardValidateRequiredFieldsBefore;

    public FamilyRegister familyRegister;
    public FamilyMemberRegister familyMemberRegister;
    public FamilyDueRegister familyDueRegister;
    public FamilyActivityRegister familyActivityRegister;
    public FamilyOtherMemberRegister familyOtherMemberRegister;
    @Nullable
    private Map<String, String> customConfigs;

    // contains the step name and the field name
    private List<Pair<String, String>> locationFields = new ArrayList<>();
    private String defaultLocation = "";
    private ArrayList<String> locationHierarchy = new ArrayList<>();

    public FamilyMetadata(Class<? extends JsonWizardFormActivity> familyFormActivity,
                          Class<? extends JsonWizardFormActivity> familyMemberFormActivity,
                          Class<? extends BaseProfileActivity> profileActivity,
                          String uniqueIdentifierKey,
                          boolean formWizardValidateRequiredFieldsBefore) {
        this.familyFormActivity = familyFormActivity;
        this.familyMemberFormActivity = familyMemberFormActivity;
        this.profileActivity = profileActivity;
        this.uniqueIdentifierKey = uniqueIdentifierKey;
        this.formWizardValidateRequiredFieldsBefore = formWizardValidateRequiredFieldsBefore;
    }

    public FamilyMetadata(Class<? extends FormConfigurationJsonWizardFormActivity> familyFormActivity,
                          Class<? extends FormConfigurationJsonWizardFormActivity> familyMemberFormActivity,
                          Class<? extends BaseProfileActivity> profileActivity,
                          boolean formWizardValidateRequiredFieldsBefore,
                          String uniqueIdentifierKey) {
        this.familyFormActivity = familyFormActivity;
        this.familyMemberFormActivity = familyMemberFormActivity;
        this.profileActivity = profileActivity;
        this.uniqueIdentifierKey = uniqueIdentifierKey;
        this.formWizardValidateRequiredFieldsBefore = formWizardValidateRequiredFieldsBefore;
    }

    public void updateFamilyRegister(String formName, String tableName, String registerEventType, String updateEventType, String config, String familyHeadRelationKey, String familyCareGiverRelationKey) {
        this.familyRegister = new FamilyRegister(formName, tableName, registerEventType, updateEventType, config, familyHeadRelationKey, familyCareGiverRelationKey);
    }

    public void updateFamilyMemberRegister(String formName, String tableName, String registerEventType, String updateEventType, String config, String familyRelationKey) {
        this.familyMemberRegister = new FamilyMemberRegister(formName, tableName, registerEventType, updateEventType, config, familyRelationKey);
    }

    public void updateFamilyDueRegister(String tableName, int currentLimit, boolean showPagination) {
        this.familyDueRegister = new FamilyDueRegister(tableName, currentLimit, showPagination);
    }

    public void updateFamilyActivityRegister(String tableName, int currentLimit, boolean showPagination) {
        this.familyActivityRegister = new FamilyActivityRegister(tableName, currentLimit, showPagination);
    }

    public void updateFamilyOtherMemberRegister(String tableName, int currentLimit, boolean showPagination) {
        this.familyOtherMemberRegister = new FamilyOtherMemberRegister(tableName, currentLimit, showPagination);
    }

    public static class FamilyRegister {

        public final String formName;

        public final String tableName;

        public final String registerEventType;

        public final String updateEventType;

        public final String config;

        public final String familyHeadRelationKey;

        public final String familyCareGiverRelationKey;


        public FamilyRegister(String formName, String tableName, String registerEventType, String updateEventType, String config, String familyHeadRelationKey, String familyCareGiverRelationKey) {
            this.formName = formName;
            this.tableName = tableName;
            this.registerEventType = registerEventType;
            this.updateEventType = updateEventType;
            this.config = config;
            this.familyHeadRelationKey = familyHeadRelationKey;
            this.familyCareGiverRelationKey = familyCareGiverRelationKey;
        }
    }

    public class FamilyMemberRegister {

        public final String formName;

        public final String tableName;

        public final String registerEventType;

        public final String updateEventType;

        public final String config;

        public final String familyRelationKey;


        public FamilyMemberRegister(String formName, String tableName, String registerEventType, String updateEventType, String config, String familyRelationKey) {
            this.formName = formName;
            this.tableName = tableName;
            this.registerEventType = registerEventType;
            this.updateEventType = updateEventType;
            this.config = config;
            this.familyRelationKey = familyRelationKey;
        }
    }

    public class FamilyDueRegister {

        public final String tableName;
        public final int currentLimit;
        public final boolean showPagination;

        public FamilyDueRegister(String tableName, int currentLimit, boolean showPagination) {
            this.tableName = tableName;
            if (currentLimit <= 0) {
                this.currentLimit = 20;
            } else {
                this.currentLimit = currentLimit;
            }
            this.showPagination = showPagination;
        }
    }

    public class FamilyActivityRegister {

        public final String tableName;
        public final int currentLimit;
        public final boolean showPagination;

        public FamilyActivityRegister(String tableName, int currentLimit, boolean showPagination) {
            this.tableName = tableName;
            if (currentLimit <= 0) {
                this.currentLimit = 20;
            } else {
                this.currentLimit = currentLimit;
            }
            this.showPagination = showPagination;
        }
    }

    public class FamilyOtherMemberRegister {

        public final String tableName;
        public final int currentLimit;
        public final boolean showPagination;

        public FamilyOtherMemberRegister(String tableName, int currentLimit, boolean showPagination) {
            this.tableName = tableName;
            if (currentLimit <= 0) {
                this.currentLimit = 20;
            } else {
                this.currentLimit = currentLimit;
            }
            this.showPagination = showPagination;
        }
    }

    public List<Pair<String, String>> getLocationFields() {
        return locationFields;
    }

    public void setLocationFields(List<Pair<String, String>> locationFields) {
        this.locationFields = locationFields;
    }

    public String getDefaultLocation() {
        return defaultLocation;
    }

    public void setDefaultLocation(String defaultLocation) {
        this.defaultLocation = defaultLocation;
    }

    public ArrayList<String> getLocationHierarchy() {
        return locationHierarchy;
    }

    public void setLocationHierarchy(ArrayList<String> locationHierarchy) {
        this.locationHierarchy = locationHierarchy;
    }

    @Nullable
    public Map<String, String> getCustomConfigs() {
        return customConfigs == null ? null : Collections.unmodifiableMap(customConfigs);
    }

    public void setCustomConfigs(@Nullable Map<String, String> customConfigs) {
        this.customConfigs = customConfigs;
    }
}
