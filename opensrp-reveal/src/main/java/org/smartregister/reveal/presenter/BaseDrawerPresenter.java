package org.smartregister.reveal.presenter;

import static org.smartregister.reveal.util.Constants.Tags.CANTON;
import static org.smartregister.reveal.util.Constants.Tags.DISTRICT;
import static org.smartregister.reveal.util.Constants.Tags.HEALTH_CENTER;
import static org.smartregister.reveal.util.Constants.Tags.OPERATIONAL_AREA;
import static org.smartregister.reveal.util.Constants.Tags.SUB_DISTRICT;
import static org.smartregister.reveal.util.Constants.Tags.VILLAGE;
import static org.smartregister.reveal.util.Constants.UseContextCode.INTERVENTION_TYPE;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.smartregister.domain.Action;
import org.smartregister.domain.PlanDefinition;
import org.smartregister.domain.PlanDefinition.PlanStatus;
import org.smartregister.domain.form.FormLocation;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.BaseDrawerContract;
import org.smartregister.reveal.interactor.BaseDrawerInteractor;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.view.EventRegisterActivity;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.SyncUtils;
import org.smartregister.util.Utils;
import timber.log.Timber;

/**
 * Created by samuelgithengi on 3/21/19.
 */
public class BaseDrawerPresenter implements BaseDrawerContract.Presenter {

    private BaseDrawerContract.View view;

    private BaseDrawerContract.DrawerActivity drawerActivity;

    private PreferencesUtil prefsUtil;

    private LocationHelper locationHelper;

    private boolean changedCurrentSelection;

    private BaseDrawerContract.Interactor interactor;

    private static TextView syncLabel;

    private static TextView syncBadge;


    private boolean viewInitialized = false;

    private RevealApplication revealApplication;

    private AllSharedPreferences sharedPreferences;


    public BaseDrawerPresenter(BaseDrawerContract.View view, BaseDrawerContract.DrawerActivity drawerActivity) {
        this.view = view;
        this.drawerActivity = drawerActivity;
        this.prefsUtil = PreferencesUtil.getInstance();
        this.locationHelper = LocationHelper.getInstance();
        interactor = new BaseDrawerInteractor(this);
        revealApplication = RevealApplication.getInstance();
        sharedPreferences = revealApplication.getContext().allSharedPreferences();
    }


    private void initializeDrawerLayout() {

        view.setOperator();

        if (StringUtils.isBlank(prefsUtil.getCurrentOperationalArea())) {
            ArrayList<String> operationalAreaLevels = new ArrayList<>();
            operationalAreaLevels.add(DISTRICT);
            operationalAreaLevels.add(HEALTH_CENTER);
            operationalAreaLevels.add(VILLAGE);
            operationalAreaLevels.add(CANTON);
            operationalAreaLevels.add(SUB_DISTRICT);
            List<String> defaultLocation = locationHelper.generateDefaultLocationHierarchy(operationalAreaLevels);

            if (defaultLocation != null) {
                view.setDistrict(defaultLocation.get(0));
                prefsUtil.setCurrentDistrict(defaultLocation.get(0));
                ArrayList<String> levels = new ArrayList<>();
                levels.add(CANTON);
                String level;
                if (locationHelper.generateLocationHierarchyTree(false, levels).isEmpty()) {
                    level = HEALTH_CENTER;
                } else {
                    level = CANTON;
                }
                if (defaultLocation.size() > 1) {
                    view.setFacility(defaultLocation.get(1), level);
                }
            }
        } else {
            populateLocationsFromPreferences();
        }

        view.setPlan(prefsUtil.getCurrentPlan());

    }


    @Override
    public void onPlansFetched(Set<PlanDefinition> planDefinitions) {
        List<String> ids = new ArrayList<>();
        List<FormLocation> formLocations = new ArrayList<>();
        for (PlanDefinition planDefinition : planDefinitions) {
            if (!planDefinition.getStatus().equals(PlanStatus.ACTIVE)) {
                continue;
            }
            ids.add(planDefinition.getIdentifier());
            FormLocation formLocation = new FormLocation();
            formLocation.name = planDefinition.getTitle();
            formLocation.key = planDefinition.getIdentifier();
            formLocation.level = "";
            formLocations.add(formLocation);

            // get intervention type for plan
            for (PlanDefinition.UseContext useContext : planDefinition.getUseContext()) {
                if (useContext.getCode().equals(INTERVENTION_TYPE)) {
                    prefsUtil.setInterventionTypeForPlan(planDefinition.getIdentifier(),
                            useContext.getValueCodableConcept());
                    break;
                }
            }

            //get actions for plan
            List<String> actionCodes = new ArrayList<>();
            for (Action action : planDefinition.getActions()) {
                actionCodes.add(action.getCode());
            }
            prefsUtil.setActionCodesForPlan(planDefinition.getIdentifier(), actionCodes);

        }

        String entireTreeString = "";
        if (formLocations != null && !formLocations.isEmpty()) {
            entireTreeString = AssetHandler.javaToJsonString(formLocations,
                    new TypeToken<List<FormLocation>>() {
                    }.getType());
        }

        view.showPlanSelector(ids, entireTreeString);
    }

    private void populateLocationsFromPreferences() {
        view.setDistrict(prefsUtil.getCurrentDistrict());
        if (org.smartregister.reveal.util.Utils.isZambiaIRSLite()
                || org.smartregister.reveal.util.Utils.isKenyaMDALite()
                || org.smartregister.reveal.util.Utils.isRwandaMDALite()) {
            view.setFacility(prefsUtil.getCurrentDistrict(), "");
        } else {
            view.setFacility(prefsUtil.getCurrentFacility(), prefsUtil.getCurrentFacilityLevel());
        }
        view.setOperationalArea(prefsUtil.getCurrentOperationalArea());
    }

    @Override
    public void onShowOperationalAreaSelector() {
        if (StringUtils.isBlank(prefsUtil.getCurrentPlanId())) {
            view.displayNotification(R.string.campaign, R.string.plan_not_selected);
            return;
        }
        PlanDefinition currentPlan = revealApplication.getPlanDefinitionRepository()
                .findPlanDefinitionById(prefsUtil.getCurrentPlanId());
        String targetGeographicLevel = currentPlan.getTargetGeographicLevel();
        List<String> hierarchyGeographicLevels = currentPlan.getHierarchyGeographicLevels();
        Pair<String, ArrayList<String>> locationHierarchy = locationHelper.extractLocationHierarchy(hierarchyGeographicLevels,
                targetGeographicLevel);
        if (locationHierarchy == null) {//try to evict location hierachy in cache
            revealApplication.getContext().anmLocationController().evict();
            locationHierarchy = locationHelper.extractLocationHierarchy(hierarchyGeographicLevels, targetGeographicLevel);
        }
        if (locationHierarchy != null) {
            view.showOperationalAreaSelector(locationHierarchy);
        } else {
            view.displayNotification(R.string.error_fetching_location_hierarchy_title,
                    R.string.error_fetching_location_hierarchy);
            revealApplication.getContext().userService()
                    .forceRemoteLogin(revealApplication.getContext().allSharedPreferences().fetchRegisteredANM());
        }

    }
    public void onOperationalAreaSelectorClicked(ArrayList<String> name) {

        Timber.d("Selected Location Hierarchy: " + TextUtils.join(",", name));
        if (name.size() <= 2)//no operational area was selected, dialog was dismissed
        {
            return;
        }
        ArrayList<String> operationalAreaLevels = new ArrayList<>();
        operationalAreaLevels.add(DISTRICT);
        operationalAreaLevels.add(HEALTH_CENTER);
        operationalAreaLevels.add(SUB_DISTRICT);
        operationalAreaLevels.add(CANTON);
        operationalAreaLevels.add(OPERATIONAL_AREA);
        List<FormLocation> entireTree = locationHelper.generateLocationHierarchyTree(false, operationalAreaLevels);
        int districtOffset = name.get(0).equalsIgnoreCase(Country.BOTSWANA.name())
                || name.get(0).equalsIgnoreCase(Country.NAMIBIA.name())
                || name.get(0).toUpperCase().startsWith(Country.SENEGAL.name()) ? 3 : 2;
        if (name.get(0).toUpperCase().startsWith(Country.ZAMBIA.name()) && name.size() > 4) {
            districtOffset = name.size() - 2;
        }
        try {
            prefsUtil.setCurrentProvince(name.get(1));
            prefsUtil.setCurrentDistrict(name.get(name.size() - districtOffset));
            String operationalArea = name.get(name.size() - 1);
            prefsUtil.setCurrentOperationalArea(operationalArea);
            final String currentOperationalAreaId = prefsUtil.getCurrentOperationalAreaId();
            if (StringUtils.isNotBlank(currentOperationalAreaId)) {
                sharedPreferences.saveDefaultLocalityId(sharedPreferences.fetchRegisteredANM(),
                        currentOperationalAreaId);
            }
            if (PreferencesUtil.getInstance().getCurrentPlanTargetLevel().equals("structure")) {
                prefsUtil.setCurrentFacility(name.get(name.size() - 2));
            } else {
                prefsUtil.setCurrentFacility(name.get(name.size() - 1));
            }
        } catch (NullPointerException e) {
            Timber.tag("Reveal Exception").w(e);
        }
        changedCurrentSelection = true;
        populateLocationsFromPreferences();
        unlockDrawerLayout();

    }

    private Pair<String, String> getFacilityFromOperationalArea(String district, String operationalArea,
            List<FormLocation> entireTree) {
        for (FormLocation districtLocation : entireTree) {
            if (!districtLocation.name.equals(district)) {
                continue;
            }
            for (FormLocation facilityLocation : districtLocation.nodes) {
                if (facilityLocation.nodes == null) {
                    continue;
                }
                for (FormLocation operationalAreaLocation : facilityLocation.nodes) {
                    if (operationalAreaLocation.name.equals(operationalArea)) {
                        return new Pair<>(facilityLocation.level, facilityLocation.name);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void onShowPlanSelector() {
        //TODO: tell user that plans are still being downloaded ....or update existing messages
        interactor.fetchPlans();
    }


    public void onPlanSelectorClicked(ArrayList<String> value, ArrayList<String> name) {
        if (Utils.isEmptyCollection(name) || (name.size() > 1)) {
            return;
        }
        Timber.d("Selected Plan : " + TextUtils.join(",", name));
        Timber.d("Selected Plan Ids: " + TextUtils.join(",", value));

        prefsUtil.setCurrentPlan(name.get(0));
        prefsUtil.setCurrentPlanId(value.get(0));
        PlanDefinition planDefinition = revealApplication.getPlanDefinitionRepository()
                .findPlanDefinitionById(value.get(0));
        List<String> planGeographicLevels = planDefinition.getHierarchyGeographicLevels();
        prefsUtil.setCurrentPlanTargetLevel(planDefinition.getTargetGeographicLevel());
        if ("structure".equals(planDefinition.getTargetGeographicLevel())) {
            prefsUtil.setCurrentFacilityLevel(
                    planGeographicLevels.get(planGeographicLevels.indexOf("structure") - 2));
        }
        view.setPlan(name.get(0));
        view.setOperationalArea("");
        prefsUtil.setCurrentOperationalArea("");
        changedCurrentSelection = true;
        unlockDrawerLayout();

    }

    public void onDrawerClosed() {
        drawerActivity.onDrawerClosed();
    }

    private void unlockDrawerLayout() {
        if (isPlanAndOperationalAreaSelected()) {
            view.unlockNavigationDrawer();
        }

    }

    @Override
    public boolean isChangedCurrentSelection() {
        return changedCurrentSelection;
    }

    @Override
    public void setChangedCurrentSelection(boolean changedCurrentSelection) {
        this.changedCurrentSelection = changedCurrentSelection;
    }

    @Override
    public BaseDrawerContract.View getView() {
        return view;
    }

    @Override
    public void onViewResumed() {
        if (viewInitialized) {
            if ((StringUtils.isBlank(prefsUtil.getCurrentPlan()) || StringUtils.isBlank(
                    prefsUtil.getCurrentOperationalArea())) &&
                    (StringUtils.isNotBlank(view.getPlan()) || StringUtils.isNotBlank(view.getOperationalArea()))) {
                view.setOperationalArea(prefsUtil.getCurrentOperationalArea());
                view.setPlan(prefsUtil.getCurrentPlan());
                view.lockNavigationDrawerForSelection(R.string.select_campaign_operational_area_title,
                        R.string.revoked_plan_operational_area);
            } else if (!prefsUtil.getCurrentPlan().equals(view.getPlan())
                    || !prefsUtil.getCurrentOperationalArea().equals(view.getOperationalArea())) {
                changedCurrentSelection = true;
                onDrawerClosed();
            } else if (StringUtils.isBlank(prefsUtil.getCurrentPlan())) {
                view.lockNavigationDrawerForSelection(R.string.select_campaign_operational_area_title,
                        R.string.select_campaign_operational_area);
            }
        } else {
            initializeDrawerLayout();
            viewInitialized = true;
        }
        if (revealApplication.getSynced() && revealApplication.isRefreshMapOnEventSaved()) {
            view.checkSynced();
        } else {
            updateSyncStatusDisplay(revealApplication.getSynced());
        }

    }


    @Override
    public boolean isPlanAndOperationalAreaSelected() {
        String planId = prefsUtil.getCurrentPlanId();
        String operationalArea = prefsUtil.getCurrentOperationalArea();

        return StringUtils.isNotBlank(planId) && StringUtils.isNotBlank(operationalArea);

    }

    @Override
    public void onShowOfflineMaps() {
        getView().openOfflineMapsView();
    }

    /**
     * Updates the Hamburger menu of the navigation drawer to display the sync status of the application
     * Updates also the Text view next to the sync button with the sync status of the application
     *
     * @param synced Sync status of the application
     */
    @Override
    public void updateSyncStatusDisplay(boolean synced) {
        Activity activity = view.getContext();
        NavigationView navigationView = activity.findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        syncLabel = headerView.findViewById(R.id.sync_label);
        syncBadge = activity.findViewById(R.id.sync_badge);
        if (syncBadge != null && syncLabel != null) {
            if (synced && SyncUtils.getTotalSyncProgress() == 100) {
                syncBadge.setBackground(ContextCompat.getDrawable(activity, R.drawable.badge_green_oval));
                syncLabel.setText(getView().getContext().getString(R.string.device_data_synced));
                syncLabel.setTextColor(ContextCompat.getColor(activity, R.color.alert_complete_green));
                syncLabel.setBackground(ContextCompat.getDrawable(activity, R.drawable.rounded_border_alert_green));
            } else {
                syncBadge.setBackground(ContextCompat.getDrawable(activity, R.drawable.badge_oval));
                syncLabel.setText(getView().getContext().getString(R.string.device_data_not_synced));
                syncLabel.setTextColor(ContextCompat.getColor(activity, R.color.alert_urgent_red));
                syncLabel.setBackground(ContextCompat.getDrawable(activity, R.drawable.rounded_border_alert_red));
            }
        }
    }

    @Override
    public void onShowFilledForms() {
        if (drawerActivity.getActivity() != null) {
            drawerActivity.getActivity()
                    .startActivity(new Intent(drawerActivity.getActivity(), EventRegisterActivity.class));
        }
    }


}
