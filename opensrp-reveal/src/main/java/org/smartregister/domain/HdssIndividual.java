package org.smartregister.domain;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HdssIndividual implements Serializable {
    private String identifier;
    private String individualId;
    private String dob;
    private String gender;
    private long serverVersion;
}
