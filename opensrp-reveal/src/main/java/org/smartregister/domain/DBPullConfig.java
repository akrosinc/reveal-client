package org.smartregister.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DBPullConfig  implements Comparable<PlanDefinition> , Serializable {

    private static final long serialVersionUID = 7928849685467336510L;

    @JsonProperty
    private String identifier;

    @JsonProperty
    private String userName;

    @Override
    public int compareTo(PlanDefinition o) {
        return 0;
    }
}
