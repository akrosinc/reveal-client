package org.smartregister.reveal.model

import org.smartregister.reveal.util.Country

data class EnvironmentDetails(val revealServerUrl: String, val authUrl: String, val buildCountry: Country)