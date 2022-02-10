package org.smartregister.domain;


import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

/**
 * Created by samuelgithengi on 4/29/19.
 */
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
    @SerializedName("goal")
    private List<Goal> goals;

    @JsonProperty
    @SerializedName("action")
    private List<Action> actions;

    @JsonProperty
    private boolean experimental;

    @JsonProperty
    private InterventionType interventionType;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Period getEffectivePeriod() {
        return effectivePeriod;
    }

    public void setEffectivePeriod(Period effectivePeriod) {
        this.effectivePeriod = effectivePeriod;
    }

    public List<UseContext> getUseContext() {
        return useContext;
    }

    public void setUseContext(List<UseContext> useContext) {
        this.useContext = useContext;
    }

    public List<Jurisdiction> getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(List<Jurisdiction> jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public Long getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(Long serverVersion) {
        this.serverVersion = serverVersion;
    }

    public boolean isExperimental() {
        return experimental;
    }

    public void setExperimental(boolean experimental) {
        this.experimental = experimental;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InterventionType getInterventionType() {
        return interventionType;
    }

    public void setInterventionType(InterventionType interventionType) {
        this.interventionType = interventionType;
    }

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

    public static  class InterventionType implements Serializable {
        private UUID identifier;
        private String name;
        private String code;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public UUID getIdentifier() {
            return identifier;
        }

        public void setIdentifier(UUID identifier) {
            this.identifier = identifier;
        }
    }


    public enum PlanStatus {
        DRAFT,
        ACTIVE,
        RETIRED,
        COMPLETED,
        UNKNOWN
    }
//    public enum PlanStatus {
//
//        /**
//         * Draft
//         * <p>
//         * This resource is still under development and is not yet considered to be ready for normal
//         * use.
//         */
//        @SerializedName("draft")
//        DRAFT("draft"),
//
//        /**
//         * Active
//         * <p>
//         * This resource is ready for normal use.
//         */
//        @SerializedName("active")
//        ACTIVE("active"),
//
//        /**
//         * Retired
//         * <p>
//         * This resource has been withdrawn or superseded and should no longer be used.
//         */
//        @SerializedName("retired")
//        RETIRED("retired"),
//
//        /**
//         * Completed
//         * <p>
//         * This resource is completed for normal use.
//         */
//        @SerializedName("complete")
//        COMPLETED("complete"),
//
//        /**
//         * Unknown
//         * <p>
//         * The authoring system does not know which of the status values currently applies for this
//         * resource. Note: This concept is not to be used for "other" - one of the listed statuses
//         * is presumed to apply, it's just not known which one.
//         */
//        @SerializedName("unknown")
//        UNKNOWN("unknown");
//
//        private final String value;
//
//        PlanStatus(String value) {
//            this.value = value;
//        }
//
//        public String value() {
//            return value;
//        }
//
//        public static PlanStatus from(String value) {
//            for (PlanStatus c : PlanStatus.values()) {
//                if (c.value.equals(value)) {
//                    return c;
//                }
//            }
//            throw new IllegalArgumentException(value);
//        }
//    }
}
