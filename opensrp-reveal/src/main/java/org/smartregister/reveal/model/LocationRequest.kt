package org.smartregister.reveal.model

import org.smartregister.domain.Geometry

data class LocationRequest(val type: String, val geometry: Geometry, val properties: LocationProperty)
