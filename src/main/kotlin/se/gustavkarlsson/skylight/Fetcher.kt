package se.gustavkarlsson.skylight

interface Fetcher<Data> {
    suspend fun fetch(): Data
}
