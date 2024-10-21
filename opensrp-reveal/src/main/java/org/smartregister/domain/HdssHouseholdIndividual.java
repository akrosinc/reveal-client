package org.smartregister.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HdssHouseholdIndividual implements Serializable {

    private String householdId;
    private String individualId;
}
