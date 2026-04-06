package org.smartregister.domain;


import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HdssIndividual implements Serializable {
    public HdssIndividual(String identifier, String individualId, String dob, String gender, String name, long serverVersion) {
        this.identifier = identifier;
        this.individualId = individualId;
        this.dob = dob;
        this.gender = gender;
        this.name = name;
        this.serverVersion = serverVersion;
    }

    private String identifier;
    private String individualId;
    private String dob;
    private String gender;
    private String name;
    private long serverVersion;
    private String floatingLocationId;
    private String floatingLocationName;
    private String floatingLocationGeographicLevel;
    private String cluster;

    public HdssIndividual(String identifier, String individualId, String dob, String gender,
        String name, long serverVersion, String floatingLocationId, String floatingLocationName,
        String floatingLocationGeographicLevel, String cluster) {
        this.identifier = identifier;
        this.individualId = individualId;
        this.dob = dob;
        this.gender = gender;
        this.name = name;
        this.serverVersion = serverVersion;
        this.floatingLocationId = floatingLocationId;
        this.floatingLocationName = floatingLocationName;
        this.floatingLocationGeographicLevel = floatingLocationGeographicLevel;
        this.cluster = cluster;
    }
}
