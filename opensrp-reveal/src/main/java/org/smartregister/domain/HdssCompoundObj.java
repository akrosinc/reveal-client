package org.smartregister.domain;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HdssCompoundObj implements Serializable {

    @Builder.Default
    private List<HdssCompound> allCompounds = new ArrayList<>();

    @Builder.Default
    private List<HdssCompoundHousehold> compoundHouseHolds = new ArrayList<>();

    @Builder.Default
    private List<HdssHouseholdIndividual> allHouseholdIndividual = new ArrayList<>();

    @Builder.Default
    private List<HdssHouseholdStructure> allHouseholdStructure = new ArrayList<>();

    @Builder.Default
    private List<HdssIndividual> allIndividuals = new ArrayList<>();

    @Builder.Default
    private List<HdssHousehold> allHouseholds = new ArrayList<>();

    @Builder.Default
    private List<String> allHouseholdIndividualToDelete = new ArrayList<>();

    @Builder.Default
    private List<String> allCompoundHouseholdToDelete = new ArrayList<>();

    @Builder.Default
    private boolean empty = true;

    @Builder.Default
    private long serverVersion = 0L;

    @Builder.Default
    private int totalRecords = 0;
    public boolean getEmpty(){
        return empty;
    }

}
