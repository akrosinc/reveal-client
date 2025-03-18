package org.smartregister.domain;


import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HdssCompoundObj implements Serializable {

    List<HdssCompound> allCompounds;

    List<HdssCompoundHousehold> compoundHouseHolds;

    List<HdssHouseholdIndividual> allHouseholdIndividual;

    List<HdssHouseholdStructure> allHouseholdStructure;

    List<HdssIndividual> allIndividuals;

    List<HdssHousehold> allHouseholds;

    List<String> allHouseholdIndividualToDelete;

    List<String> allCompoundHouseholdToDelete;

    boolean empty;

    long serverVersion;

    int totalRecords;

    public boolean getEmpty(){
        return empty;
    }

}
