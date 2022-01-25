package se.gustavkarlsson.skylight

interface Source<T : Any> {
    val name: String

    suspend fun get(): T?
}
