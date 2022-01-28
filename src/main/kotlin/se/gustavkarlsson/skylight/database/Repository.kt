package se.gustavkarlsson.skylight.database

import java.time.Instant

interface Repository<T> {
    data class Entry<T>(val sourceName: String, val report: T, val fetchTime: Instant)

    val entries: List<Entry<T>>
    fun update(entry: Entry<T>)
}

class InMemoryRepository<T>(
    private val maxEntriesPerSource: Int = 10,
) : Repository<T> {
    private val _entries = hashSetOf<Repository.Entry<T>>()

    override val entries: List<Repository.Entry<T>>
        get() = _entries.sortedBy { it.fetchTime }

    override fun update(entry: Repository.Entry<T>) {
        _entries += entry
        getOldEntries(entry.sourceName).forEach {
            _entries.remove(it)
        }
    }

    private fun getOldEntries(source: String): Iterable<Repository.Entry<T>> {
        return _entries
            .filter { it.sourceName == source }
            .sortedByDescending { it.fetchTime }
            .drop(maxEntriesPerSource)
    }
}
