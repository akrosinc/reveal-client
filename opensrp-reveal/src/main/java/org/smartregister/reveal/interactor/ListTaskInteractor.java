package org.smartregister.reveal.interactor;

import android.content.ContentValues;

import com.mapbox.geojson.Feature;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.repository.StructureRepository;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.ListTaskContract;
import org.smartregister.reveal.model.CardDetails;
import org.smartregister.reveal.model.FamilyCardDetails;
import org.smartregister.reveal.model.IRSVerificationCardDetails;
import org.smartregister.reveal.model.MosquitoHarvestCardDetails;
import org.smartregister.reveal.model.SprayCardDetails;
import org.smartregister.reveal.model.TaskDetails;
import org.smartregister.reveal.presenter.ListTaskPresenter;
import org.smartregister.reveal.util.CardDetailsUtil;
import org.smartregister.reveal.util.Constants.GeoJSON;
import org.smartregister.reveal.util.GeoJsonUtils;
import org.smartregister.reveal.util.IndicatorUtils;
import org.smartregister.reveal.util.InteractorUtils;
import org.smartregister.reveal.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.domain.LocationProperty.PropertyStatus.INACTIVE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_ELIGIBLE;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.AUTHORED_ON;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.BASE_ENTITY_ID;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.BUSINESS_STATUS;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.CARD_SPRAY;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.CHALK_SPRAY;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.CODE;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.ELIGIBLE_STRUCTURE;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.FOR;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.LAST_UPDATED_DATE;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.OWNER;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.PAOT_COMMENTS;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.PAOT_STATUS;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.PLAN_ID;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.REPORT_SPRAY;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.SPRAYED_STRUCTURES;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STATUS;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STICKER_SPRAY;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.TASK_TABLE;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.TRUE_STRUCTURE;
import static org.smartregister.reveal.util.Constants.Intervention.CASE_CONFIRMATION;
import static org.smartregister.reveal.util.Constants.Intervention.IRS;
import static org.smartregister.reveal.util.Constants.Intervention.IRS_VERIFICATION;
import static org.smartregister.reveal.util.Constants.Intervention.LARVAL_DIPPING;
import static org.smartregister.reveal.util.Constants.Intervention.MOSQUITO_COLLECTION;
import static org.smartregister.reveal.util.Constants.Intervention.PAOT;
import static org.smartregister.reveal.util.Constants.Intervention.REGISTER_FAMILY;
import static org.smartregister.reveal.util.Constants.Properties.TASK_CODE;
import static org.smartregister.reveal.util.Constants.Properties.TASK_IDENTIFIER;
import static org.smartregister.reveal.util.Constants.Tables.IRS_VERIFICATION_TABLE;
import static org.smartregister.reveal.util.Constants.Tables.LARVAL_DIPPINGS_TABLE;
import static org.smartregister.reveal.util.Constants.Tables.MOSQUITO_COLLECTIONS_TABLE;
import static org.smartregister.reveal.util.Constants.Tables.PAOT_TABLE;
import static org.smartregister.reveal.util.Utils.getInterventionLabel;
import static org.smartregister.reveal.util.Utils.getPropertyValue;

/**
 * Created by samuelgithengi on 11/27/18.
 */
public class ListTaskInteractor extends BaseInteractor {

    private CommonRepository commonRepository;
    private InteractorUtils interactorUtils;
    private StructureRepository structureRepository;
    private SQLiteDatabase writableDatabase;

    public ListTaskInteractor(ListTaskContract.Presenter presenter) {
        super(presenter);
        commonRepository = RevealApplication.getInstance().getContext().commonrepository(SPRAYED_STRUCTURES);
        interactorUtils = new InteractorUtils();
        structureRepository = RevealApplication.getInstance().getContext().getStructureRepository();
        writableDatabase = RevealApplication.getInstance().getRepository().getWritableDatabase();
    }

    public void fetchInterventionDetails(String interventionType, String featureId, boolean isForForm) {
        String sql = "SELECT status, start_date, end_date FROM %s WHERE id=?";
        if (IRS.equals(interventionType)) {
            sql = "SELECT spray_status, not_sprayed_reason, not_sprayed_other_reason, property_type, spray_date," +
                    " spray_operator, family_head_name FROM sprayed_structures WHERE id=?";
        } else if (MOSQUITO_COLLECTION.equals(interventionType)) {
            sql = String.format(sql, MOSQUITO_COLLECTIONS_TABLE);
        } else if (LARVAL_DIPPING.equals(interventionType)) {
            sql = String.format(sql, LARVAL_DIPPINGS_TABLE);
        } else if (PAOT.equals(interventionType)) {
            sql = String.format("SELECT %s, %s, %s  FROM %s WHERE %s=? ", PAOT_STATUS,
                    PAOT_COMMENTS, LAST_UPDATED_DATE, PAOT_TABLE, BASE_ENTITY_ID);
        } else if (IRS_VERIFICATION.equals(interventionType)) {
            sql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE id= ?",
                    TRUE_STRUCTURE, ELIGIBLE_STRUCTURE, REPORT_SPRAY, CHALK_SPRAY, STICKER_SPRAY, CARD_SPRAY, IRS_VERIFICATION_TABLE);
        } else if (REGISTER_FAMILY.equals(interventionType)) {
            sql = String.format("SELECT %s, %s, %s FROM %s WHERE %s = ?",
                    BUSINESS_STATUS, AUTHORED_ON, OWNER, TASK_TABLE, FOR);
        }

        final String SQL = sql;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getDatabase().rawQuery(SQL, new String[]{featureId});

                CardDetails cardDetails = null;
                try {
                    if (cursor.moveToFirst()) {
                        cardDetails = createCardDetails(cursor, interventionType);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                // run on ui thread
                final CardDetails CARD_DETAILS = cardDetails;
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isForForm) {
                            getSprayDetails(interventionType, featureId, CARD_DETAILS);
                            ((ListTaskPresenter) presenterCallBack).onInterventionFormDetailsFetched(CARD_DETAILS);
                        } else {
                            ((ListTaskPresenter) presenterCallBack).onCardDetailsFetched(CARD_DETAILS);
                        }
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void getSprayDetails(String interventionType, String structureId, CardDetails cardDetails) {
        if (!IRS.equals(interventionType))
            return;
        CommonPersonObject commonPersonObject = interactorUtils.fetchSprayDetails(interventionType, structureId,
                eventClientRepository, commonRepository);
        ((SprayCardDetails) cardDetails).setCommonPersonObject(commonPersonObject);
    }

    private CardDetails createCardDetails(Cursor cursor, String interventionType) {
        CardDetails cardDetails = null;
        if (MOSQUITO_COLLECTION.equals(interventionType) || LARVAL_DIPPING.equals(interventionType)) {
            cardDetails = createMosquitoHarvestCardDetails(cursor, interventionType);
        } else if (IRS.equals(interventionType)) {
            cardDetails = createSprayCardDetails(cursor);
        } else if (PAOT.equals(interventionType)) {
            cardDetails = createPaotCardDetails(cursor, interventionType);
        } else if (IRS_VERIFICATION.equals(interventionType)) {
            cardDetails = createIRSverificationCardDetails(cursor);
        } else if (REGISTER_FAMILY.equals(interventionType)) {
            cardDetails = createFamilyCardDetails(cursor);
        }

        return cardDetails;
    }

    private SprayCardDetails createSprayCardDetails(Cursor cursor) {
        String reason = cursor.getString(cursor.getColumnIndex("not_sprayed_reason"));
        if ("other".equals(reason)) {
            reason = cursor.getString(cursor.getColumnIndex("not_sprayed_other_reason"));
        }
        return new SprayCardDetails(
                CardDetailsUtil.getTranslatedBusinessStatus(cursor.getString(cursor.getColumnIndex("spray_status"))),
                cursor.getString(cursor.getColumnIndex("property_type")),
                cursor.getString(cursor.getColumnIndex("spray_date")),
                cursor.getString(cursor.getColumnIndex("spray_operator")),
                cursor.getString(cursor.getColumnIndex("family_head_name")),
                reason
        );
    }

    private MosquitoHarvestCardDetails createMosquitoHarvestCardDetails(Cursor cursor, String interventionType) {
        return new MosquitoHarvestCardDetails(
                CardDetailsUtil.getTranslatedBusinessStatus(cursor.getString(cursor.getColumnIndex("status"))),
                cursor.getString(cursor.getColumnIndex("start_date")),
                cursor.getString(cursor.getColumnIndex("end_date")),
                interventionType
        );
    }

    private MosquitoHarvestCardDetails createPaotCardDetails(Cursor cursor, String interventionType) {
        MosquitoHarvestCardDetails paotCardDetails = new MosquitoHarvestCardDetails(
                cursor.getString(cursor.getColumnIndex(PAOT_STATUS)),
                cursor.getString(cursor.getColumnIndex(LAST_UPDATED_DATE)),
                null,
                interventionType
        );
        paotCardDetails.setComments(cursor.getString(cursor.getColumnIndex(PAOT_COMMENTS)));
        return paotCardDetails;
    }

    private IRSVerificationCardDetails createIRSverificationCardDetails(Cursor cursor) {
        IRSVerificationCardDetails irsVerificationCardDetails = new IRSVerificationCardDetails(
                COMPLETE,
                cursor.getString(cursor.getColumnIndex(TRUE_STRUCTURE)),
                cursor.getString(cursor.getColumnIndex(ELIGIBLE_STRUCTURE)),
                cursor.getString(cursor.getColumnIndex(REPORT_SPRAY)),
                cursor.getString(cursor.getColumnIndex(CHALK_SPRAY)),
                cursor.getString(cursor.getColumnIndex(STICKER_SPRAY)),
                cursor.getString(cursor.getColumnIndex(CARD_SPRAY))
        );
        return irsVerificationCardDetails;
    }

    private FamilyCardDetails createFamilyCardDetails(Cursor cursor) {
        return new FamilyCardDetails(
                CardDetailsUtil.getTranslatedBusinessStatus(cursor.getString(cursor.getColumnIndex("business_status"))),
                cursor.getString(cursor.getColumnIndex("authored_on")),
                cursor.getString(cursor.getColumnIndex("owner"))

        );
    }

    public void fetchLocations(String plan, String operationalArea) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                JSONObject featureCollection = null;

                Location operationalAreaLocation = Utils.getOperationalAreaLocation(operationalArea);
                List<TaskDetails> taskDetailsList = null;

                try {
                    featureCollection = createFeatureCollection();
                    if (operationalAreaLocation != null) {
                        Map<String, Set<Task>> tasks = taskRepository.getTasksByPlanAndGroup(plan, operationalAreaLocation.getId());
                        List<Location> structures = structureRepository.getLocationsByParentId(operationalAreaLocation.getId());
                        taskDetailsList = IndicatorUtils.processTaskDetails(tasks);
                        String indexCase = null;
                        if (getInterventionLabel() == R.string.focus_investigation)
                            indexCase = getIndexCaseStructure(plan);
                        String features = GeoJsonUtils.getGeoJsonFromStructuresAndTasks(structures, tasks, indexCase);
                        featureCollection.put(GeoJSON.FEATURES, new JSONArray(features));

                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                JSONObject finalFeatureCollection = featureCollection;
                List<TaskDetails> finalTaskDetailsList = taskDetailsList;
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (operationalAreaLocation != null) {
                            operationalAreaId = operationalAreaLocation.getId();
                            Feature operationalAreaFeature = Feature.fromJson(gson.toJson(operationalAreaLocation));
                            getPresenter().onStructuresFetched(finalFeatureCollection, operationalAreaFeature, finalTaskDetailsList);
                        } else {
                            getPresenter().onStructuresFetched(finalFeatureCollection, null, null);
                        }
                    }
                });

            }

        };


        appExecutors.diskIO().execute(runnable);
    }

    private String getIndexCaseStructure(String planId) {
        Cursor cursor = null;
        String structureId = null;
        try {
            String query = getMemberTasksSelect(String.format("%s=? AND %s=?",
                    PLAN_ID, CODE), new String[]{});
            Timber.d(query);
            cursor = getDatabase().rawQuery(query, new String[]{planId, CASE_CONFIRMATION});
            if (cursor.moveToNext()) {
                structureId = cursor.getString(0);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return structureId;
    }

    private JSONObject createFeatureCollection() throws JSONException {
        JSONObject featureCollection = new JSONObject();
        featureCollection.put(GeoJSON.TYPE, GeoJSON.FEATURE_COLLECTION);
        return featureCollection;
    }

    private ListTaskContract.Presenter getPresenter() {
        return (ListTaskContract.Presenter) presenterCallBack;
    }

    public void markStructureAsInactive(Feature feature) {

        try {
            Location structure = structureRepository.getLocationById(feature.id());
            structure.getProperties().setStatus(INACTIVE);
            structureRepository.addOrUpdate(structure);

            ContentValues values = new ContentValues();
            values.put(STATUS, Task.TaskStatus.CANCELLED.name());
            writableDatabase.update(TASK_TABLE, values, FOR + " = ?",
                    new String[]{feature.id()} );
        } catch (Exception e) {
            Timber.e(e);
        }

        appExecutors.mainThread().execute(new Runnable() {
            @Override
            public void run() {
                    ((ListTaskPresenter) presenterCallBack).onStructureMarkedInactive();
            }
        });

    }

    public void markStructureAsIneligible(Feature feature) {

        String taskIdentifier = getPropertyValue(feature, TASK_IDENTIFIER);
        String code = getPropertyValue(feature, TASK_CODE);

        if (code.equals(REGISTER_FAMILY)) {
            Task task = taskRepository.getTaskByIdentifier(taskIdentifier);
            task.setBusinessStatus(NOT_ELIGIBLE);
            task.setStatus(Task.TaskStatus.COMPLETED);
            task.setLastModified(new DateTime());

            taskRepository.addOrUpdate(task);

        }

        appExecutors.mainThread().execute(new Runnable() {
            @Override
            public void run() {
                ((ListTaskPresenter) presenterCallBack).onStructureMarkedIneligible();
            }
        });
    }
}
