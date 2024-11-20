package org.smartregister.domain;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class HdssIndividualHouseHoldCompound implements Serializable {
    private String identifier;
    private String individualId;
    private String dob;
    private String gender;
    private String householdId;
    private String compoundId;
    private int serverVersion;
    private String structureId;
    private String name;

    public HdssIndividualHouseHoldCompound(String identifier, String individualId, String dob, String gender, String householdId, String compoundId, String name) {
        this.identifier = identifier;
        this.individualId = individualId;
        this.dob = dob;
        this.gender = gender;
        this.householdId = householdId;
        this.compoundId = compoundId;
        this.name = name;
    }
}
