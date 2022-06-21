package org.smartregister.reveal.model

import java.util.UUID

data class LocationProperty(val name: String, val status: String, val externalId: UUID? = null, val geographicLevel: String)
