package org.smartregister.reveal.util;

import android.content.Context;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import org.joda.time.DateTime;
import org.smartregister.domain.Event;
import org.smartregister.domain.Task;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.model.IndicatorDetails;
import org.smartregister.reveal.model.TaskDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.smartregister.reveal.util.Constants.BusinessStatus;
import org.smartregister.reveal.util.Constants.Intervention;
import timber.log.Timber;

import static org.smartregister.reveal.util.Constants.BusinessStatus.COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.IN_PROGRESS;
import static org.smartregister.reveal.util.Constants.Intervention.CELL_COORDINATION;
import static org.smartregister.reveal.util.Constants.JsonForm.HEALTH_EDUCATION_5_TO_15;
import static org.smartregister.reveal.util.Constants.JsonForm.HEALTH_EDUCATION_ABOVE_16;
import static org.smartregister.reveal.util.Constants.JsonForm.SUM_TREATED_1_TO_4;
import static org.smartregister.reveal.util.Constants.JsonForm.SUM_TREATED_5_TO_15;
import static org.smartregister.reveal.util.Constants.JsonForm.SUM_TREATED_6_TO_11_MOS;
import static org.smartregister.reveal.util.Constants.JsonForm.SUM_TREATED_ABOVE_16;

/**
 * Created by ndegwamartin on 2019-09-27.
 */
public class IndicatorUtils {


    public static final String VITAMIN_A = "Vitamin A";
    public static final String ALB_MEB = "ALB/MEB";
    public static final String PZQ = "PZQ";
    public static final String NTD_TREATED = "ntd_treated";

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

    public static IndicatorDetails getNamibiaIndicators(String locationId, String planId, SQLiteDatabase sqLiteDatabase) {
        String query = "select" +
                " sum(ifNull(ss.nSprayableTotal,0)) as foundStruct" +
                " ,sum(ifNull(ss.nSprayedTotalFirst,0)+ifNull(ss.nSprayedTotalMop,0)) as sprayedStruct" +
                " ,sum(ifNull(nSprayableTotal,0)- ifNull(nSprayedTotalFirst,0) - ifnull(nSprayedTotalMop,0)) as notSprayedStruct" +
                " from sprayed_structures ss " +
                " join structure s on s._id=ss.id" +
                " where parent_id=? and ss.plan_id=?";
        IndicatorDetails indicatorDetails = new IndicatorDetails();
        try (Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{locationId, planId})) {
            if (cursor.moveToNext()) {
                indicatorDetails.setFoundStructures(cursor.getInt(cursor.getColumnIndex("foundStruct")));
                indicatorDetails.setSprayed(cursor.getInt(cursor.getColumnIndex("sprayedStruct")));
                indicatorDetails.setNotSprayed(cursor.getInt(cursor.getColumnIndex("notSprayedStruct")));
            }
        } catch (SQLiteException e) {
            Timber.e(e);
        }
        return indicatorDetails;
    }

    public static IndicatorDetails processRwandaIndicators(List<TaskDetails> tasks){
        IndicatorDetails indicatorDetails = new IndicatorDetails();
        Integer value;

        List<TaskDetails> validTasks = tasks.stream()
                .filter(taskDetails -> taskDetails.getTaskCode().equals(CELL_COORDINATION) && (taskDetails.getBusinessStatus().equals(IN_PROGRESS) || taskDetails.getBusinessStatus().equals(COMPLETE)))
                .collect(Collectors.toList());

       Set<String> taskIdentifiers = validTasks.stream().map(taskDetails -> taskDetails.getTaskId())
                                               .collect(Collectors.toSet());

        EventClientRepository eventClientRepository = RevealApplication.getInstance().getContext().getEventClientRepository();

        List<Event> dataCaptured = eventClientRepository.getEventsByTaskIds(taskIdentifiers);

        List<Event> latestEvents = validTasks.stream().map(taskDetails -> {
            Map<DateTime,Event> eachTaskEventAndDateMap = dataCaptured.stream().filter(event -> taskDetails.getTaskId().equals(event.getDetails().getOrDefault("taskIdentifier","empty"))).collect(Collectors.toMap(Event::getDateCreated,Function.identity()));
            List<DateTime> eventDates = eachTaskEventAndDateMap.keySet().stream().collect(Collectors.toList());
            DateTime maxDatTime = Collections.max(eventDates);
            return eachTaskEventAndDateMap.get(maxDatTime);
        }).collect(Collectors.toList());

        value  = latestEvents.stream().map(Event::getObs)
                                              .map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(HEALTH_EDUCATION_5_TO_15)).findFirst().get())
                                              .map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setHealthEducatedChildren5To15(value);

        value = latestEvents.stream().map(Event::getObs)
                .map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(HEALTH_EDUCATION_ABOVE_16)).findFirst().get())
                .map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setHealthEducatedChildrenAbove16(value);

        value = latestEvents.stream().map(Event::getObs)
                .filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(VITAMIN_A)).findAny().isPresent())
                .map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_6_TO_11_MOS)).findFirst().get())
                .map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setVitaminTreatedChildren6To11Months(value);

        value = latestEvents.stream().map(Event::getObs)
                .filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(VITAMIN_A)).findAny().isPresent())
                .map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_1_TO_4)).findFirst().get())
                .map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setVitaminTreatedChildren12To59Months(value);

        value = latestEvents.stream().map(Event::getObs)
                .filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(ALB_MEB)).findAny().isPresent())
                .map(obs -> obs.stream().filter(obsValue -> (obsValue.getFieldCode().equals(SUM_TREATED_1_TO_4))).findFirst().get())
                .map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setAlbMebTreatedChildren12To59Months(value);

        value = latestEvents.stream().map(Event::getObs)
                .filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(ALB_MEB)).findAny().isPresent())
                .map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_5_TO_15)).findFirst().get())
                .map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setAlbMebTreatedChildren5To15Years(value);

        value = latestEvents.stream().map(Event::getObs)
                .filter(obs -> obs.stream().filter(val -> val.getFieldCode().equals(NTD_TREATED) && val.getValue().equals(PZQ)).findAny().isPresent())
                .map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_5_TO_15)).findFirst().get())
                .map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setPzqTreatedChildren5To15Years(value);

        value = latestEvents.stream().map(Event::getObs)
                .filter(obs -> obs.stream().filter(val -> val.getValue().equals(ALB_MEB)).findAny().isPresent())
                .map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_ABOVE_16)).findFirst().get())
                .map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setAlbMebTreatedChildrenAbove16Years(value);

        value = latestEvents.stream().map(Event::getObs)
                .filter(obs -> obs.stream().filter(val -> val.getValue().equals(PZQ)).findAny().isPresent())
                .map(obs -> obs.stream().filter(obsValue -> obsValue.getFieldCode().equals(SUM_TREATED_ABOVE_16)).findFirst().get())
                .map(obs -> obs.getValue()).mapToInt(val -> Integer.parseInt(val.toString())).sum();
        indicatorDetails.setPzqTreatedChildrenAbove16Years(value);


        return indicatorDetails;
    }

    public static List<String> populateRwandaIndicators(Context context, IndicatorDetails indicatorDetails){
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

        return  indicators;
    }

    public static List<String> populateNigeriaIndicators(Context context,IndicatorDetails indicatorDetails){
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

    public static IndicatorDetails processIndicatorsNigeria(List<TaskDetails> tasks) {
        Map<String, List<TaskDetails>> indicatorDetailsMap = new HashMap<>();
        IndicatorDetails indicatorDetails = new IndicatorDetails();
        if (tasks != null) {

            for (int i = 0; i < tasks.size(); i++) {
                    String structureId = tasks.get(i).getStructureId();
                    List<TaskDetails> taskDetails = indicatorDetailsMap.get(structureId);
                    if(taskDetails == null){
                        taskDetails  = new ArrayList<>();
                    }
                    taskDetails.add(tasks.get(i));
                    indicatorDetailsMap.put(tasks.get(i).getStructureId(),taskDetails);
            }

            for(Map.Entry<String,List<TaskDetails>> entry : indicatorDetailsMap.entrySet()){
                for(TaskDetails task: entry.getValue()){
                    if(Constants.BusinessStatusWrapper.NOT_ELIGIBLE.contains(task.getBusinessStatus())){
                        indicatorDetails.setIneligible(indicatorDetails.getIneligible() + 1);
                        break;
                    }
                    if(task.getTaskCode().equals(Constants.Intervention.MDA_DISPENSE)  && Constants.BusinessStatusWrapper.MDA_DISPENSE_ELIGIBLE_STATUS.contains(task.getBusinessStatus())) {
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


    private static int calculateDrugCompletion(List<TaskDetails> tasks ) {
        boolean dispenseComplete = false;
        boolean adherenceComplete = false;

        for(TaskDetails task : tasks){
            if(Constants.Intervention.MDA_DISPENSE.equals(task.getTaskCode()) && Constants.BusinessStatus.SMC_COMPLETE.equals(task.getBusinessStatus())){
                dispenseComplete = true;
            } else if(Constants.Intervention.MDA_ADHERENCE.equals(task.getTaskCode()) && Constants.BusinessStatus.SPAQ_COMPLETE.equals(task.getBusinessStatus())){
                adherenceComplete = true;
            }
        }
        return (dispenseComplete && adherenceComplete) ? 1 : 0;
    }

    private  static int calculatePartialDrugDistribution(List<TaskDetails> tasks){
        boolean dispenseComplete = false;
        boolean adherenceComplete = false;
        for(TaskDetails task : tasks){
            if(Constants.Intervention.MDA_DISPENSE.equals(task.getTaskCode()) && Constants.BusinessStatus.SMC_COMPLETE.equals(task.getBusinessStatus())){
                dispenseComplete = true;
            }else if(Constants.Intervention.MDA_ADHERENCE.equals(task.getTaskCode()) && Constants.BusinessStatus.SPAQ_COMPLETE.equals(task.getBusinessStatus())){
                adherenceComplete = true;
            }
        }
        return (dispenseComplete && (!adherenceComplete)) ? 1 : 0;
    }

    private static int calculateChildrenEligible(List<TaskDetails> tasks){
        int childrenEligible = 0;
        for(TaskDetails task: tasks){
            if(task.getTaskCode().equals(Constants.Intervention.MDA_DISPENSE)  && Constants.BusinessStatusWrapper.MDA_DISPENSE_ELIGIBLE_STATUS.contains(task.getBusinessStatus())){
                childrenEligible++;
            }
        }
        return childrenEligible;
    }

    private static int calculateChildrenTreated(List<TaskDetails> tasks) {
        int treated = 0;
        boolean adherenceComplete = false;

        for(TaskDetails task : tasks){
            if(Intervention.MDA_ADHERENCE.equals(task.getTaskCode()) && BusinessStatus.SPAQ_COMPLETE.equals(task.getBusinessStatus())) {
                adherenceComplete = true;
            }
            if(Constants.Intervention.MDA_DISPENSE.equals(task.getTaskCode()) && Constants.BusinessStatus.SMC_COMPLETE.equals(task.getBusinessStatus())){
                treated++;
            }
        }
        return adherenceComplete ? treated : 0;
    }

    private static int calculateStructuresNotFamilyRegistered(List<TaskDetails> tasks){
        int notVisited = 0;
        for(TaskDetails task : tasks){
            if(task.getTaskCode().equals(Constants.Intervention.REGISTER_FAMILY) && Constants.BusinessStatus.NOT_VISITED.equals(task.getBusinessStatus())){
                notVisited++;
                break;
            }
        }
        return notVisited;
    }
}



