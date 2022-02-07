package org.smartregister.domain;


import java.io.Serializable;
import java.util.Set;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by samuelgithengi on 4/29/19.
 */
@NoArgsConstructor
@Setter
@Getter
public class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ActionType {
        @SerializedName("create") CREATE,
        @SerializedName("update") UPDATE,
        @SerializedName("remove") REMOVE,
        @SerializedName("fire-event") FIRE_EVENT
    }

    private String identifier;

    private int prefix;

    private String title;

    private String description;

    private String code;

    private Period timingPeriod;

    private String reason;

    private String goalId;

    private SubjectConcept subjectCodableConcept;

    private String taskTemplate;

    private Set<Trigger> trigger;

    private Set<Condition> condition;

    private String definitionUri;

    private Set<DynamicValue> dynamicValue;

    private ActionType type = ActionType.CREATE;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class SubjectConcept implements Serializable {
        private String text;
    }
}
