package se.gustavkarlsson.skylight

interface KpIndexSource {
    val name: String

    suspend fun get(): KpIndex
}
