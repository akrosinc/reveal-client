package org.smartregister.domain;


import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;
import org.smartregister.reveal.model.PersonRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
public class Task implements Serializable {

    private static final long serialVersionUID = -9118755114172291102L;

    public enum TaskStatus {

        @SerializedName("Draft")
        DRAFT,
        @SerializedName("Ready")
        READY,
        @SerializedName("Cancelled")
        CANCELLED,
        @SerializedName("In Progress")
        IN_PROGRESS,
        @SerializedName("Completed")
        COMPLETED,
        @SerializedName("Failed")
        FAILED,
        @SerializedName("Archived")
        ARCHIVED;

        private static final Map<String, TaskStatus> lookup = new HashMap<String, TaskStatus>();

        static {
            lookup.put("Draft", TaskStatus.DRAFT);
            lookup.put("Ready", TaskStatus.READY);
            lookup.put("Cancelled", TaskStatus.CANCELLED);
            lookup.put("In Progress", TaskStatus.IN_PROGRESS);
            lookup.put("Completed", TaskStatus.COMPLETED);
            lookup.put("Failed", TaskStatus.FAILED);
            lookup.put("Archived", TaskStatus.ARCHIVED);
        }

        public static TaskStatus get(String algorithm) {
            return lookup.get(algorithm);
        }
    }

    public enum TaskPriority {

        @SerializedName("stat")
        STAT,
        @SerializedName("asap")
        ASAP,
        @SerializedName("urgent")
        URGENT,
        @SerializedName("routine")
        ROUTINE;

        public static TaskPriority get(String priority) {
            if (priority == null)
                throw new IllegalArgumentException("Value is required");
            switch (priority) {
                case "routine":
                    return ROUTINE;
                case "urgent":
                    return URGENT;
                case "asap":
                    return ASAP;
                case "stat":
                    return STAT;
                default:
                    throw new IllegalArgumentException("Not a valid Task priority");
            }

        }
    }

    public static final String[] INACTIVE_TASK_STATUS = new String[] { TaskStatus.CANCELLED.name(),
            TaskStatus.ARCHIVED.name() };

    private String identifier;

    private String planIdentifier;

    private String groupIdentifier;

    private TaskStatus status;

    private String businessStatus;

    private TaskPriority priority;

    private String code;

    private String description;

    private String focus;

    @SerializedName("for")
    private String forEntity;

    private PersonRequest personRequest;

    private Period executionPeriod;

    private DateTime authoredOn;

    private DateTime lastModified;

    private String owner;

    @SerializedName("note")
    private List<Note> notes;

    private long serverVersion;

    private String reasonReference;

    private String location;

    private String requester;

    private String syncStatus;

    private String structureId;

    private Long rowid;

    private Restriction restriction;

    public Long getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(Long serverVersion) {
        this.serverVersion = serverVersion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Restriction {

        private int repetitions;

        private Period period;
    }
}
