package se.gustavkarlsson.skylight.database

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

interface Repository<T> {
    data class Entry<T>(val sourceName: String, val report: T, val fetchTime: Instant)

    suspend fun getEntries(): List<Entry<T>>
    suspend fun update(entry: Entry<T>)
}

class InMemoryRepository<T>(
    private val maxEntriesPerSource: Int = 10,
) : Repository<T> {
    private val _entries = hashSetOf<Repository.Entry<T>>()

    private val mutex = Mutex()

    override suspend fun getEntries(): List<Repository.Entry<T>> {
        return mutex.withLock {
            _entries.toList().sortedBy { it.fetchTime }
        }
    }

    override suspend fun update(entry: Repository.Entry<T>) {
        mutex.withLock {
            _entries += entry
            getOldEntries(entry.sourceName).forEach {
                _entries.remove(it)
            }
        }
    }

    private fun getOldEntries(source: String): Iterable<Repository.Entry<T>> {
        return _entries
            .filter { it.sourceName == source }
            .sortedByDescending { it.fetchTime }
            .drop(maxEntriesPerSource)
    }
}
