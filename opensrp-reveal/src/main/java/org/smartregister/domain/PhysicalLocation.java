package org.smartregister.domain;

import org.smartregister.domain.Geometry;
import org.smartregister.domain.LocationProperty;
import org.smartregister.domain.LocationTag;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "id")
@Data
public class PhysicalLocation implements Serializable {

    private static final long serialVersionUID = -4863877528673921296L;

    private String type;

    private String id;

    private Geometry geometry;

    private LocationProperty properties;

    private Long serverVersion;

    private Set<LocationTag> locationTags;

    private transient boolean isJurisdiction;

}
