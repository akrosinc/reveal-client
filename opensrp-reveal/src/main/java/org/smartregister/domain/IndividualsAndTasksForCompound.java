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
public class IndividualsAndTasksForCompound implements Serializable {

    private String individualIdentifier;
    private String individualId;
    private String householdId;
    private String compoundId;
    private String taskId;
    private String businessStatus;
    private String code;
    private String status;
}
