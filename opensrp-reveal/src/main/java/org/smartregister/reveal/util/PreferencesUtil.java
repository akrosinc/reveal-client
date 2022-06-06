package org.smartregister.reveal.util;

import static org.smartregister.reveal.util.Constants.ACTIONS;
import static org.smartregister.reveal.util.Constants.Preferences.CURRENT_DISTRICT;
import static org.smartregister.reveal.util.Constants.Preferences.CURRENT_FACILITY;
import static org.smartregister.reveal.util.Constants.Preferences.CURRENT_OPERATIONAL_AREA;
import static org.smartregister.reveal.util.Constants.Preferences.CURRENT_OPERATIONAL_AREA_ID;
import static org.smartregister.reveal.util.Constants.Preferences.CURRENT_PLAN;
import static org.smartregister.reveal.util.Constants.Preferences.CURRENT_PLAN_ID;
import static org.smartregister.reveal.util.Constants.Preferences.CURRENT_PROVINCE;
import static org.smartregister.reveal.util.Constants.Preferences.FACILITY_LEVEL;
import static org.smartregister.reveal.util.Constants.Preferences.TOTAL_SYNC_PROGRESS;
import static org.smartregister.reveal.util.Constants.TILDE;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.smartregister.account.AccountHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.reveal.application.RevealApplication;

/**
 * Created by samuelgithengi on 11/29/18.
 */
public class PreferencesUtil {

    public static final String ALL_TASKS_SYNCED = "ALL_TASKS_SYNCED";

    public static final String ALL_EVENTS_SYNCED = "ALL_EVENTS_SYNCED";

    public static final String ALL_PLANS_SYNCED = "ALL_PLANS_SYNCED";

    public static final String ALL_LOCATIONS_SYNCED = "ALL_LOCATIONS_SYNCED";

    private AllSharedPreferences allSharedPreferences;

    private static PreferencesUtil instance;

    private PreferencesUtil(AllSharedPreferences allSharedPreferences) {
        this.allSharedPreferences = allSharedPreferences;
    }


    public static PreferencesUtil getInstance() {
        if (instance == null) {
            instance = new PreferencesUtil(RevealApplication.getInstance().getContext().allSharedPreferences());
        }
        return instance;
    }

    public void setCurrentFacility(String facility) {
        allSharedPreferences.savePreference(CURRENT_FACILITY, facility);
    }

    public String getCurrentFacility() {
        return allSharedPreferences.getPreference(CURRENT_FACILITY);
    }

    public void setCurrentOperationalArea(String operationalArea) {
        allSharedPreferences.savePreference(CURRENT_OPERATIONAL_AREA, operationalArea);
        if (StringUtils.isNotBlank(operationalArea)) {
            allSharedPreferences.savePreference(CURRENT_OPERATIONAL_AREA_ID, Utils.getCurrentLocationId());
        } else {
            allSharedPreferences.savePreference(CURRENT_OPERATIONAL_AREA_ID, null);
        }
    }

    public String getCurrentOperationalArea() {
        return allSharedPreferences.getPreference(CURRENT_OPERATIONAL_AREA);
    }

    public String getCurrentOperationalAreaId() {
        return allSharedPreferences.getPreference(CURRENT_OPERATIONAL_AREA_ID);
    }

    public void setCurrentDistrict(String district) {
        allSharedPreferences.savePreference(CURRENT_DISTRICT, district);
    }

    public String getCurrentDistrict() {
        return allSharedPreferences.getPreference(CURRENT_DISTRICT);
    }

    public void setCurrentProvince(String province) {
        allSharedPreferences.savePreference(CURRENT_PROVINCE, province);
    }

    public String getCurrentProvince() {
        return allSharedPreferences.getPreference(CURRENT_PROVINCE);
    }

    public void setCurrentPlan(String campaign) {
        allSharedPreferences.savePreference(CURRENT_PLAN, campaign);
    }

    public String getCurrentPlan() {
        return allSharedPreferences.getPreference(CURRENT_PLAN);
    }

    public void setCurrentPlanId(String campaignId) {
        allSharedPreferences.savePreference(CURRENT_PLAN_ID, campaignId);
    }

    public String getCurrentPlanId() {
        return allSharedPreferences.getPreference(CURRENT_PLAN_ID);
    }

    public String getPreferenceValue(String key) {
        return allSharedPreferences.getPreference(key);
    }

    public void setCurrentFacilityLevel(String facilityLevel) {
        allSharedPreferences.savePreference(FACILITY_LEVEL, facilityLevel);
    }


    public String getCurrentFacilityLevel() {
        return allSharedPreferences.getPreference(FACILITY_LEVEL);
    }

    public void setInterventionTypeForPlan(String planId, String interventionType) {
        allSharedPreferences.savePreference(planId, interventionType);
    }

    public String getInterventionTypeForPlan(String planId) {
        return allSharedPreferences.getPreference(planId);
    }

    public boolean isKeycloakConfigured() {
        return allSharedPreferences.getPreferences().getBoolean(AccountHelper.CONFIGURATION_CONSTANTS.IS_KEYCLOAK_CONFIGURED, false);
    }

    public void setActionCodesForPlan(String planId, List<String> actionCodes) {
        String actionCodesJsonString = new Gson().toJson(actionCodes);
        allSharedPreferences.savePreference(planId.concat(TILDE).concat(ACTIONS), actionCodesJsonString);
    }

    public List<String> getActionCodesForPlan(String planId) {
        List<String> actionCodes;
        String actionCodesJsonString = allSharedPreferences.getPreference(planId.concat(TILDE).concat(ACTIONS));
        Type type = new TypeToken<List<String>>() {
        }.getType();
        actionCodes = new Gson().fromJson(actionCodesJsonString, type);
        return actionCodes;
    }

    public void setCurrentTotalSyncProgress(String progress){
        allSharedPreferences.savePreference(TOTAL_SYNC_PROGRESS,progress);
    }

    public String getCurrentTotalSyncProgress(){
        return allSharedPreferences.getPreference(TOTAL_SYNC_PROGRESS);
    }

    public void setAllTasksSynced(boolean isAllTasksSynced){
       allSharedPreferences.saveBooleanPreference(ALL_TASKS_SYNCED,isAllTasksSynced);
    }

    public boolean isAllTasksSynced(){
        return allSharedPreferences.getBooleanPreference(ALL_TASKS_SYNCED);
    }

    public void setAllEventsSynced(boolean isAllEventsSynced){
        allSharedPreferences.saveBooleanPreference(ALL_EVENTS_SYNCED,isAllEventsSynced);
    }

    public boolean isAllEventsSynced(){
        return allSharedPreferences.getBooleanPreference(ALL_EVENTS_SYNCED);
    }

    public void setAllPlansSynced(boolean isAllPlansSynced){
        allSharedPreferences.saveBooleanPreference(ALL_PLANS_SYNCED,isAllPlansSynced);
    }

    public boolean isAllPlansSynced(){
        return allSharedPreferences.getBooleanPreference(ALL_PLANS_SYNCED);
    }

    public void setAllLocationsSynced(boolean isAllLocationsSynced){
        allSharedPreferences.saveBooleanPreference(ALL_LOCATIONS_SYNCED,isAllLocationsSynced);
    }

    public boolean isAllLocationsSynced(){
        return allSharedPreferences.getBooleanPreference(ALL_LOCATIONS_SYNCED);
    }
}
