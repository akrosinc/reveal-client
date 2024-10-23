package org.smartregister.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HdssCompoundHousehold implements Serializable {

    private String compoundId;
    private String householdId;
    private long serverVersion;
}
