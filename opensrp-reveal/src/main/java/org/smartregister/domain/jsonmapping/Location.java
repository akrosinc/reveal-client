package org.smartregister.domain.jsonmapping;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.smartregister.domain.Address;
import org.smartregister.domain.BaseDataObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The registered teamLocation which needs to be identified uniquely across the system
 */
public class Location extends BaseDataObject {

    private String locationId;
    private String name;
    private Address address;
    private Map<String, String> identifiers;
    private Location parentLocation;
    private Set<String> tags;
    private Map<String, Object> attributes;
    private Set<String> planIds;

    public Location() {
    }

    public Location(String locationId, String name, Address address, Location parentLocation) {
        this.locationId = locationId;
        this.name = name;
        this.address = address;
        this.parentLocation = parentLocation;
    }

    public Location(String locationId, String name, Address address,
                    Map<String, String> identifiers, Location parentLocation,
                    Set<String> tags, Map<String, Object> attributes) {
        this.locationId = locationId;
        this.name = name;
        this.address = address;
        this.identifiers = identifiers;
        this.parentLocation = parentLocation;
        this.tags = tags;
        this.attributes = attributes;
    }

    public Set<String> getPlanIds() {
        return planIds;
    }

    public void setPlanIds(final Set<String> planIds) {
        this.planIds = planIds;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Map<String, String> getIdentifiers() {
        return identifiers;
    }

    public String getIdentifier(String identifierType) {
        return identifiers.get(identifierType);
    }

    /**
     * WARNING: Overrides all existing identifiers
     *
     * @param identifiers
     * @return
     */
    public void setIdentifiers(Map<String, String> identifiers) {
        this.identifiers = identifiers;
    }

    public void addIdentifier(String identifierType, String identifier) {
        if (identifiers == null) {
            identifiers = new HashMap<>();
        }

        identifiers.put(identifierType, identifier);
    }

    public void removeIdentifier(String identifierType) {
        identifiers.remove(identifierType);
    }

    public Location getParentLocation() {
        return parentLocation;
    }

    public void setParentLocation(Location parentLocation) {
        this.parentLocation = parentLocation;
    }

    public Set<String> getTags() {
        return tags;
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    /**
     * WARNING: Overrides all existing tags
     *
     * @param tags
     * @return
     */
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
        }

        tags.add(tag);
    }

    public boolean removeTag(String tag) {
        return tags.remove(tag);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Object getAttribute(String name) {
        return attributes == null ? null : attributes.get(name);
    }

    /**
     * WARNING: Overrides all existing attributes
     *
     * @param attributes
     * @return
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String name, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public Location withLocationId(String locationId) {
        this.locationId = locationId;
        return this;
    }

    public Location withName(String name) {
        this.name = name;
        return this;
    }

    public Location withAddress(Address address) {
        this.address = address;
        return this;
    }

    /**
     * WARNING: Overrides all existing identifiers
     *
     * @param identifiers
     * @return
     */
    public Location withIdentifiers(Map<String, String> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public Location withIdentifier(String identifierType, String identifier) {
        if (identifiers == null) {
            identifiers = new HashMap<>();
        }

        identifiers.put(identifierType, identifier);
        return this;
    }

    public Location withParentLocation(Location parentLocation) {
        this.parentLocation = parentLocation;
        return this;
    }

    /**
     * WARNING: Overrides all existing tags
     *
     * @param tags
     * @return
     */
    public Location withTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public Location withTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
        }

        tags.add(tag);
        return this;
    }

    /**
     * WARNING: Overrides all existing attributes
     *
     * @param attributes
     * @return
     */
    public Location withAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public Location withAttribute(String name, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        attributes.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}