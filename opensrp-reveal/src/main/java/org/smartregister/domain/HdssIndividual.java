package org.smartregister.domain;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
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
}
