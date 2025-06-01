package org.smartregister.domain;


import java.io.Serializable;

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

    private String cluster;
    private String floatingLocationId;
    private String floatingLocationName;
    private String floatingLocationGeographicLevel;

    private String floatingHouseholdLocationName;

    public HdssIndividualHouseHoldCompound(String identifier, String individualId, String dob, String gender, String householdId, String compoundId, String name, String cluster) {
        this.identifier = identifier;
        this.individualId = individualId;
        this.dob = dob;
        this.gender = gender;
        this.householdId = householdId;
        this.compoundId = compoundId;
        this.name = name;
        this.cluster = cluster;
    }

    public HdssIndividualHouseHoldCompound(String identifier, String individualId, String dob, String gender, String householdId, String compoundId, String structureId, int serverVersion, String name, String floatingLocationId, String floatingLocationName, String floatingLocationGeographicLevel, String floatingHouseholdLocationName, String cluster) {
        this.identifier = identifier;
        this.individualId = individualId;
        this.dob = dob;
        this.gender = gender;
        this.householdId = householdId;
        this.compoundId = compoundId;
        this.name = name;

        this.serverVersion = serverVersion;


        this.structureId = structureId;

        this.floatingLocationId = floatingLocationId;
        this.floatingLocationName = floatingLocationName;
        this.floatingLocationGeographicLevel = floatingLocationGeographicLevel;

        this.floatingHouseholdLocationName = floatingHouseholdLocationName;
        this.cluster = cluster;
    }

//    public HdssIndividualHouseHoldCompound(String identifier, String individualId, String dob, String gender, String householdId, String compoundId, String name) {
//        this.identifier = identifier;
//        this.individualId = individualId;
//        this.dob = dob;
//        this.gender = gender;
//        this.householdId = householdId;
//        this.compoundId = compoundId;
//        this.name = name;
//    }
}
