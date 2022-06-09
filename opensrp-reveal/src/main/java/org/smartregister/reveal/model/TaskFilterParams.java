package org.smartregister.reveal.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class TaskFilterParams implements Serializable {

    private String sortBy;

    private String searchPhrase;

    private Date fromDate;

    private boolean viewAllEvents;

    @Builder.Default
    private Map<String, Set<String>> checkedFilters = new HashMap<>();


}
