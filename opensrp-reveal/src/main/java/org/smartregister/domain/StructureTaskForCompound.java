package org.smartregister.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StructureTaskForCompound implements Serializable {

    private String structureId;
    private String householdId;
    private String compoundId;
    private String taskId;
    private String businessStatus;
    private String code;
    private String status;
}
