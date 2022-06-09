package org.smartregister.reveal.model;

import androidx.annotation.ArrayRes;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Data;


@Data
@Builder

public class FilterConfiguration implements Serializable {

    @Builder.Default
    private boolean businessStatusLayoutEnabled = true;

    @Builder.Default
    private boolean taskCodeLayoutEnabled = true;

    @Builder.Default
    private boolean interventionTypeLayoutEnabled = true;

    private boolean formsLayoutEnabled;

    private boolean filterFromDateAndViewAllEventsEnabled;

    private List<String> businessStatusList;

    private List<String> eventTypeList;

    @ArrayRes
    private Integer sortOptions;
}
