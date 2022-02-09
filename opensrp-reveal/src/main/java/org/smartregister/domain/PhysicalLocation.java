package org.smartregister.domain;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "identifier")
@Data
public class PhysicalLocation implements Serializable {

    private static final long serialVersionUID = -4863877528673921296L;

    private String type;

    private String identifier;

    private Geometry geometry;

    private LocationProperty properties;

    private Long serverVersion;

    private Set<LocationTag> locationTags;

    private transient boolean isJurisdiction;

}
