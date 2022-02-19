package se.gustavkarlsson.skylight.sources.potsdam

import se.gustavkarlsson.skylight.Extractor
import se.gustavkarlsson.skylight.Fetcher
import se.gustavkarlsson.skylight.KpIndexReport
import se.gustavkarlsson.skylight.Source

class PotsdamKpIndexSource(
    private val fetcher: Fetcher<PotsdamData> = PotsdamFetcher,
    private val extractor: Extractor<PotsdamData, KpIndexReport> = PotsdamExtractor,
) : Source<KpIndexReport> {
    override val name: String = "Potsdam Kp Index"

    override suspend fun get(): KpIndexReport? {
        val data = fetcher.fetch()
        return extractor.extract(data)
    }
}
