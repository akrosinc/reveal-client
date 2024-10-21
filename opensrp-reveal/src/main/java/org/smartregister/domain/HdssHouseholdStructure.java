package org.smartregister.domain;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HdssHouseholdStructure implements Serializable {
    private String householdId;
    private String structureId;
}
