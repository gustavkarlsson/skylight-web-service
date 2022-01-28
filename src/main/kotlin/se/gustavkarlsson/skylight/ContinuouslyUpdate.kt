package se.gustavkarlsson.skylight

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import se.gustavkarlsson.skylight.database.Repository
import se.gustavkarlsson.skylight.logging.logError
import se.gustavkarlsson.skylight.logging.logInfo
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
suspend fun <T : Any> continuouslyUpdate(
    sources: Iterable<Source<T>>,
    repo: Repository<T>,
    timeBetweenUpdates: Duration,
    debugName: String,
): Nothing {
    while (true) {
        logInfo { "Updating $debugName..." }
        val elapsed = measureTime {
            updateSafe(sources, repo, timeBetweenUpdates)
        }
        logInfo { "$debugName update completed in $elapsed" }
        val delay = timeBetweenUpdates - elapsed
        if (delay.isPositive()) {
            logInfo { "Waiting for $delay until next $debugName update" }
            delay(delay)
        }
    }
}

@OptIn(ExperimentalTime::class)
private suspend fun <T : Any> updateSafe(
    sources: Iterable<Source<T>>,
    repository: Repository<T>,
    timeout: Duration,
) {
    try {
        withTimeout(timeout) {
            updateAll(sources, repository)
        }
    } catch (e: TimeoutCancellationException) {
        logError(e) { "Update timed out" }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logError(e) { "Update failed" }
    }
}

private suspend fun <T : Any> updateAll(sources: Iterable<Source<T>>, repo: Repository<T>) {
    val jobs = supervisorScope {
        sources.map { source ->
            val handler = CoroutineExceptionHandler { _, t ->
                logError(t) { "${source.name} failed to update" }
            }
            launch(handler + CoroutineName(source.name)) {
                val report = source.get()
                if (report == null) {
                    logInfo { "No report from source: $source" }
                    return@launch
                }
                val fetchTime = Instant.now()
                val entry = Repository.Entry(source.name, report, fetchTime)
                repo.update(entry)
            }
        }
    }
    jobs.joinAll()
}
