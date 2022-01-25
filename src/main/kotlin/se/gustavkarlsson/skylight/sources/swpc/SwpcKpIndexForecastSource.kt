package se.gustavkarlsson.skylight.sources.swpc

import se.gustavkarlsson.skylight.Extractor
import se.gustavkarlsson.skylight.Fetcher
import se.gustavkarlsson.skylight.KpIndexForecastReport
import se.gustavkarlsson.skylight.Source

class SwpcKpIndexForecastSource(
    private val fetcher: Fetcher<SwpcForecastData> = SwpcForecastFetcher,
    private val extractor: Extractor<SwpcForecastData, KpIndexForecastReport> = SwpcForecastExtractor,
) : Source<KpIndexForecastReport> {
    override val name: String = "SWPC Kp Index Forecast"

    override suspend fun get(): KpIndexForecastReport? {
        val data = fetcher.fetch()
        return extractor.extract(data)
    }
}
