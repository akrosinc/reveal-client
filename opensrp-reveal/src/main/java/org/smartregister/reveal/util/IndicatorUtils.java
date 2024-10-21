package org.smartregister.reveal.util;

import static java.util.stream.Collectors.toList;
import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE;
import static org.smartregister.reveal.util.Constants.Action.INDEX_CASE_MEMBER;
import static org.smartregister.reveal.util.Constants.Action.MDA_ONCHOCERCIASIS_SURVEY;
import static org.smartregister.reveal.util.Constants.Action.RCD;
import static org.smartregister.reveal.util.Constants.Action.RCD_MEMBER;
import static org.smartregister.reveal.util.Constants.BusinessStatus.COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.INCOMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.INDEX_CASE_COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.INDEX_CASE_NOT_VISITED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.IN_PROGRESS;
import static org.smartregister.reveal.util.Constants.BusinessStatus.MDA_COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.MDA_PARTIALLY_COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.MDA_REFUSED_OR_ABSENT;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_ELIGIBLE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;
import static org.smartregister.reveal.util.Constants.EventType.CDD_DRUG_RECEIVED_EVENT;
import static org.smartregister.reveal.util.Constants.EventType.CDD_DRUG_WITHDRAWAL_EVENT;
import static org.smartregister.reveal.util.Constants.EventType.MDA_ONCHO_EVENT;
import static org.smartregister.reveal.util.Constants.Intervention.CDD_SUPERVISION;
import static org.smartregister.reveal.util.Constants.Intervention.CELL_COORDINATION;
import static org.smartregister.reveal.util.Constants.JsonForm.HEALTH_EDUCATION_5_TO_15;
import static org.smartregister.reveal.util.Constants.JsonForm.HEALTH_EDUCATION_ABOVE_16;
import static org.smartregister.reveal.util.Constants.JsonForm.SUM_TREATED_1_TO_4;
import static org.smartregister.reveal.util.Constants.JsonForm.SUM_TREATED_5_TO_15;
import static org.smartregister.reveal.util.Constants.JsonForm.SUM_TREATED_6_TO_11_MOS;
import static org.smartregister.reveal.util.Constants.JsonForm.SUM_TREATED_ABOVE_16;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import org.joda.time.DateTime;
import org.smartregister.domain.Event;
import org.smartregister.domain.Obs;
import org.smartregister.domain.Task;
import org.smartregister.domain.db.EventClient;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.InterventionAdditionalDetailsRepository;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.model.BaseTaskDetails;
import org.smartregister.reveal.model.IndicatorDetails;
import org.smartregister.reveal.model.TaskDetails;
import org.smartregister.reveal.util.Constants.BusinessStatus;
import org.smartregister.reveal.util.Constants.Intervention;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 2019-09-27.
 */
public class IndicatorUtils {


    public static final String VITAMIN_A = "Vitamin A";
    public static final String ALB_MEB = "ALB/MEB";
    public static final String PZQ = "PZQ";
    public static final String NTD_TREATED = "ntd_treated";

    public static final String STH = "STH";

    public static final String TOTAL_MALE_AND_FEMALE = "total_male_and_female";

    public static final String SCH = "SCH";

    public static final String DRUGS = "drugs";

    public static final String ADMINSTERED = "adminstered";

    public static final String MBZ = "MBZ";

    public static final String DAMAGED = "damaged";

    public static final String DRUG_ISSUED = "drug_issued";

    public static final String BOTH = "Both";

    public static final String MBZ_RECEIVED = "mbz_received";


    public static final String DRUG_WITHDRAWN = "drug_withdrawn";

    public static final String PZQ_RECEIVED = "pzq_received";

    public static final String MBZ_WITHDRAWN = "mbz_withdrawn";

    public static final String PZQ_WITHDRAWN = "pzq_withdrawn";

    public static final String DAMAGED_PZQ = "damaged_pzq";

    public static final String DAMAGED_MBZ = "damaged_mbz";

    /**
     * Process task details from map of tasks (key : structureId)
     */
    public static List<TaskDetails> processTaskDetails(Map<String, Set<Task>> map) {

        List<TaskDetails> taskDetailsList = new ArrayList<>();

        for (Map.Entry<String, Set<Task>> entry : map.entrySet()) {

            for (Task task : entry.getValue()) {

                taskDetailsList.add(convertToTaskDetails(task));
            }

        }

        return taskDetailsList;

    }

    /**
     * Convert task to task details object
     *
     * @param task the task
     * @return TaskDetails object
     */

    public static TaskDetails convertToTaskDetails(Task task) {

        TaskDetails taskDetails = new TaskDetails(task.getIdentifier());

        taskDetails.setTaskCode(task.getCode());
        taskDetails.setTaskEntity(task.getForEntity());
        taskDetails.setBusinessStatus(task.getBusinessStatus());
        taskDetails.setTaskStatus(task.getStatus().name());
        taskDetails.setStructureId(task.getStructureId());
        taskDetails.setGroupId(task.getGroupIdentifier());

        return taskDetails;

    }

    public static IndicatorDetails processIndicators(List<TaskDetails> tasks) {

        Map<String, TaskDetails> indicatorDetailsMap = new HashMap<>();

        IndicatorDetails indicatorDetails = new IndicatorDetails();

        if (tasks != null) {

            for (int i = 0; i < tasks.size(); i++) {

                if (Constants.Intervention.IRS.equals(tasks.get(i).getTaskCode())) {

                    indicatorDetailsMap.put(tasks.get(i).getStructureId(), tasks.get(i));
                }

            }

            for (Map.Entry<String, TaskDetails> entry : indicatorDetailsMap.entrySet()) {

                if (Constants.BusinessStatusWrapper.SPRAYED.contains(entry.getValue().getBusinessStatus())) {

                    indicatorDetails.setSprayed(indicatorDetails.getSprayed() + 1);

                } else if (Constants.BusinessStatusWrapper.NOT_SPRAYED.contains(entry.getValue().getBusinessStatus())) {

                    indicatorDetails.setNotSprayed(indicatorDetails.getNotSprayed() + 1);

                } else if (Constants.BusinessStatusWrapper.NOT_VISITED.contains(entry.getValue().getBusinessStatus())) {

                    indicatorDetails.setNotVisited(indicatorDetails.getNotVisited() + 1);

                } else if (Constants.BusinessStatusWrapper.NOT_ELIGIBLE.contains(entry.getValue().getBusinessStatus())) {
                    indicatorDetails.setIneligible(indicatorDetails.getIneligible() + 1);
                }
            }

        }

        indicatorDetails.setTotalStructures(indicatorDetailsMap.size() - indicatorDetails.getIneligible());
        indicatorDetails.setProgress(indicatorDetails.getTotalStructures() > 0 ? Math.round(indicatorDetails.getSprayed() * 100 / indicatorDetails.getTotalStructures()) : 0);

        return indicatorDetails;
    }


    public static List<String> populateNamibiaSprayIndicators(Context context, IndicatorDetails indicatorDetails) {
        List<String> sprayIndicator = new ArrayList<>();
        sprayIndicator.add(context.getResources().getString(R.string.structures_targeted));
        sprayIndicator.add(String.valueOf(indicatorDetails.getTarget()));

        sprayIndicator.add(context.getResources().getString(R.string.structures_visited_found));
        sprayIndicator.add(String.valueOf(indicatorDetails.getFoundStructures()));

        sprayIndicator.add(context.getResources().getString(R.string.structures_sprayed));
        sprayIndicator.add(String.valueOf(indicatorDetails.getSprayed()));

        sprayIndicator.add(context.getResources().getString(R.string.structures_not_sprayed));
        sprayIndicator.add(String.valueOf(indicatorDetails.getNotSprayed()));


        return sprayIndicator;
    }

    public static List<String> populateSprayIndicators(Context context, IndicatorDetails indicatorDetails) {

        int totalStructures = indicatorDetails.getTotalStructures();
        int sprayCoverage = indicatorDetails.getProgress();

        List<String> sprayIndicator = new ArrayList<>();

        sprayIndicator.add(context.getResources().getString(R.string.spray_coverage));
        sprayIndicator.add(context.getResources().getString(R.string.n_percent, sprayCoverage));

        int totalFound = (indicatorDetails.getSprayed() + indicatorDetails.getNotSprayed());

        sprayIndicator.add(context.getResources().getString(R.string.structures_remaining_90));
        sprayIndicator.add(String.valueOf(Math.round(totalStructures * 0.9) - indicatorDetails.getSprayed()));

        sprayIndicator.add(context.getResources().getString(R.string.total_structures));
        sprayIndicator.add(String.valueOf(totalStructures));

        sprayIndicator.add(context.getResources().getString(R.string.structures_not_visited));
        sprayIndicator.add(String.valueOf(indicatorDetails.getNotVisited()));

        sprayIndicator.add(context.getResources().getString(R.string.structures_visited_found));
        sprayIndicator.add(String.valueOf(totalFound));

        sprayIndicator.add(context.getResources().getString(R.string.sprayed));
        sprayIndicator.add(String.valueOf(indicatorDetails.getSprayed()));

        sprayIndicator.add(context.getResources().getString(R.string.structures_not_sprayed));
        sprayIndicator.add(String.valueOf(indicatorDetails.getNotSprayed()));

        return sprayIndicator;
    }

    public static List<String> populateMaliIndicators(Context context, IndicatorDetails indicatorDetails) {

        int totalStructures = indicatorDetails.getMdaTotalStructures();

        List<String> indicators = new ArrayList<>();

        indicators.add(context.getResources().getString(R.string.total_structures));
        indicators.add(String.valueOf(totalStructures));

        int structureVisited = totalStructures - indicatorDetails.getMdaNotVisited();
        indicators.add(context.getResources().getString(R.string.structure_visited));
        indicators.add(String.valueOf(structureVisited));

        indicators.add(context.getResources().getString(R.string.structure_not_visited));
        indicators.add(String.valueOf(indicatorDetails.getMdaNotVisited()));

        indicators.add(context.getResources().getString(R.string.structure_complete_drug_distribution));
        indicators.add(String.valueOf(indicatorDetails.getMdaComplete()));

        indicators.add(context.getResources().getString(R.string.structure_partial_drug_distribution));
        indicators.add(String.valueOf(indicatorDetails.getMdaPartiallyComplete()));

        indicators.add(context.getResources().getString(R.string.structure_refused_or_absent));
        indicators.add(String.valueOf(indicatorDetails.getMdaRefusedOrAbsent()));

        indicators.add(context.getResources().getString(R.string.individual_total_number_of_eligible_people));
        indicators.add(String.valueOf(indicatorDetails.getMdaTotalEligible()));

        indicators.add(context.getResources().getString(R.string.individual_total_treated));
        indicators.add(String.valueOf(indicatorDetails.getMdaTotalTreated()));

        return indicators;

    }

    public static List<String> populateGdrsIndicators(Context context, IndicatorDetails indicatorDetails) {

        List<String> indicators = new ArrayList<>();

        indicators.add(context.getResources().getString(R.string.totalRcdStructures));
        indicators.add(String.valueOf(indicatorDetails.getTotalRcdStructures()));

        indicators.add(context.getResources().getString(R.string.totalIndexStructure));
        indicators.add(String.valueOf(indicatorDetails.getTotalIndexStructure()));

        indicators.add(context.getResources().getString(R.string.totalRcdMemberTasks));
        indicators.add(String.valueOf(indicatorDetails.getTotalRcdMemberTasks()));

        indicators.add(context.getResources().getString(R.string.totalIndexMemberTasks));
        indicators.add(String.valueOf(indicatorDetails.getTotalIndexMemberTasks()));

        indicators.add(context.getResources().getString(R.string.totalCompleteRcdStructures));
        indicators.add(String.valueOf(indicatorDetails.getTotalCompleteRcdStructures()));

        indicators.add(context.getResources().getString(R.string.totalCompleteIndexStructure));
        indicators.add(String.valueOf(indicatorDetails.getTotalCompleteIndexStructure()));

        indicators.add(context.getResources().getString(R.string.totalCompleteRcdMemberTasks));
        indicators.add(String.valueOf(indicatorDetails.getTotalCompleteRcdMemberTasks()));

        indicators.add(context.getResources().getString(R.string.totalCompleteIndexMemberTasks));
        indicators.add(String.valueOf(indicatorDetails.getTotalCompleteIndexMemberTasks()));

        indicators.add(context.getResources().getString(R.string.totalVisitRCDStructure));
        indicators.add(String.valueOf(indicatorDetails.getTotalRcdStructures()));

        indicators.add(context.getResources().getString(R.string.totalVisitIndexStructure));
        indicators.add(String.valueOf(indicatorDetails.getTotalIndexStructure()));

        indicators.add(context.getResources().getString(R.string.totalUnVisitRcdStructures));
        indicators.add(String.valueOf(indicatorDetails.getTotalRcdStructures()));

        indicators.add(context.getResources().getString(R.string.totalUnVisitIndexStructure));
        indicators.add(String.valueOf(indicatorDetails.getTotalIndexStructure()));

        return indicators;

    }

    public static IndicatorDetails getNamibiaIndicators(String locationId, String planId, SQLiteDatabase sqLiteDatabase) {
        String query = "select" + " sum(ifNull(ss.nSprayableTotal,0)) as foundStruct" + " ,sum(ifNull(ss.nSprayedTotalFirst,0)+ifNull(ss.nSprayedTotalMop,0)) as sprayedStruct" + " ,sum(ifNull(nSprayableTotal,0)- ifNull(nSprayedTotalFirst,0) - ifnull(nSprayedTotalMop,0)) as notSprayedStruct" + " from sprayed_structures ss " + " join structure s on s._id=ss.id" + " where parent_id=? and ss.plan_id=?";
        IndicatorDetails indicatorDetails = new IndicatorDetails();
        try (Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{locationId, planId})) {
            if (cursor.moveToNext()) {
                indicatorDetails.setFoundStructures(cursor.getInt(cursor.getColumnIndex("foundStruct")));
                indicatorDetails.setSprayed(cursor.getInt(cursor.getColumnIndex("sprayedStruct")));
                indicatorDetails.setNotSprayed(cursor.getInt(cursor.getColumnIndex("notSprayedStruct")));
            }
        } catch (SQLiteException e) {
            Timber.tag("Reveal Exception").w(e);
        }
        return indicatorDetails;
    }

    public static IndicatorDetails processRwandaIndicators(List<TaskDetails> tasks) {
        IndicatorDetails indicatorDetails = new IndicatorDetails();
        Integer value;

        List<TaskDetails> validTasks = tasks.stream().filter(taskDetails -> taskDetails.getTaskCode().equals(CELL_COORDINATION) && (taskDetails.getBusinessStatus().equals(IN_PROGRESS) || taskDetails.getBusinessStatus().equals(COMPLETE))).collect(toList());

        Set<String> taskIdentifiers = validTasks.stream().map(taskDetails -> taskDetails.getTaskId()).collect(Collectors.toSet());

        EventClientRepository eventClientRepository = RevealApplication.getInstance().getContext().getEventClientRepository();

        List<Event> dataCaptured = eventClientRepository.getEventsByTaskIds(taskIdentifiers);

        List<Event> latestEvents = validTasks.stream().map(taskDetails -> {
            Map<DateTime, Event> eachTaskEventAndDateMap = dataCaptured.stream().filter(event -> taskDetails.getTaskId().equals(event.getDetails().getOrDefault("taskIdentifier", "empty"))).collect(Collectors.toMap(Event::getDateCreated, Function.identity()));
            List<DateTime> eventDates = eachTaskEventAndDateMap.keySet().stream().collect(toList());
            DateTime maxDatTime = Collections.max(eventDates);
            return eachTaskEventAndDateMap.get(maxDatTime);
        }).collect(toList());

        value = latestEvents.stream().map(Event::getObs).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(HEALTH_EDUCATION_5_TO_15)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setHealthEducatedChildren5To15(value);

        value = latestEvents.stream().map(Event::getObs).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(HEALTH_EDUCATION_ABOVE_16)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setHealthEducatedChildrenAbove16(value);

        value = latestEvents.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(VITAMIN_A)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_6_TO_11_MOS)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setVitaminTreatedChildren6To11Months(value);

        value = latestEvents.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(VITAMIN_A)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_1_TO_4)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setVitaminTreatedChildren12To59Months(value);

        value = latestEvents.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(ALB_MEB)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> (obsValue.getFieldCode().equals(SUM_TREATED_1_TO_4))).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setAlbMebTreatedChildren12To59Months(value);

        value = latestEvents.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(ALB_MEB)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_5_TO_15)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setAlbMebTreatedChildren5To15Years(value);

        value = latestEvents.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(PZQ)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_5_TO_15)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setPzqTreatedChildren5To15Years(value);

        value = latestEvents.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getValue().equals(ALB_MEB)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_ABOVE_16)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setAlbMebTreatedChildrenAbove16Years(value);

        value = latestEvents.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getValue().equals(PZQ)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_ABOVE_16)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setPzqTreatedChildrenAbove16Years(value);


        return indicatorDetails;
    }

    public static List<String> populateRwandaIndicators(Context context, IndicatorDetails indicatorDetails) {
        List<String> indicators = new ArrayList<>();

        indicators.add(context.getResources().getString(R.string.health_education_ages_5_to_15_years));
        indicators.add(String.valueOf(indicatorDetails.getHealthEducatedChildren5To15()));

        indicators.add(context.getResources().getString(R.string.health_education_16_years_and_above));
        indicators.add(String.valueOf(indicatorDetails.getHealthEducatedChildrenAbove16()));

        indicators.add(context.getResources().getString(R.string.vitamina_total_6_to_11_months));
        indicators.add(String.valueOf(indicatorDetails.getVitaminTreatedChildren6To11Months()));

        indicators.add(context.getResources().getString(R.string.vitamina_total_12_to_59_months));
        indicators.add(String.valueOf(indicatorDetails.getVitaminTreatedChildren12To59Months()));

        indicators.add(context.getResources().getString(R.string.alb_meb_total_12_to_59_months));
        indicators.add(String.valueOf(indicatorDetails.getAlbMebTreatedChildren12To59Months()));

        indicators.add(context.getResources().getString(R.string.alb_meb_total_5_to_15_years));
        indicators.add(String.valueOf(indicatorDetails.getAlbMebTreatedChildren5To15Years()));

        indicators.add(context.getResources().getString(R.string.alb_meb_total_16_years_and_above));
        indicators.add(String.valueOf(indicatorDetails.getAlbMebTreatedChildrenAbove16Years()));

        indicators.add(context.getResources().getString(R.string.pzq_total_5_to_15_years));
        indicators.add(String.valueOf(indicatorDetails.getPzqTreatedChildren5To15Years()));

        indicators.add(context.getResources().getString(R.string.pzq_total_16_years_and_above));
        indicators.add(String.valueOf(indicatorDetails.getPzqTreatedChildrenAbove16Years()));

        return indicators;
    }

    public static List<String> populateNigeriaIndicators(Context context, IndicatorDetails indicatorDetails) {
        List<String> indicators = new ArrayList<>();
        int totalStructures = indicatorDetails.getTotalStructures() - indicatorDetails.getIneligible();
        indicators.add(context.getResources().getString(R.string.structure_total));
        indicators.add(String.valueOf(totalStructures));


        int structureVisited = totalStructures - indicatorDetails.getNotVisited();
        indicators.add(context.getResources().getString(R.string.structure_visited));
        indicators.add(String.valueOf(structureVisited));

        indicators.add(context.getResources().getString(R.string.structure_not_visited));
        indicators.add(String.valueOf(indicatorDetails.getNotVisited()));

        indicators.add(context.getResources().getString(R.string.structure_confirmed_eligible));
        indicators.add(String.valueOf(indicatorDetails.getFoundStructures()));


        indicators.add(context.getResources().getString(R.string.structure_complete_drug_distribution));
        indicators.add(String.valueOf(indicatorDetails.getCompleteDrugDistribution()));


        indicators.add(context.getResources().getString(R.string.structure_partial_drug_distribution));
        indicators.add(String.valueOf(indicatorDetails.getPartialDrugDistribution()));

        indicators.add(context.getResources().getString(R.string.individual_total_number_of_children_eligible_3_to_49_mos));
        indicators.add(String.valueOf(indicatorDetails.getChildrenEligible()));

        indicators.add(context.getResources().getString(R.string.individual_total_treated_3_to_59_mos));
        indicators.add(String.valueOf(indicatorDetails.getTotalIndividualTreated()));

        return indicators;
    }

    public static List<String> populateNigeriaIndicatorsSurvey(Context context, IndicatorDetails indicatorDetails) {
        List<String> indicators = new ArrayList<>();

        indicators.add(context.getResources().getString(R.string.structure_total_survey));
        indicators.add(String.valueOf(indicatorDetails.getTotalStructures() - indicatorDetails.getIneligible()));

//        indicators.add(context.getResources().getString(R.string.hdss_structures_visited_survey));
//        indicators.add(String.valueOf(indicatorDetails.getFoundStructures()));

        indicators.add(context.getResources().getString(R.string.hdss_structures_visited_survey));
        indicators.add(String.valueOf(indicatorDetails.getSurveyedStructures()));

        indicators.add(context.getResources().getString(R.string.structures_not_visited_survey));
        indicators.add(String.valueOf(indicatorDetails.getNotVisited()));

//        indicators.add(context.getResources().getString(R.string.structure_ineligible));
//        indicators.add(String.valueOf(indicatorDetails.getIneligible()));

        return indicators;
    }

    public static IndicatorDetails processIndicatorsNigeriaSurvey(List<TaskDetails> tasks) {
        IndicatorDetails indicatorDetails = new IndicatorDetails();

        if (tasks != null) {
            List<TaskDetails> notVisited = tasks.stream().filter(Objects::nonNull).filter(t -> NOT_VISITED.equals(t.getBusinessStatus())).collect(toList());
            List<TaskDetails> complete = tasks.stream().filter(Objects::nonNull).filter(t -> COMPLETE.equals(t.getBusinessStatus())).collect(toList());
            List<TaskDetails> notEligible = tasks.stream().filter(Objects::nonNull).filter(t -> NOT_ELIGIBLE.equals(t.getBusinessStatus())).collect(toList());
            indicatorDetails.setIneligible(notEligible.size());
            indicatorDetails.setSurveyedStructures(complete.size());
            indicatorDetails.setNotVisited(notVisited.size());
//            indicatorDetails.setFoundStructures(indicatorDetails.getSurveyedStructures() + indicatorDetails.getIneligible());
            indicatorDetails.setTotalStructures(tasks.size());
        } else {
            indicatorDetails.setIneligible(0);
            indicatorDetails.setSurveyedStructures(0);
            indicatorDetails.setNotVisited(0);
//            indicatorDetails.setFoundStructures(0);
            indicatorDetails.setTotalStructures(0);
        }


        return indicatorDetails;
    }

    public static IndicatorDetails processIndicatorsNigeria(List<TaskDetails> tasks) {
        Map<String, List<TaskDetails>> indicatorDetailsMap = new HashMap<>();
        IndicatorDetails indicatorDetails = new IndicatorDetails();
        if (tasks != null) {

            for (int i = 0; i < tasks.size(); i++) {
                String structureId = tasks.get(i).getStructureId();
                List<TaskDetails> taskDetails = indicatorDetailsMap.get(structureId);
                if (taskDetails == null) {
                    taskDetails = new ArrayList<>();
                }
                taskDetails.add(tasks.get(i));
                indicatorDetailsMap.put(tasks.get(i).getStructureId(), taskDetails);
            }

            for (Map.Entry<String, List<TaskDetails>> entry : indicatorDetailsMap.entrySet()) {
                for (TaskDetails task : entry.getValue()) {
                    if (Constants.BusinessStatusWrapper.NOT_ELIGIBLE.contains(task.getBusinessStatus())) {
                        indicatorDetails.setIneligible(indicatorDetails.getIneligible() + 1);
                        break;
                    }
                    if (task.getTaskCode().equals(Constants.Intervention.MDA_DISPENSE) && Constants.BusinessStatusWrapper.MDA_DISPENSE_ELIGIBLE_STATUS.contains(task.getBusinessStatus())) {
                        indicatorDetails.setFoundStructures(indicatorDetails.getFoundStructures() + 1);
                        break;
                    }
                }
                indicatorDetails.setNotVisited(indicatorDetails.getNotVisited() + calculateStructuresNotFamilyRegistered(entry.getValue()));
                indicatorDetails.setCompleteDrugDistribution(indicatorDetails.getCompleteDrugDistribution() + calculateDrugCompletion(entry.getValue()));
                indicatorDetails.setPartialDrugDistribution(indicatorDetails.getPartialDrugDistribution() + calculatePartialDrugDistribution(entry.getValue()));
                indicatorDetails.setChildrenEligible(indicatorDetails.getChildrenEligible() + calculateChildrenEligible(entry.getValue()));
                indicatorDetails.setTotalIndividualTreated(indicatorDetails.getTotalIndividualTreated() + calculateChildrenTreated(entry.getValue()));
            }
        }

        indicatorDetails.setTotalStructures(indicatorDetailsMap.size());
        return indicatorDetails;
    }


    public static List<String> populateKenyaIndicators(Context context, final IndicatorDetails indicatorDetails) {
        List<String> indicators = new ArrayList<>();
        indicators.add(context.getResources().getString(R.string.number_of_people_treated_for_sth));
        indicators.add(String.valueOf(indicatorDetails.getPeopleTreatedForSTH()));

        indicators.add(context.getResources().getString(R.string.number_of_people_treated_for_sch));
        indicators.add(String.valueOf(indicatorDetails.getPeopleTreatedForSCH()));


        indicators.add(context.getResources().getString(R.string.mbz_tablets_remaining));
        indicators.add(String.valueOf(indicatorDetails.getMbzTabletsRemaining()));

        indicators.add(context.getResources().getString(R.string.pzq_tablets_remaining));
        indicators.add(String.valueOf(indicatorDetails.getPzqTabletsRemaining()));

        indicators.add(context.getResources().getString(R.string.mbz_dispensed));
        indicators.add(String.valueOf(indicatorDetails.getMbzDispensed()));

        indicators.add(context.getResources().getString(R.string.pzq_dispensed));
        indicators.add(String.valueOf(indicatorDetails.getPzqDispensed()));

        indicators.add(context.getResources().getString(R.string.mbz_damaged));
        indicators.add(String.valueOf(indicatorDetails.getMbzDamaged()));


        indicators.add(context.getResources().getString(R.string.pzq_damaged));
        indicators.add(String.valueOf(indicatorDetails.getPzqDamaged()));
        return indicators;
    }

    public static IndicatorDetails processIndicatorsKenya(final List<TaskDetails> tasks) {
        IndicatorDetails indicatorDetails = new IndicatorDetails();
        List<TaskDetails> validTasks = tasks.stream().filter(taskDetails -> taskDetails.getTaskCode().equals(CDD_SUPERVISION) && (taskDetails.getBusinessStatus().equals(INCOMPLETE) || taskDetails.getBusinessStatus().equals(COMPLETE))).collect(toList());

        Set<String> taskIdentifiers = validTasks.stream().map(taskDetails -> taskDetails.getTaskId()).collect(Collectors.toSet());

        EventClientRepository eventClientRepository = RevealApplication.getInstance().getContext().getEventClientRepository();

        List<Event> cddSupervisionEvents = eventClientRepository.getEventsByTaskIds(taskIdentifiers);
        List<Event> latestCddSupervisionEvents = getLatestEventsPerFormSubmissionId(cddSupervisionEvents, cddSupervisionEvents.stream().map(Event::getFormSubmissionId).collect(Collectors.toSet()));


        List<Event> otherFormsEvents = eventClientRepository.fetchEventClientsByEventTypesAndPlanId(Arrays.asList(CDD_DRUG_RECEIVED_EVENT, CDD_DRUG_WITHDRAWAL_EVENT), PreferencesUtil.getInstance().getCurrentPlanId()).stream().map(EventClient::getEvent).collect(toList());


        List<Event> otherFormsLatestEvents = getLatestEventsPerFormSubmissionId(otherFormsEvents, otherFormsEvents.stream().map(Event::getFormSubmissionId).collect(Collectors.toSet()));

        List<Event> drugReceivedFormEvents = otherFormsLatestEvents.stream().filter(event -> CDD_DRUG_RECEIVED_EVENT.equals(event.getEventType())).collect(toList());
        List<Event> drugWithdrawalFormEvents = otherFormsLatestEvents.stream().filter(event -> CDD_DRUG_WITHDRAWAL_EVENT.equals(event.getEventType())).collect(toList());

        indicatorDetails.setPeopleTreatedForSTH(calculatePeopleTreatedForSTH(latestCddSupervisionEvents));
        indicatorDetails.setPeopleTreatedForSCH(calculatePeopleTreatedForSCH(latestCddSupervisionEvents));
        indicatorDetails.setMbzTabletsRemaining(calculateRemainingMBZ(latestCddSupervisionEvents, drugReceivedFormEvents, drugWithdrawalFormEvents));
        indicatorDetails.setPzqTabletsRemaining(calculateRemainingPZQ(latestCddSupervisionEvents, drugReceivedFormEvents, drugWithdrawalFormEvents));
        indicatorDetails.setMbzDispensed(calculateDispensedMBZ(latestCddSupervisionEvents));
        indicatorDetails.setPzqDispensed(calculateDispensedPZQ(latestCddSupervisionEvents));
        indicatorDetails.setMbzDamaged(calculateDamagedMBZ(latestCddSupervisionEvents));
        indicatorDetails.setPzqDamaged(calculateDamagedPZQ(latestCddSupervisionEvents));
        return indicatorDetails;
    }

    public static IndicatorDetails processIndicatorsGdrs(final List<TaskDetails> tasks) {

        Map<String, List<TaskDetails>> tasksByTaskCode = tasks.stream()
                .collect(Collectors.groupingBy(BaseTaskDetails::getTaskCode, toList()));

        Map<String, List<TaskDetails>> completedTasksByTaskCode = tasks.stream()
                .filter(details -> COMPLETE.equals(details.getBusinessStatus()))
                .collect(Collectors.groupingBy(BaseTaskDetails::getTaskCode, toList()));

        Map<String, List<TaskDetails>> unvisitedTasksByTaskCode = tasks.stream()
                .filter(details -> NOT_VISITED.equals(details.getBusinessStatus()))
                .collect(Collectors.groupingBy(BaseTaskDetails::getTaskCode, toList()));

        List<TaskDetails> total = tasks.stream()
                .filter(details->List.of(RCD,INDEX_CASE).contains(details.getTaskCode()))
                .collect( toList());

        List<TaskDetails> totalVisited = tasks.stream()
                .filter(details->List.of(RCD,INDEX_CASE).contains(details.getTaskCode()))
                .filter(details -> !List.of(NOT_VISITED, INDEX_CASE_NOT_VISITED).contains(details.getBusinessStatus()))
                .collect( toList());

        List<TaskDetails> indexStructuresVisited = tasks.stream()
                .filter(details -> Constants.Action.INDEX_CASE.equals(details.getTaskCode()))
                .filter(details -> !NOT_VISITED.equals(details.getBusinessStatus()))
                .collect(Collectors.toList());

        List<TaskDetails> rcdStructuresVisited = tasks.stream()
                .filter(details -> RCD.equals(details.getTaskCode()))
                .filter(details -> !INDEX_CASE_COMPLETE.equals(details.getBusinessStatus()))
                .collect(Collectors.toList());


        int totalRcdStructures = tasksByTaskCode.containsKey(RCD) ?  Objects.requireNonNull(tasksByTaskCode.get(RCD)).size() : 0;
        int totalRcdMemberTasks = tasksByTaskCode.containsKey(RCD_MEMBER) ?  Objects.requireNonNull(tasksByTaskCode.get(RCD_MEMBER)).size() : 0;
        int totalIndexStructure = tasksByTaskCode.containsKey(INDEX_CASE) ?  Objects.requireNonNull(tasksByTaskCode.get(INDEX_CASE)).size() : 0;
        int totalIndexMemberTasks = tasksByTaskCode.containsKey(INDEX_CASE_MEMBER) ?  Objects.requireNonNull(tasksByTaskCode.get(INDEX_CASE_MEMBER)).size() : 0;

        int totalCompleteRcdStructures = completedTasksByTaskCode.containsKey(RCD) ?  Objects.requireNonNull(completedTasksByTaskCode.get(RCD)).size() : 0;
        int totalCompleteRcdMemberTasks = completedTasksByTaskCode.containsKey(RCD_MEMBER) ?  Objects.requireNonNull(completedTasksByTaskCode.get(RCD_MEMBER)).size() : 0;
        int totalCompleteIndexStructure = completedTasksByTaskCode.containsKey(INDEX_CASE) ?  Objects.requireNonNull(completedTasksByTaskCode.get(INDEX_CASE)).size() : 0;
        int totalCompleteIndexMemberTasks = completedTasksByTaskCode.containsKey(INDEX_CASE_MEMBER) ?  Objects.requireNonNull(completedTasksByTaskCode.get(INDEX_CASE_MEMBER)).size() : 0;

        int totalUnVisitRcdStructures = unvisitedTasksByTaskCode.containsKey(RCD) ?  Objects.requireNonNull(unvisitedTasksByTaskCode.get(RCD)).size() : 0;
        int totalUnVisitIndexStructure = unvisitedTasksByTaskCode.containsKey(INDEX_CASE) ?  Objects.requireNonNull(unvisitedTasksByTaskCode.get(INDEX_CASE)).size() : 0;

        int totalVisitIndexStructure = indexStructuresVisited.size();
        int totalVisitRCDStructure = rcdStructuresVisited.size();

        int indexStructureCoverage = totalIndexStructure>0 ? totalCompleteIndexStructure / totalIndexStructure : 0;
        int rcdStructureCoverage = totalRcdStructures>0 ? totalCompleteRcdStructures / totalRcdStructures : 0;
        int visitedCoverage = !total.isEmpty() ? totalVisited.size() / total.size() : 0;


        IndicatorDetails indicatorDetails = IndicatorDetails.builder()
                .totalRcdStructures(totalRcdStructures)
                .totalRcdMemberTasks(totalRcdMemberTasks)
                .totalIndexStructure(totalIndexStructure)
                .totalIndexMemberTasks(totalIndexMemberTasks)
                .totalCompleteRcdStructures(totalCompleteRcdStructures)
                .totalCompleteRcdMemberTasks(totalCompleteRcdMemberTasks)
                .totalCompleteIndexStructure(totalCompleteIndexStructure)
                .totalCompleteIndexMemberTasks(totalCompleteIndexMemberTasks)
                .totalUnVisitRcdStructures(totalUnVisitRcdStructures)
                .totalUnVisitIndexStructure(totalUnVisitIndexStructure)
                .totalVisitIndexStructure(totalVisitIndexStructure)
                .totalVisitRCDStructure(totalVisitRCDStructure)
                .indexStructureCoverage(indexStructureCoverage)
                .rcdStructureCoverage(rcdStructureCoverage)
                .visitedGDRSCoverage(visitedCoverage)
                .build();
        return indicatorDetails;
    }

    public static IndicatorDetails processIndicatorsMali(final List<TaskDetails> tasks) {
        IndicatorDetails indicatorDetails = new IndicatorDetails();
        List<TaskDetails> validTasks = tasks.stream()
                .filter(taskDetails -> taskDetails.getTaskCode()
                        .equals(MDA_ONCHOCERCIASIS_SURVEY))
                .collect(toList());

        long mdaComplete = validTasks.stream()
                .filter(taskDetails ->
                        taskDetails.getBusinessStatus() != null
                                && taskDetails.getBusinessStatus().equals(MDA_COMPLETE))
                .map(BaseTaskDetails::getStructureId).distinct().count();
        indicatorDetails.setMdaComplete(Long.valueOf(mdaComplete).intValue());

        long notVisited = validTasks.stream().filter(taskDetails ->
                        taskDetails.getBusinessStatus() != null
                                && taskDetails.getBusinessStatus().equals(NOT_VISITED))
                .map(BaseTaskDetails::getStructureId).filter(Objects::nonNull).distinct().count();
        indicatorDetails.setMdaNotVisited(Long.valueOf(notVisited).intValue());

        long partiallyComplete = validTasks.stream().filter(taskDetails -> taskDetails.getBusinessStatus() != null
                        && taskDetails.getBusinessStatus().equals(MDA_PARTIALLY_COMPLETE))
                .map(BaseTaskDetails::getStructureId).filter(Objects::nonNull).distinct().count();
        indicatorDetails.setMdaPartiallyComplete(Long.valueOf(partiallyComplete).intValue());

        long refusedOrAbsent = validTasks.stream().filter(taskDetails -> taskDetails.getBusinessStatus() != null
                        && taskDetails.getBusinessStatus().equals(MDA_REFUSED_OR_ABSENT))
                .map(BaseTaskDetails::getStructureId).filter(Objects::nonNull).distinct().count();
        indicatorDetails.setMdaRefusedOrAbsent(Long.valueOf(refusedOrAbsent).intValue());

        long notEligible = validTasks.stream().filter(taskDetails -> taskDetails.getBusinessStatus() != null
                        && taskDetails.getBusinessStatus().equals(NOT_ELIGIBLE))
                .map(BaseTaskDetails::getStructureId).filter(Objects::nonNull).distinct().count();
        indicatorDetails.setMdaNotEligible(Long.valueOf(notEligible).intValue());

        long totalStructures = mdaComplete + notVisited + partiallyComplete + refusedOrAbsent;

        indicatorDetails.setMdaTotalStructures(Long.valueOf(totalStructures).intValue());

        double distributionCoverageDouble = totalStructures > 0 ? (double) (mdaComplete + partiallyComplete) / (double) totalStructures * 100 : 0;

        indicatorDetails.setMdaDistributionCoverage(Double.valueOf(Math.floor(distributionCoverageDouble)).intValue());

        double successRate = (mdaComplete + partiallyComplete + refusedOrAbsent) > 0 ? (double) (mdaComplete + partiallyComplete) / (double) (mdaComplete + partiallyComplete + refusedOrAbsent) * 100 : 0;

        indicatorDetails.setMdaSuccessRate(Double.valueOf(Math.floor(successRate)).intValue());

        double foundCoverageDouble = totalStructures > 0 ? (double) (mdaComplete + partiallyComplete) / (double) totalStructures * 100 : 0;

        indicatorDetails.setMdaFoundCoverage(Double.valueOf(Math.floor(foundCoverageDouble)).intValue());

        InterventionAdditionalDetailsRepository interventionAdditionalDetailsRepository = RevealApplication.getInstance().getContext().getInterventionAdditionalDetailsRepository();

        try {
            int eligiblePop = interventionAdditionalDetailsRepository.getSumPerFieldCode("eligible_pop"
                    , PreferencesUtil.getInstance().getCurrentPlanId()
                    , PreferencesUtil.getInstance().getCurrentOperationalAreaId());
            int totalTreated = interventionAdditionalDetailsRepository.getSumPerFieldCode("total_treated"
                    , PreferencesUtil.getInstance().getCurrentPlanId()
                    , PreferencesUtil.getInstance().getCurrentOperationalAreaId());

            indicatorDetails.setMdaTotalEligible(eligiblePop);
            indicatorDetails.setMdaTotalTreated(totalTreated);
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e.toString());

            indicatorDetails.setMdaTotalEligible(0);
            indicatorDetails.setMdaTotalTreated(0);
        }

        return indicatorDetails;
    }

    @NonNull
    private static List<Event> getLatestEventsPerFormSubmissionId(final List<Event> events, final Set<String> formSubmissionIds) {
        return formSubmissionIds.stream().map(formSubmissionId -> {
            List<Event> groupedEvents = events.stream().filter(event -> event.getFormSubmissionId().equals(formSubmissionId)).collect(toList());
            return Collections.max(groupedEvents, Comparator.comparing(Event::getEventDate));
        }).collect(toList());
    }

    private static int calculateDamagedPZQ(final List<Event> events) {
        return events.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(DRUGS) && val.getValue().equals(PZQ)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(DAMAGED_PZQ)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
    }

    private static int calculateDamagedMBZ(final List<Event> events) {
        return events.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(DRUGS) && val.getValue().equals(MBZ)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(DAMAGED_MBZ)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
    }

    private static int calculateDispensedPZQ(final List<Event> events) {
        return events.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(DRUGS) && val.getValue().equals(PZQ)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(ADMINSTERED)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
    }

    private static int calculateDispensedMBZ(final List<Event> events) {
        return events.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(DRUGS) && val.getValue().equals(MBZ)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(ADMINSTERED)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
    }

    private static int calculateRemainingPZQ(final List<Event> events, final List<Event> drugReceivedFormEvents, final List<Event> drugWithdrawalFormEvents) {
        return calculateReceivedPZQ(drugReceivedFormEvents) - (calculateDispensedPZQ(events) + calculateWithdrawnPZQ(drugWithdrawalFormEvents) + calculateDamagedPZQ(events));
    }

    private static int calculateRemainingMBZ(final List<Event> supervisionEvents, List<Event> drugReceivedFormEvents, List<Event> drugWithdrawalFormEvents) {
        int mbzAdministered = calculateDispensedMBZ(supervisionEvents);
        int mbzWithdrawn = calculateWithdrawnMBZ(drugWithdrawalFormEvents);
        int mbzReceived = calculateReceivedMBZ(drugReceivedFormEvents);
        int mbzDamaged = calculateDamagedMBZ(supervisionEvents);
        return mbzReceived - (mbzAdministered + mbzWithdrawn + mbzDamaged);
    }

    private static int calculateReceivedMBZ(final List<Event> drugReceivedFormEvents) {
        List<Obs> mbzReceivedEvents = drugReceivedFormEvents.stream().map(Event::getObs).filter(obs -> obs.stream().filter(value -> (value.getFieldCode().equals(DRUG_ISSUED) && value.getValue().equals(MBZ))).findAny().isPresent()).filter(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(MBZ_RECEIVED)).findFirst().isPresent()).flatMap(Collection::stream).collect(toList());
        return mbzReceivedEvents.stream().filter(obs -> obs.getFieldCode().equals(MBZ_RECEIVED)).map(obs -> Integer.parseInt(obs.getValue().toString())).reduce(0, Integer::sum);
    }

    private static int calculateReceivedPZQ(final List<Event> drugReceivedFormEvents) {
        return drugReceivedFormEvents.stream().map(Event::getObs).filter(obs -> obs.stream().filter(value -> (value.getFieldCode().equals(DRUG_ISSUED) && (value.getValue().equals(PZQ)))).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(PZQ_RECEIVED)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(x -> Integer.parseInt(x.toString())).sum();
    }

    private static int calculateWithdrawnMBZ(List<Event> events) {
        return events.stream().map(Event::getObs).filter(obs -> obs.stream().filter(value -> (value.getFieldCode().equals(DRUG_WITHDRAWN) && (value.getValue().equals(MBZ) || value.getValue().equals(BOTH)))).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(MBZ_WITHDRAWN)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(x -> Integer.parseInt(x.toString())).sum();
    }

    private static int calculateWithdrawnPZQ(List<Event> events) {
        return events.stream().map(Event::getObs).filter(obs -> obs.stream().filter(value -> (value.getFieldCode().equals(DRUG_WITHDRAWN) && (value.getValue().equals(PZQ) || value.getValue().equals(BOTH)))).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(PZQ_WITHDRAWN)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(x -> Integer.parseInt(x.toString())).sum();
    }

    private static int calculatePeopleTreatedForSCH(final List<Event> events) {
        return events.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(SCH)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(TOTAL_MALE_AND_FEMALE)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
    }

    private static int calculatePeopleTreatedForSTH(final List<Event> events) {
        return events.stream().map(Event::getObs).filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(STH)).findAny().isPresent()).map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(TOTAL_MALE_AND_FEMALE)).findFirst().get()).map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
    }

    private static int calculateDrugCompletion(List<TaskDetails> tasks) {
        boolean dispenseComplete = false;
        boolean adherenceComplete = false;

        for (TaskDetails task : tasks) {
            if (Constants.Intervention.MDA_DISPENSE.equals(task.getTaskCode()) && Constants.BusinessStatus.SMC_COMPLETE.equals(task.getBusinessStatus())) {
                dispenseComplete = true;
            } else if (Constants.Intervention.MDA_ADHERENCE.equals(task.getTaskCode()) && Constants.BusinessStatus.SPAQ_COMPLETE.equals(task.getBusinessStatus())) {
                adherenceComplete = true;
            }
        }
        return (dispenseComplete && adherenceComplete) ? 1 : 0;
    }

    private static int calculatePartialDrugDistribution(List<TaskDetails> tasks) {
        boolean dispenseComplete = false;
        boolean adherenceComplete = false;
        for (TaskDetails task : tasks) {
            if (Constants.Intervention.MDA_DISPENSE.equals(task.getTaskCode()) && Constants.BusinessStatus.SMC_COMPLETE.equals(task.getBusinessStatus())) {
                dispenseComplete = true;
            } else if (Constants.Intervention.MDA_ADHERENCE.equals(task.getTaskCode()) && Constants.BusinessStatus.SPAQ_COMPLETE.equals(task.getBusinessStatus())) {
                adherenceComplete = true;
            }
        }
        return (dispenseComplete && (!adherenceComplete)) ? 1 : 0;
    }

    private static int calculateChildrenEligible(List<TaskDetails> tasks) {
        int childrenEligible = 0;
        for (TaskDetails task : tasks) {
            if (task.getTaskCode().equals(Constants.Intervention.MDA_DISPENSE) && Constants.BusinessStatusWrapper.MDA_DISPENSE_ELIGIBLE_STATUS.contains(task.getBusinessStatus())) {
                childrenEligible++;
            }
        }
        return childrenEligible;
    }

    private static int calculateChildrenTreated(List<TaskDetails> tasks) {
        int treated = 0;
        boolean adherenceComplete = false;

        for (TaskDetails task : tasks) {
            if (Intervention.MDA_ADHERENCE.equals(task.getTaskCode()) && BusinessStatus.SPAQ_COMPLETE.equals(task.getBusinessStatus())) {
                adherenceComplete = true;
            }
            if (Constants.Intervention.MDA_DISPENSE.equals(task.getTaskCode()) && Constants.BusinessStatus.SMC_COMPLETE.equals(task.getBusinessStatus())) {
                treated++;
            }
        }
        return adherenceComplete ? treated : 0;
    }

    private static int calculateStructuresNotFamilyRegistered(List<TaskDetails> tasks) {
        int notVisited = 0;
        for (TaskDetails task : tasks) {
            if (task.getTaskCode().equals(Constants.Intervention.REGISTER_FAMILY) && Constants.BusinessStatus.NOT_VISITED.equals(task.getBusinessStatus())) {
                notVisited++;
                break;
            }
        }
        return notVisited;
    }
}



