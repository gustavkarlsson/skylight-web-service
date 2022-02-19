package se.gustavkarlsson.skylight.sources.swpc

import se.gustavkarlsson.skylight.Fetcher
import se.gustavkarlsson.skylight.SimpleHttpClient

object SwpcForecastFetcher : Fetcher<SwpcForecastData> {
    private const val URL = "https://services.swpc.noaa.gov/text/3-day-forecast.txt"

    override suspend fun fetch(): SwpcForecastData {
        val response = SimpleHttpClient.get(URL)
        return SwpcForecastData(response)
    }
}
