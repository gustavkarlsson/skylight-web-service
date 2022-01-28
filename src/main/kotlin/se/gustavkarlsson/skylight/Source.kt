package se.gustavkarlsson.skylight

interface Source<out T : Any> {
    val name: String

    suspend fun get(): T?
}
