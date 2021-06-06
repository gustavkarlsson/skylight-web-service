package se.gustavkarlsson.skylight

import kotlinx.serialization.Serializable

@Serializable
data class KpIndexResponse(val value: Float, val timestamp: Long)
