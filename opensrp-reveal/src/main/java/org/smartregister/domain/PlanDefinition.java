package org.smartregister.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.LocalDate;

@Getter
@Setter
public class PlanDefinition implements Comparable<PlanDefinition> , Serializable {

    private static final long serialVersionUID = 7928849685467336510L;

    @JsonProperty
    private String identifier;

    @JsonProperty
    private String description;

    @JsonProperty
    private String version;

    @JsonProperty
    private String name;

    @JsonProperty
    private String title;

    @JsonProperty
    private PlanStatus status;

    @JsonProperty
    private LocalDate date;

    @JsonProperty
    private Period effectivePeriod;

    @JsonProperty
    private List<UseContext> useContext;

    @JsonProperty
    private List<Jurisdiction> jurisdiction;

    private Long serverVersion;

    @JsonProperty
    private List<Goal> goals;

    @JsonProperty
    private List<Action> actions;

    @JsonProperty
    private boolean experimental;

    @JsonProperty
    private String targetGeographicLevel;

    @JsonProperty
    private List<String> hierarchyGeographicLevels;

    @Override
    public int compareTo(PlanDefinition o) {
        return getName().equals(o.getName()) ? getName().compareTo(o.getIdentifier()) : getName().compareTo(o.getName());
    }

    public static class UseContext implements Serializable {

        private String code;

        private String valueCodableConcept;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getValueCodableConcept() {
            return valueCodableConcept;
        }

        public void setValueCodableConcept(String valueCodableConcept) {
            this.valueCodableConcept = valueCodableConcept;
        }
    }

    public enum PlanStatus {
        DRAFT,
        ACTIVE,
        RETIRED,
        COMPLETED,
        UNKNOWN
    }
}
