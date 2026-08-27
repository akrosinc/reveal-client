package org.smartregister.domain;


public enum SyncEntity {
    TASKS("Tasks"), EVENTS("Events"), LOCATIONS("Locations"),HDSS("hdss"),
    STRUCTURES("structures"), PLANS("Plans"),HDSS_OFFLINE("hdss offline"),HDSS_FILE("hdss file");
    private String value;

    SyncEntity(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
