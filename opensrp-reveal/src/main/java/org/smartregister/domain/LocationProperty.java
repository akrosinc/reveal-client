package org.smartregister.domain;


import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LocationProperty implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum PropertyStatus {
        ACTIVE,
        INACTIVE,
        PENDING_REVIEW,
        NOT_ELIGIBLE

    };

    private String uid;

    private String code;

    private String type;

    private PropertyStatus status;

    private String parentId;

    private String name;


    private String geographicLevel;

    private String structureNumber;

    private Date effectiveStartDate;

    private Date effectiveEndDate;

    private int version;

    private String username;

    private transient Map<String, String> customProperties = new HashMap<>();

    public String getUid() {
        return uid;
    }

    public String getStructureNumber() {
        return structureNumber;
    }

    public void setStructureNumber(final String structureNumber) {
        this.structureNumber = structureNumber;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PropertyStatus getStatus() {
        return status;
    }

    public void setStatus(PropertyStatus status) {
        this.status = status;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public void setEffectiveStartDate(Date effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }


    public String getGeographicLevel() {
        return geographicLevel;
    }

    public void setGeographicLevel(String geographicLevel) {
        this.geographicLevel = geographicLevel;
    }

    public Date getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public void setEffectiveEndDate(Date effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
