package se.gustavkarlsson.skylight

import kotlinx.serialization.Serializable

@Serializable
data class KpIndexForecastResponse(val kpIndexes: List<KpIndexResponse>)
