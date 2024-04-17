package org.smartregister.reveal.model;

import java.util.List;


public class IndicatorDetails {

    private int sprayed;
    private int notSprayed;
    private int totalStructures;
    private int progress;
    private int notVisited;
    private int ineligible;
    private int foundStructures;
    private int roomCoverage;
    private List<String> sprayIndicatorList;
    private int target;
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


    //Mali Indicators
    private int mdaTotalStructures;

    public int getMdaComplete() {
        return mdaComplete;
    }

    public int getMdaPartiallyComplete() {
        return mdaPartiallyComplete;
    }

    public int getMdaRefusedOrAbsent() {
        return mdaRefusedOrAbsent;
    }

    public int getMdaNotVisited() {
        return mdaNotVisited;
    }

    public int getMdaNotEligible() {
        return mdaNotEligible;
    }

    public int getMdaDistributionCoverage() {
        return mdaDistributionCoverage;
    }

    public int getMdaSuccessRate() {
        return mdaSuccessRate;
    }

    public int getMdaFoundCoverage() {
        return mdaFoundCoverage;
    }

    private int mdaComplete;
    private int mdaPartiallyComplete;
    private int mdaRefusedOrAbsent;
    private int mdaNotVisited;
    private int mdaNotEligible;
    private int mdaDistributionCoverage;
    private int mdaSuccessRate;
    private int mdaFoundCoverage;

    private int mdaTotalTreated;

    public int getMdaTotalTreated() {
        return mdaTotalTreated;
    }

    public void setMdaTotalTreated(int mdaTotalTreated) {
        this.mdaTotalTreated = mdaTotalTreated;
    }

    public int getMdaTotalEligible() {
        return mdaTotalEligible;
    }

    public void setMdaTotalEligible(int mdaTotalEligible) {
        this.mdaTotalEligible = mdaTotalEligible;
    }

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


    public void setMdaTotalStructures(int mdaTotalStructures) {
        this.mdaTotalStructures = mdaTotalStructures;
    }

    public int getMdaTotalStructures() {
        return mdaTotalStructures;
    }
    public void setMdaComplete(int mdaComplete) {
        this.mdaComplete = mdaComplete;
    }

    public void setMdaPartiallyComplete(int mdaPartiallyComplete) {
        this.mdaPartiallyComplete = mdaPartiallyComplete;
    }

    public void setMdaRefusedOrAbsent(int mdaRefusedOrAbsent) {
        this.mdaRefusedOrAbsent = mdaRefusedOrAbsent;
    }

    public void setMdaNotVisited(int mdaNotVisited) {
        this.mdaNotVisited = mdaNotVisited;
    }

    public void setMdaNotEligible(int mdaNotEligible) {
        this.mdaNotEligible = mdaNotEligible;
    }

    public void setMdaDistributionCoverage(int mdaDistributionCoverage) {
        this.mdaDistributionCoverage = mdaDistributionCoverage;
    }
    public void setMdaSuccessRate(int mdaSuccessRate) {
        this.mdaSuccessRate = mdaSuccessRate;
    }

    public void setMdaFoundCoverage(int mdaFoundCoverage) {
        this.mdaFoundCoverage = mdaFoundCoverage;
    }
    public int getSprayed() {
        return sprayed;
    }

    public void setSprayed(int sprayed) {
        this.sprayed = sprayed;
    }

    public int getNotSprayed() {
        return notSprayed;
    }

    public void setNotSprayed(int notSprayed) {
        this.notSprayed = notSprayed;
    }

    public int getTotalStructures() {
        return totalStructures;
    }

    public void setTotalStructures(int totalStructures) {
        this.totalStructures = totalStructures;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getNotVisited() {
        return notVisited;
    }

    public void setNotVisited(int notVisited) {
        this.notVisited = notVisited;
    }

    public List<String> getSprayIndicatorList() {
        return sprayIndicatorList;
    }

    public void setSprayIndicatorList(List<String> sprayIndicatorList) {
        this.sprayIndicatorList = sprayIndicatorList;
    }

    public int getIneligible() {
        return ineligible;
    }

    public void setIneligible(int ineligible) {
        this.ineligible = ineligible;
    }

    public int getFoundStructures() {
        return foundStructures;
    }

    public void setFoundStructures(int foundStructures) {
        this.foundStructures = foundStructures;
    }

    public int getRoomCoverage() {
        return roomCoverage;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void setRoomCoverage(int roomCoverage) {
        this.roomCoverage = roomCoverage;
    }

    public int getHealthEducatedChildren5To15() {
        return healthEducatedChildren5To15;
    }

    public void setHealthEducatedChildren5To15(int healthEducatedChildren5To15) {
        this.healthEducatedChildren5To15 = healthEducatedChildren5To15;
    }

    public int getHealthEducatedChildrenAbove16() {
        return healthEducatedChildrenAbove16;
    }

    public void setHealthEducatedChildrenAbove16(int healthEducatedChildrenAbove16) {
        this.healthEducatedChildrenAbove16 = healthEducatedChildrenAbove16;
    }

    public int getVitaminTreatedChildren6To11Months() {
        return vitaminTreatedChildren6To11Months;
    }

    public void setVitaminTreatedChildren6To11Months(int vitaminTreatedChildren6To11Months) {
        this.vitaminTreatedChildren6To11Months = vitaminTreatedChildren6To11Months;
    }

    public int getVitaminTreatedChildren12To59Months() {
        return vitaminTreatedChildren12To59Months;
    }

    public void setVitaminTreatedChildren12To59Months(int vitaminTreatedChildren12To59Months) {
        this.vitaminTreatedChildren12To59Months = vitaminTreatedChildren12To59Months;
    }

    public int getAlbMebTreatedChildren12To59Months() {
        return albMebTreatedChildren12To59Months;
    }

    public void setAlbMebTreatedChildren12To59Months(int albMebTreatedChildren12To59Months) {
        this.albMebTreatedChildren12To59Months = albMebTreatedChildren12To59Months;
    }

    public int getAlbMebTreatedChildren5To15Years() {
        return albMebTreatedChildren5To15Years;
    }

    public void setAlbMebTreatedChildren5To15Years(int albMebTreatedChildren5To15Years) {
        this.albMebTreatedChildren5To15Years = albMebTreatedChildren5To15Years;
    }

    public int getAlbMebTreatedChildrenAbove16Years() {
        return albMebTreatedChildrenAbove16Years;
    }

    public void setAlbMebTreatedChildrenAbove16Years(int albMebTreatedChildrenAbove16Years) {
        this.albMebTreatedChildrenAbove16Years = albMebTreatedChildrenAbove16Years;
    }

    public int getPzqTreatedChildren5To15Years() {
        return pzqTreatedChildren5To15Years;
    }

    public void setPzqTreatedChildren5To15Years(int pzqTreatedChildren5To15Years) {
        this.pzqTreatedChildren5To15Years = pzqTreatedChildren5To15Years;
    }

    public int getPzqTreatedChildrenAbove16Years() {
        return pzqTreatedChildrenAbove16Years;
    }

    public void setPzqTreatedChildrenAbove16Years(int pzqTreatedChildrenAbove16Years) {
        this.pzqTreatedChildrenAbove16Years = pzqTreatedChildrenAbove16Years;
    }


    public int getCompleteDrugDistribution() {
        return completeDrugDistribution;
    }

    public void setCompleteDrugDistribution(int completeDrugDistribution) {
        this.completeDrugDistribution = completeDrugDistribution;
    }

    public int getPartialDrugDistribution() {
        return partialDrugDistribution;
    }

    public void setPartialDrugDistribution(int partialDrugDistribution) {
        this.partialDrugDistribution = partialDrugDistribution;
    }

    public int getTotalIndividualTreated() {
        return totalIndividualTreated;
    }

    public void setTotalIndividualTreated(int totalIndividualTreated) {
        this.totalIndividualTreated = totalIndividualTreated;
    }

    public int getChildrenEligible() {
        return childrenEligible;
    }

    public void setChildrenEligible(int childrenEligible) {
        this.childrenEligible = childrenEligible;
    }

    public int getPeopleTreatedForSTH() {
        return peopleTreatedForSTH;
    }

    public void setPeopleTreatedForSTH(final int peopleTreatedForSTH) {
        this.peopleTreatedForSTH = peopleTreatedForSTH;
    }

    public int getPeopleTreatedForSCH() {
        return peopleTreatedForSCH;
    }

    public void setPeopleTreatedForSCH(final int peopleTreatedForSCH) {
        this.peopleTreatedForSCH = peopleTreatedForSCH;
    }

    public int getPzqTabletsRemaining() {
        return pzqTabletsRemaining;
    }

    public void setPzqTabletsRemaining(final int pzqTabletsRemaining) {
        this.pzqTabletsRemaining = pzqTabletsRemaining;
    }

    public int getMbzTabletsRemaining() {
        return mbzTabletsRemaining;
    }

    public void setMbzTabletsRemaining(final int mbzTabletsRemaining) {
        this.mbzTabletsRemaining = mbzTabletsRemaining;
    }

    public int getMbzDispensed() {
        return mbzDispensed;
    }

    public void setMbzDispensed(final int mbzDispensed) {
        this.mbzDispensed = mbzDispensed;
    }

    public int getPzqDispensed() {
        return pzqDispensed;
    }

    public void setPzqDispensed(final int pzqDispensed) {
        this.pzqDispensed = pzqDispensed;
    }

    public int getMbzDamaged() {
        return mbzDamaged;
    }

    public void setMbzDamaged(final int mbzDamaged) {
        this.mbzDamaged = mbzDamaged;
    }

    public int getPzqDamaged() {
        return pzqDamaged;
    }

    public void setPzqDamaged(final int pzqDamaged) {
        this.pzqDamaged = pzqDamaged;
    }

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
