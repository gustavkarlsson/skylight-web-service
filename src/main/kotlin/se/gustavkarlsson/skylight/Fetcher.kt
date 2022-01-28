package se.gustavkarlsson.skylight

interface Fetcher<out Data> {
    suspend fun fetch(): Data
}
