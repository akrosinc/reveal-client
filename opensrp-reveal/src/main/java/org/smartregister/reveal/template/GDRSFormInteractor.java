package org.smartregister.reveal.template;

import static org.smartregister.reveal.util.Utils.getOperationalAreaLocation;

import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.domain.Location;
import org.smartregister.domain.PhysicalLocation;
import org.smartregister.domain.Task;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.BaseContract;
import org.smartregister.reveal.interactor.BaseInteractor;
import org.smartregister.reveal.model.StructureDetails;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.GeoJsonUtils;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.RevealJsonFormUtils;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import timber.log.Timber;

/**
 * Thin {@link BaseInteractor} subclass used by {@link GDRSFormRecyclerActivity}
 * to save form JSON and handle "last event found" lookups.
 *
 * <p>Unlike {@link org.smartregister.reveal.test.GDRSInteractor} this class has
 * no dependency on {@link org.smartregister.reveal.test.GDRSActivity} — it receives
 * a {@link RevealJsonFormUtils} and a host context reference instead.
 */
public class GDRSFormInteractor extends BaseInteractor {

    private final String locationUUID;
    private final String compoundId;
    private final RevealJsonFormUtils formUtils;
    private final GDRSFormRecyclerActivity activity;

    /** Stub presenter — save callbacks are handled directly in the activity. */
    private static final BaseContract.BasePresenter STUB = new BaseContract.BasePresenter() {
        @Override public void onFormSaved(String sId, String tId, Task.TaskStatus s,
                String bs, String it) {}
        @Override public void onStructureAdded(Feature f, JSONArray c, double z) {}
        @Override public void onFormSaveFailure(String et) {
            Timber.tag("GDRSFormInteractor").e("Save failure: %s", et);
        }
        @Override public void onFamilyFound(
                org.smartregister.commonregistry.CommonPersonObjectClient c) {}
    };

    public GDRSFormInteractor(String locationUUID, String compoundId) {
        super(STUB);
        this.locationUUID = locationUUID;
        this.compoundId   = compoundId;
        this.formUtils    = new RevealJsonFormUtils();
        this.activity     = null; // not needed for saving
    }

    /**
     * Full constructor used when the interactor also needs to open pre-filled forms
     * (handleLasteventFound path).
     */
    public GDRSFormInteractor(GDRSFormRecyclerActivity activity,
                               String locationUUID,
                               String compoundId) {
        super(STUB);
        this.activity     = activity;
        this.locationUUID = locationUUID;
        this.compoundId   = compoundId;
        this.formUtils    = new RevealJsonFormUtils();
    }

    /**
     * Called when a sub-task was previously completed and the user taps it to re-open.
     * Finds the last saved event for the entity, pre-populates the correct form, and
     * opens it via {@link RevealJsonFormUtils#startJsonForm}.
     */
    @Override
    public void handleLasteventFound(org.smartregister.domain.Event event) {
        if (event == null || activity == null) return;

        String taskID = event.getDetails().get(Constants.Properties.TASK_IDENTIFIER);
        Task task = taskRepository.getTaskByIdentifier(taskID);
        if (task == null) return;

        org.smartregister.reveal.test.HdssLocation location =
                org.smartregister.reveal.test.GDRSActivity.getHdssLocation(locationUUID);
        org.smartregister.reveal.model.BaseTaskDetails details =
                org.smartregister.reveal.test.GDRSActivity
                        .getBaseTaskDetails(task, taskID, locationUUID);

        // Resolve form name from task code
        String formName;
        switch (task.getCode()) {
            case Constants.Action.SECONDARY_INDEX_CASE_MEMBER:
                formName = Constants.JsonForm.GDRS_SECONDARY_INDEX_CASE;
                break;
            case Constants.Action.INDEX_CASE_MEMBER:
                formName = Constants.JsonForm.GDRS_INDEX_CASE;
                break;
            default:
                formName = Constants.JsonForm.GDRS_RCD;
        }

        JSONObject formJSON = formUtils.getFormJSON(activity, formName, details, location);
        if (formJSON == null) return;

        // Populate geo feature collection (needed by geo widget if present)
        populateGeoFeatureCollection(formJSON);

        formUtils.populateForm(event, formJSON);

        JSONObject finalForm = formJSON;
        activity.runOnUiThread(() ->
                formUtils.startJsonForm(finalForm, activity,
                        Constants.RequestCode.REQUEST_CODE_GET_JSON));
    }

    /* ------------------------------------------------------------------ geo helper */

    private void populateGeoFeatureCollection(JSONObject formJSON) {
        try {
            JSONObject featureCollection = new JSONObject();
            featureCollection.put(Constants.GeoJSON.TYPE,
                    Constants.GeoJSON.FEATURE_COLLECTION);

            Location operationalAreaLocation =
                    getOperationalAreaLocation(
                            PreferencesUtil.getInstance().getCurrentOperationalArea());

            List<Location> structures =
                    RevealApplication.getInstance()
                            .getContext()
                            .getStructureRepository()
                            .getLocationsByParentIdForGdrs(
                                    operationalAreaLocation.getId(), "structure");

            Map<String, StructureDetails> detailsMap = structures.stream()
                    .map(s -> new AbstractMap.SimpleEntry<>(
                            s.getId(),
                            new StructureDetails(s.getProperties().getName(),
                                    s.getProperties().getName())))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));

            Map<String, Set<org.smartregister.domain.Task>> taskMap = new HashMap<>();
            for (Location structure : structures) {
                taskMap.put(structure.getId(),
                        taskRepository.getTasksByEntity(structure.getId()));
            }

            Map<String, Boolean> householdMap =
                    RevealApplication.getInstance()
                            .getHdssRepository()
                            .getIsAHouseholdByStructureList(
                                    structures.stream()
                                            .map(PhysicalLocation::getId)
                                            .collect(Collectors.toList()));

            String features = GeoJsonUtils.getGeoJsonFromStructuresAndTasksForGdrs(
                    structures, taskMap, null, detailsMap, null, householdMap);

            featureCollection.put(Constants.GeoJSON.FEATURES, new JSONArray(features));

            RevealApplication.getInstance()
                    .setFeatureCollection(
                            FeatureCollection.fromJson(featureCollection.toString()));
            RevealApplication.getInstance()
                    .setOperationalArea(Feature.fromJson(
                            gson.toJson(operationalAreaLocation)));

        } catch (Exception e) {
            Timber.tag("GDRSFormInteractor").e(e, "populateGeoFeatureCollection error");
            if (activity != null) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity,
                                "Cannot open geo widget", Toast.LENGTH_LONG).show());
            }
        }
    }
}
