package org.smartregister.reveal.interactor;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;

import net.sqlcipher.Cursor;
import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.Event;
import org.smartregister.domain.Obs;
import org.smartregister.domain.Task;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.EventClientRepository.event_column;
import org.smartregister.repository.StructureRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.StructureTasksContract;
import org.smartregister.reveal.model.EventTask;
import org.smartregister.reveal.model.FamilySummaryModel;
import org.smartregister.reveal.model.StructureTaskDetails;
import org.smartregister.reveal.util.AppExecutors;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.Constants.Intervention;
import org.smartregister.reveal.util.InteractorUtils;
import org.smartregister.reveal.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

import static org.smartregister.domain.Task.INACTIVE_TASK_STATUS;
import static org.smartregister.domain.Task.TaskStatus.READY;
import static org.smartregister.family.util.DBConstants.KEY.DOB;
import static org.smartregister.family.util.DBConstants.KEY.FIRST_NAME;
import static org.smartregister.family.util.DBConstants.KEY.LAST_NAME;
import static org.smartregister.family.util.DBConstants.KEY.MIDDLE_NAME;
import static org.smartregister.reveal.util.Constants.BLOOD_SCREENING_EVENT;
import static org.smartregister.reveal.util.Constants.BusinessStatus.SMC_COMPLETE;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.ADMINISTERED_SPAQ;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.BUSINESS_STATUS;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.CODE;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.FOR;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.GROUPID;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.ID_;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.NAME;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.NUMBER_OF_ADDITIONAL_DOSES;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.PLAN_ID;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STATUS;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STRUCTURES_TABLE;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STRUCTURE_ID;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.TASK_TABLE;
import static org.smartregister.reveal.util.FamilyConstants.TABLE_NAME.FAMILY_MEMBER;


/**
 * Created by samuelgithengi on 4/12/19.
 */
public class StructureTasksInteractor extends BaseInteractor implements StructureTasksContract.Interactor {

    private AppExecutors appExecutors;

    private SQLiteDatabase database;
    private StructureTasksContract.Presenter presenter;
    private StructureRepository structureRepository;
    private InteractorUtils interactorUtils;

    public StructureTasksInteractor(StructureTasksContract.Presenter presenter) {
        this(presenter, RevealApplication.getInstance().getAppExecutors(), RevealApplication.getInstance().getRepository().getReadableDatabase(), RevealApplication.getInstance().getStructureRepository());
    }

    @VisibleForTesting
    protected StructureTasksInteractor(StructureTasksContract.Presenter presenter, AppExecutors appExecutors,
                                       SQLiteDatabase database, StructureRepository structureRepository) {
        super(presenter);
        this.presenter = presenter;
        this.appExecutors = appExecutors;
        this.database = database;
        this.structureRepository = structureRepository;
        this.interactorUtils = new InteractorUtils(RevealApplication.getInstance().getTaskRepository(), eventClientRepository, clientProcessor);
    }

    @Override
    public void findTasks(String structureId, String planId, String operationalAreaId) {
        appExecutors.diskIO().execute(() -> {
            List<StructureTaskDetails> taskDetailsList = new ArrayList<>();
            StructureTaskDetails incompleteIndexCase = null;
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(getTaskSelect(String.format(
                        "%s=? AND %s=? AND %s NOT IN (%s)", FOR, PLAN_ID, STATUS,
                        TextUtils.join(",", Collections.nCopies(INACTIVE_TASK_STATUS.length, "?")))),
                        ArrayUtils.addAll(new String[]{structureId, planId,}, INACTIVE_TASK_STATUS));
                while (cursor.moveToNext()) {
                    taskDetailsList.add(readTaskDetails(cursor));
                }

                cursor.close();
                cursor = database.rawQuery(getMemberTasksSelect(String.format("%s.%s=? AND %s=? AND %s IS NULL AND %s NOT IN (%s)",
                        STRUCTURES_TABLE, ID_, PLAN_ID, DBConstants.KEY.DATE_REMOVED, STATUS,
                        TextUtils.join(",", Collections.nCopies(INACTIVE_TASK_STATUS.length, "?"))), getMemberColumns()),
                        ArrayUtils.addAll(new String[]{structureId, planId}, INACTIVE_TASK_STATUS));
                while (cursor.moveToNext()) {
                    taskDetailsList.add(readMemberTaskDetails(cursor));
                }
                cursor.close();
                cursor = database.rawQuery(getTaskSelect(String.format("%s = ? AND %s = ? AND %s = ? AND %s = ?",
                        GROUPID, PLAN_ID, CODE, STATUS)),
                        new String[]{operationalAreaId, planId, Intervention.CASE_CONFIRMATION, READY.name()});
                if (cursor.moveToNext()) {
                    incompleteIndexCase = readTaskDetails(cursor);
                }
            } catch (Exception e) {
                Timber.e(e, "Error querying tasks for " + structureId);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            StructureTaskDetails finalIndexCase = incompleteIndexCase;
            populateEventsPerTask(taskDetailsList);
            appExecutors.mainThread().execute(() -> {
                presenter.onTasksFound(taskDetailsList, finalIndexCase);
            });
        });
    }

    @Override
    public void getStructure(StructureTaskDetails details) {
        appExecutors.diskIO().execute(() -> {

            String structureId = details.getTaskEntity();
            if (Intervention.PERSON_INTERVENTIONS.contains(details.getTaskCode())) {
                structureId = details.getStructureId();
            }
            org.smartregister.domain.Location structure =
                    structureRepository.getLocationById(structureId);
            appExecutors.mainThread().execute(() -> {
                presenter.onStructureFound(structure, details);
            });
        });
    }

    @Override
    public void findLastEvent(StructureTaskDetails taskDetails) {

        appExecutors.diskIO().execute(() -> {
            // Heads up
            Event event = getLastEvent(taskDetails);

            if (event != null) {
                presenter.onEventFound(event);
            }
        });

    }

    @Override
    public void findTotalSMCDosageCounts(StructureTaskDetails taskDetails, JSONObject formJSON) {

        String totalAdministeredSpaqQuery = String.format("SELECT count(%s) FROM %s WHERE %s = ? AND %s = ?",
                ADMINISTERED_SPAQ, FAMILY_MEMBER, STRUCTURE_ID, ADMINISTERED_SPAQ);

        String totalNumberOfAdditionalDosesQuery = String.format("SELECT count(%s) FROM %s WHERE %s = ? AND %s = ?",
                NUMBER_OF_ADDITIONAL_DOSES, FAMILY_MEMBER, STRUCTURE_ID, NUMBER_OF_ADDITIONAL_DOSES, 1);

        appExecutors.diskIO().execute(() -> {
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(totalAdministeredSpaqQuery, new String[]{taskDetails.getStructureId(), "Yes"});
                if (cursor.moveToFirst()) {
                    int totalAdministeredSpaqCount = cursor.getInt(0);
                    taskDetails.setTotalAdministeredSpaq(totalAdministeredSpaqCount);
                }
                cursor.close();

                cursor = database.rawQuery(totalNumberOfAdditionalDosesQuery, new String[]{taskDetails.getStructureId(), "1"});
                if (cursor.moveToFirst()) {
                    int totalNumberOfAdditionalDoses = cursor.getInt(0);
                    taskDetails.setTotalNumberOfAdditionalDoses(totalNumberOfAdditionalDoses);
                }
                cursor.close();

                appExecutors.mainThread().execute(() -> {
                    presenter.onTotalSMCDosageCountsFound(taskDetails, formJSON);
                });

            } catch (SQLException e) {
                Timber.e(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }

    private Event getLastEvent(StructureTaskDetails taskDetails) {
        String eventType = taskDetails.getTaskCode().equals(Intervention.BLOOD_SCREENING) ? BLOOD_SCREENING_EVENT : Constants.BEDNET_DISTRIBUTION_EVENT;

        if (taskDetails.getTaskCode().equals(Intervention.MDA_DISPENSE)) {
            eventType = Constants.EventType.MDA_DISPENSE;
        } else if (taskDetails.getTaskCode().equals(Intervention.MDA_ADHERENCE)) {
            eventType = Constants.EventType.MDA_ADHERENCE;
        } else if (taskDetails.getTaskCode().equals(Intervention.MDA_DRUG_RECON)){
            eventType = Constants.EventType.MDA_DRUG_RECON;
        }

        String events = String.format("select %s from %s where %s = ? and %s =? order by %s desc limit 1",
                event_column.json, EventClientRepository.Table.event.name(), event_column.baseEntityId, event_column.eventType, event_column.updatedAt);
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(events, new String[]{taskDetails.getTaskEntity(), eventType});
            if (cursor.moveToFirst()) {
                String eventJSON = cursor.getString(0);
                return eventClientRepository.convert(eventJSON, Event.class);
            }
        } catch (SQLException e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    @Override
    public void resetTaskInfo(Context context, StructureTaskDetails taskDetails) {
        appExecutors.diskIO().execute(() -> {
            interactorUtils.resetTaskInfo(getDatabase(), taskDetails);

            appExecutors.mainThread().execute(() -> {
                presenter.onTaskInfoReset(taskDetails.getStructureId());
            });
        });
    }

    @Override
    public void findCompletedDispenseTasks(StructureTaskDetails taskDetails, List<StructureTaskDetails> taskDetailsList) {
        // HEADS UP
        appExecutors.diskIO().execute(() -> {

            final FamilySummaryModel summary = new FamilySummaryModel();

            // Get the number of SMC_COMPLETE tasks for the structure
            try (Cursor cursor = database.rawQuery(
                    String.format("SELECT count(*) FROM %s WHERE %s = ? AND %s =? AND %s=?",
                            TASK_TABLE, STRUCTURE_ID, PLAN_ID, BUSINESS_STATUS),
                    new String[]{taskDetails.getStructureId(), PreferencesUtil.getInstance().getCurrentPlanId(), SMC_COMPLETE})) {

                while (cursor.moveToNext()) {
                    summary.setChildrenTreated(cursor.getInt(0));
                }
            } catch (Exception e) {
                Timber.e(e, "Error find Number of members ");
            }

            // Get the total number of doses administered
            for (StructureTaskDetails taskDetail : taskDetailsList) {
                if (taskDetail.getTaskCode().equals(Intervention.MDA_ADHERENCE) && taskDetail.getTaskStatus().equals(Task.TaskStatus.COMPLETED.name())) {
                    Event event = this.getLastEvent(taskDetail);

                    if (event != null) {
                        Obs obs = event.findObs(null, false, Constants.JsonForm.NUMBER_OF_ADDITIONAL_DOSES);
                        if (obs != null && obs.getValues() != null) {
                            Object value = obs.getValue();

                            try {
                                Integer number = Integer.valueOf((String) value);

                                summary.setAdditionalDosesAdministered(summary.getAdditionalDosesAdministered() + number);
                            } catch (Exception e) {
                                Timber.e(e, "Error find Number of members ");
                            }
                        }
                    }
                }
            }

            appExecutors.mainThread().execute(() -> {
                presenter.onFetchedMembersCount(summary);
            });
        });

    }

    private void populateEventsPerTask(List<StructureTaskDetails> tasks) {
        SQLiteStatement eventsPerTask = database.compileStatement("SELECT count(*) as events_per_task FROM event_task WHERE task_id = ?");
        SQLiteStatement lastEventDate = database.compileStatement("SELECT max(event_date) FROM event_task WHERE task_id = ?");

        try {
            for (StructureTaskDetails task : tasks) {
                EventTask eventTask = new EventTask();

                eventsPerTask.bindString(1, task.getTaskId());
                eventTask.setEventsPerTask(eventsPerTask.simpleQueryForLong());

                if (eventTask.getEventsPerTask() > 1) {
                    lastEventDate.bindString(1, task.getTaskId());
                    eventTask.setLastEventDate(lastEventDate.simpleQueryForString());
                    task.setLastEdited(DateUtil.parseDate(eventTask.getLastEventDate()));

                }

                if (Intervention.BLOOD_SCREENING.equals(task.getTaskCode())){
                    setPersonTested(task,eventTask.getEventsPerTask() > 1);
                }

            }

        } catch (Exception e) {
            Timber.e(e, "Error querying events counts ");
        } finally {
            if (eventsPerTask != null) {
                eventsPerTask.close();
            }
            if (lastEventDate != null) {
                lastEventDate.close();
            }
        }

    }

    private void setPersonTested(StructureTaskDetails task, boolean isEdit) {

        SQLiteStatement personTestWithEdits = null;
        SQLiteStatement personTested = null;

        try {
            if (isEdit){
                String personTestWithEditsSql = "select person_tested from event_task where id in " +
                        "(select formSubmissionId from event where baseEntityId = ? and eventType = ? " +
                        "order by updatedAt desc limit 1)";
                personTestWithEdits = database.compileStatement(personTestWithEditsSql);
                personTestWithEdits.bindString(1, task.getTaskEntity());
                personTestWithEdits.bindString(2, BLOOD_SCREENING_EVENT);
                task.setPersonTested(personTestWithEdits.simpleQueryForString());
            } else {
                personTested = database.compileStatement("SELECT person_tested FROM event_task WHERE task_id = ?");
                personTested.bindString(1, task.getTaskId());
                task.setPersonTested(personTested.simpleQueryForString());
            }
        } catch (SQLException e) {
            Timber.e(e, "Error querying person tested values ");
        } finally {
            if (personTestWithEdits != null) {
                personTestWithEdits.close();
            }
            if (personTested != null) {
                personTested.close();
            }
        }
    }

    private String getTaskSelect(String mainCondition) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.selectInitiateMainTable(TASK_TABLE, getStructureColumns(), ID_);
        return queryBuilder.mainCondition(mainCondition);
    }


    private String[] getStructureColumns() {
        return new String[]{
                TASK_TABLE + "." + ID_,
                TASK_TABLE + "." + CODE,
                TASK_TABLE + "." + FOR,
                TASK_TABLE + "." + BUSINESS_STATUS,
                TASK_TABLE + "." + STATUS,
                TASK_TABLE + "." + STRUCTURE_ID
        };
    }

    private String[] getMemberColumns() {
        String[] columns = getStructureColumns();
        String[] otherColumns = new String[]{
                "printf('%s %s %s'," + FIRST_NAME + "," + MIDDLE_NAME + "," + LAST_NAME + ") AS " + NAME,
                DOB,
                FAMILY_MEMBER + "." + STRUCTURE_ID
        };
        return ArrayUtils.addAll(columns, otherColumns);
    }

    private StructureTaskDetails readTaskDetails(Cursor cursor) {
        StructureTaskDetails task = new StructureTaskDetails(cursor.getString(cursor.getColumnIndex(ID_)));
        task.setTaskCode(cursor.getString(cursor.getColumnIndex(CODE)));
        task.setTaskEntity(cursor.getString(cursor.getColumnIndex(FOR)));
        task.setBusinessStatus(cursor.getString(cursor.getColumnIndex(BUSINESS_STATUS)));
        task.setTaskStatus(cursor.getString(cursor.getColumnIndex(STATUS)));
        task.setStructureId(cursor.getString(cursor.getColumnIndex(STRUCTURE_ID)));
        return task;
    }

    private StructureTaskDetails readMemberTaskDetails(Cursor cursor) {
        StructureTaskDetails task = readTaskDetails(cursor);
        String dob = cursor.getString(cursor.getColumnIndex(DOB));
        String dobString = Utils.getDuration(dob);
        dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;
        task.setTaskName(cursor.getString(cursor.getColumnIndex(NAME)) + ", " + dobString);
        task.setStructureId(cursor.getString(cursor.getColumnIndex(STRUCTURE_ID)));
        return task;
    }
}
