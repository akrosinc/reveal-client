package org.smartregister.reveal.interactor;

import android.util.Log;

import com.mapbox.geojson.Feature;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Constants.INTENT_KEY;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.ListTaskContract;
import org.smartregister.reveal.model.MosquitoCollectionCardDetails;
import org.smartregister.reveal.model.SprayCardDetails;
import org.smartregister.reveal.util.Constants.DatabaseKeys;
import org.smartregister.reveal.util.Constants.GeoJSON;
import org.smartregister.reveal.util.FamilyConstants;
import org.smartregister.reveal.util.FamilyConstants.TABLE_NAME;
import org.smartregister.reveal.util.GeoJsonUtils;
import org.smartregister.reveal.util.Utils;

import java.util.List;
import java.util.Map;


/**
 * Created by samuelgithengi on 11/27/18.
 */
public class ListTaskInteractor extends BaseInteractor {

    private static final String TAG = ListTaskInteractor.class.getCanonicalName();


    private SQLiteDatabase database;

    public ListTaskInteractor(ListTaskContract.Presenter presenter) {
        super(presenter);
        database = RevealApplication.getInstance().getRepository().getReadableDatabase();
    }


    public void fetchSprayDetails(String structureId, boolean isForSprayForm) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final String sql = "SELECT spray_status, not_sprayed_reason, not_sprayed_other_reason, property_type, spray_date," +
                        " spray_operator, family_head_name FROM sprayed_structures WHERE id=?";
                Cursor cursor = database.rawQuery(sql, new String[]{structureId});
                SprayCardDetails sprayCardDetails = null;
                try {
                    if (cursor.moveToFirst()) {
                        sprayCardDetails = createSprayCardDetails(cursor);
                    }
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                // run on ui thread
                SprayCardDetails finalSprayCardDetails = sprayCardDetails;
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isForSprayForm) {
                            getPresenter().onInterventionFormDetailsFetched(finalSprayCardDetails);
                        } else {
                            getPresenter().onCardDetailsFetched(finalSprayCardDetails);
                        }
                    }
                });
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    public void fetchMosquitoCollectionDetails(String mosquitoCollectionPointId, boolean isForMosquitoCollectionForm) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final String sql = "SELECT status, start_date, end_date FROM mosquito_interventions WHERE id=?";
                Cursor cursor = database.rawQuery(sql, new String[]{mosquitoCollectionPointId});

                MosquitoCollectionCardDetails mosquitoCollectionCardDetails = null;
                try {
                    if (cursor.moveToFirst()) {
                        mosquitoCollectionCardDetails = createMosquitoCollectionCardDetails(cursor);
                    }
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                // run on ui thread
                MosquitoCollectionCardDetails finalMosquitoCollectionCardDetails = mosquitoCollectionCardDetails;
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {

                        if (isForMosquitoCollectionForm) {
                            getPresenter().onInterventionFormDetailsFetched(finalMosquitoCollectionCardDetails);
                        } else {
                            getPresenter().onCardDetailsFetched(finalMosquitoCollectionCardDetails);
                        }
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    private SprayCardDetails createSprayCardDetails(Cursor cursor) {
        String reason = cursor.getString(cursor.getColumnIndex("not_sprayed_reason"));
        if ("other".equals(reason)) {
            reason = cursor.getString(cursor.getColumnIndex("not_sprayed_other_reason"));
        }
        return new SprayCardDetails(
                cursor.getString(cursor.getColumnIndex("spray_status")),
                cursor.getString(cursor.getColumnIndex("property_type")),
                cursor.getString(cursor.getColumnIndex("spray_date")),
                cursor.getString(cursor.getColumnIndex("spray_operator")),
                cursor.getString(cursor.getColumnIndex("family_head_name")),
                reason
        );
    }


    private MosquitoCollectionCardDetails createMosquitoCollectionCardDetails(Cursor cursor) {
        return new MosquitoCollectionCardDetails(
                cursor.getString(cursor.getColumnIndex("status")),
                cursor.getString(cursor.getColumnIndex("start_date")),
                cursor.getString(cursor.getColumnIndex("end_date"))
        );
    }

    public void fetchLocations(String campaign, String operationalArea) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final JSONObject featureCollection = createFeatureCollection();
                Location operationalAreaLocation = Utils.getOperationalAreaLocation(operationalArea);
                try {
                    if (operationalAreaLocation != null) {
                        Map<String, Task> tasks = taskRepository.getTasksByCampaignAndGroup(campaign, operationalAreaLocation.getId());
                        List<Location> structures = structureRepository.getLocationsByParentId(operationalAreaLocation.getId());
                        featureCollection.put(GeoJSON.FEATURES, new JSONArray(GeoJsonUtils.getGeoJsonFromStructuresAndTasks(structures, tasks)));
                        Log.d(TAG, "features:" + featureCollection.toString());

                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (operationalAreaLocation != null) {
                            operationalAreaId = operationalAreaLocation.getId();
                            Feature operationalAreaFeature = Feature.fromJson(gson.toJson(operationalAreaLocation));
                            getPresenter().onStructuresFetched(featureCollection, operationalAreaFeature);
                        } else {
                            getPresenter().onStructuresFetched(featureCollection, null);
                        }
                    }
                });
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    private JSONObject createFeatureCollection() {
        JSONObject featureCollection = new JSONObject();
        try {
            featureCollection.put(GeoJSON.TYPE, GeoJSON.FEATURE_COLLECTION);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating feature collection");
            return null;
        }
        return featureCollection;
    }

    private ListTaskContract.Presenter getPresenter() {
        return (ListTaskContract.Presenter) presenterCallBack;
    }


    public void fetchFamilyDetails(String structureId) {
        appExecutors.diskIO().execute(() -> {
            Cursor cursor = null;
            CommonPersonObjectClient family=null;
            try {
                cursor = database.rawQuery(String.format("SELECT %s FROM %S WHERE %s = ?",
                        INTENT_KEY.BASE_ENTITY_ID, TABLE_NAME.FAMILY, DatabaseKeys.STRUCTURE_ID), new String[]{structureId});
                if (cursor.moveToNext()) {
                    String baseEntityId = cursor.getString(0);
                    final CommonPersonObject personObject = org.smartregister.family.util.Utils.context().commonrepository(org.smartregister.family.util.Utils.metadata().familyRegister.tableName).findByBaseEntityId(baseEntityId);
                    family = new CommonPersonObjectClient(personObject.getCaseId(),
                            personObject.getDetails(), "");
                    family.setColumnmaps(personObject.getColumnmaps());
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                if (cursor != null)
                    cursor.close();
            }

            CommonPersonObjectClient finalFamily = family;
            appExecutors.mainThread().execute(() -> {
                getPresenter().onFamilyFound(finalFamily);
            });
        });
    }

}
