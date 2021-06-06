package se.gustavkarlsson.skylight.sources.potsdam

import se.gustavkarlsson.skylight.Fetcher
import se.gustavkarlsson.skylight.SimpleHttpClient

object PotsdamFetcher : Fetcher<PotsdamData> {
    private const val URL = "http://www-app3.gfz-potsdam.de/kp_index/qlyymm.tab"

    override suspend fun fetch(): PotsdamData {
        val response = SimpleHttpClient.get(URL)
        return PotsdamData(response)
    }
}
