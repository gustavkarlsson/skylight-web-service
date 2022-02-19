package se.gustavkarlsson.skylight

import java.time.Instant

data class KpIndexForecastReport(val map: Map<Instant, KpIndex>, val signature: Signature)
