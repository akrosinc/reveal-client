package org.smartregister.reveal.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class IndicatorDetails {

    private int sprayed;
    private int notSprayed;
    private int totalStructures;
    private int progress;
    private int notVisited;
    private int ineligible;
    private int foundStructures;
    private int surveyedStructures;
    private int roomCoverage;
    private List<String> sprayIndicatorList;
    private int target;

    //NIH Indicator
    private int complete;
    private int incomplete;
    private int notEligible;
    private int visited;
    private int total;
    private double visitedCoverage;
    private double completeCoverage;
    private double successRate;


    //RWANDA indicators
    private int healthEducatedChildren5To15;
    private int healthEducatedChildrenAbove16;
    private int vitaminTreatedChildren6To11Months;
    private int vitaminTreatedChildren12To59Months;
    private int albMebTreatedChildren12To59Months;
    private int albMebTreatedChildren5To15Years;
    private int albMebTreatedChildrenAbove16Years;
    private int pzqTreatedChildren5To15Years;
    private int pzqTreatedChildrenAbove16Years;

    //Nigeria Indicators

    private int completeDrugDistribution;
    private int partialDrugDistribution;
    private int totalIndividualTreated;
    private int childrenEligible;
    private int totalRcdStructures;
    private int totalRcdMemberTasks;
    private int totalIndexStructure;
    private int totalIndexMemberTasks;
    private int totalCompleteRcdStructures;
    private int totalCompleteRcdMemberTasks;
    private int totalCompleteIndexStructure;
    private int totalCompleteIndexMemberTasks;
    private int totalUnVisitRcdStructures;
    private int totalUnVisitIndexStructure;
    private int totalVisitIndexStructure;
    private int totalVisitRCDStructure;
    int indexStructureCoverage;
    int rcdStructureCoverage;
    int visitedGDRSCoverage;

    //Mali Indicators

    private int mdaTotalStructures;
    private int mdaComplete;
    private int mdaCompleteTotal;
    private int mdaDone;
    private int mdaPartiallyComplete;
    private int mdaRefusedOrAbsent;
    private int mdaNotVisited;
    private int mdaVisited;
    private int mdaNotEligible;
    private int mdaDistributionCoverage;
    private int mdaSuccessRate;
    private int mdaFoundCoverage;
    private double coverageOfStructuresCompleted;
    private double treatedOverVisited;
    private double visitedOverTotal;
    private int mdaTotalTreated;
    private int mdaTotalEligible;

    //Kenya indicators


    private int peopleTreatedForSTH;
    private int peopleTreatedForSCH;
    private int pzqTabletsRemaining;
    private int mbzTabletsRemaining;
    private int mbzDispensed;
    private int pzqDispensed;
    private int mbzDamaged;
    private int pzqDamaged;

    public String toMaliIndicatorString() {
        return "IndicatorDetails{" +
                "mdaTotalStructures=" + mdaTotalStructures +
                ", mdaComplete=" + mdaComplete +
                ", mdaPartiallyComplete=" + mdaPartiallyComplete +
                ", mdaRefusedOrAbsent=" + mdaRefusedOrAbsent +
                ", mdaNotVisited=" + mdaNotVisited +
                ", mdaNotEligible=" + mdaNotEligible +
                ", mdaDistributionCoverage=" + mdaDistributionCoverage +
                ", mdaSuccessRate=" + mdaSuccessRate +
                ", mdaFoundCoverage=" + mdaFoundCoverage +
                '}';
    }
}
