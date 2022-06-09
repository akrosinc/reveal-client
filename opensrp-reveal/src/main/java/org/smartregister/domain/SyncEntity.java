package org.smartregister.domain;


public enum SyncEntity {
    TASKS("Tasks"), EVENTS("Events"), LOCATIONS("Locations"),
    STRUCTURES("structures"), PLANS("Plans");
    private String value;

    SyncEntity(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
