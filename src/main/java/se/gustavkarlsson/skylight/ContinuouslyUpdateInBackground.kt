package se.gustavkarlsson.skylight

import kotlinx.coroutines.*
import se.gustavkarlsson.skylight.database.Database
import se.gustavkarlsson.skylight.logging.logError
import se.gustavkarlsson.skylight.logging.logInfo
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
fun CoroutineScope.continuouslyUpdateInBackground(
    sources: Iterable<KpIndexSource>,
    database: Database,
    timeBetweenUpdates: Duration,
): Job = launch(CoroutineName("Update")) {
    while (true) {
        logInfo { "Updating..." }
        val elapsed = measureTime {
            updateSafe(sources, database, timeBetweenUpdates)
        }
        logInfo { "Update completed in $elapsed" }
        val delay = timeBetweenUpdates - elapsed
        if (delay.isPositive()) {
            logInfo { "Waiting for $delay until next update" }
            delay(delay)
        }
    }
}

@OptIn(ExperimentalTime::class)
private suspend fun updateSafe(
    sources: Iterable<KpIndexSource>,
    database: Database,
    timeout: Duration,
) {
    try {
        withTimeout(timeout) {
            updateAll(sources, database)
        }
    } catch (e: TimeoutCancellationException) {
        logError(e) { "Update timed out" }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logError(e) { "Update failed" }
    }
}

private suspend fun updateAll(sources: Iterable<KpIndexSource>, database: Database) {
    val jobs = supervisorScope {
        sources.map { source ->
            val handler = CoroutineExceptionHandler { _, t ->
                logError(t) { "${source.name} failed to update" }
            }
            launch(handler + CoroutineName(source.name)) {
                val report = source.get()
                val fetchTime = Instant.now()
                val entry = Database.Entry(source.name, report, fetchTime)
                database.update(entry)
            }
        }
    }
    jobs.joinAll()
}
