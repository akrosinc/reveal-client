package org.smartregister.reveal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public  class PersonName {

    private String use;
    private String text;
    private String family;
    private String given;
    private String prefix;
    private String suffix;
}
