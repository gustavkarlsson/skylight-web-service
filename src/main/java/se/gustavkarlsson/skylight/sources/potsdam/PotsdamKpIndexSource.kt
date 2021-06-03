package se.gustavkarlsson.skylight.sources.potsdam

import se.gustavkarlsson.skylight.*

class PotsdamKpIndexSource(
    private val fetcher: Fetcher<PotsdamData> = PotsdamFetcher,
    private val extractor: Extractor<PotsdamData> = PotsdamExtractor,
) : KpIndexSource {
    override val name: String = "Potsdam"

    override suspend fun get(): KpIndexReport {
        val data = fetcher.fetch()
        return extractor.extract(data)
    }
}
