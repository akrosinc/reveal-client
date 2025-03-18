package org.smartregister.domain;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HdssHousehold implements Serializable {
    private String householdId;
    private String floatingHouseholdLocationName;
    private long serverVersion;
}