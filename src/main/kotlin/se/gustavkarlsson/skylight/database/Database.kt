package se.gustavkarlsson.skylight.database

import se.gustavkarlsson.skylight.KpIndexReport
import java.time.Instant

interface Database {
    data class Entry(val sourceName: String, val kpIndexResult: KpIndexReport, val fetchTime: Instant)

    val entries: List<Entry>
    fun update(entry: Entry)
}

class InMemoryDatabase(
    private val maxEntriesPerSource: Int = 10,
) : Database {
    private val _entries = hashSetOf<Database.Entry>()

    override val entries: List<Database.Entry>
        get() = _entries.sortedBy { it.fetchTime }

    override fun update(entry: Database.Entry) {
        _entries += entry
        getOldEntries(entry.sourceName).forEach {
            _entries.remove(it)
        }
    }

    private fun getOldEntries(source: String): Iterable<Database.Entry> {
        return _entries
            .filter { it.sourceName==source }
            .sortedByDescending { it.fetchTime }
            .drop(maxEntriesPerSource)
    }
}
