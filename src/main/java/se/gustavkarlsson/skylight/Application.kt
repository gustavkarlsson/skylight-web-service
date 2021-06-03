@file:JvmName("Application")

package se.gustavkarlsson.skylight

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import se.gustavkarlsson.skylight.database.Database
import se.gustavkarlsson.skylight.database.InMemoryDatabase
import se.gustavkarlsson.skylight.sources.potsdam.PotsdamKpIndexSource
import java.time.Instant
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
fun main() {
    val port = getPortFromEnv("PORT")
    if (port==null) {
        logError { "Failed to read port." }
        exitProcess(1)
    }
    val updateDelay = Duration.minutes(15)
    val sources = listOf(PotsdamKpIndexSource())
    val database = InMemoryDatabase()
    embeddedServer(Netty, port = port) {
        install(ContentNegotiation) {
            json()
        }
        install(CallLogging)
        continuouslyUpdateInBackground(updateDelay, sources, database)
        routing {
            get("/kp-index") {
                val entry = database.entries.firstOrNull()
                if (entry==null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val response = KpIndexResponse(entry.kpIndexResult.kpIndex.value, entry.fetchTime.toEpochMilli())
                    call.respond(response)
                }
            }
        }
    }.start(wait = true)
}

@OptIn(ExperimentalTime::class)
private fun CoroutineScope.continuouslyUpdateInBackground(
    timeBetweenUpdates: Duration,
    sources: Iterable<KpIndexSource>,
    database: Database,
): Job = launch {
    while (true) {
        logInfo { "Updating..." }
        val elapsed = measureTime {
            updateSafe(timeBetweenUpdates, sources, database)
        }
        logInfo { "Update completed in $elapsed." }
        val delay = timeBetweenUpdates - elapsed
        if (delay.isPositive()) {
            logInfo { "Waiting for $delay until next update." }
            delay(delay)
        }
    }
}

@OptIn(ExperimentalTime::class)
private suspend fun updateSafe(
    timeout: Duration,
    sources: Iterable<KpIndexSource>,
    database: Database,
) {
    try {
        withTimeout(timeout) {
            updateAll(sources, database)
        }
    } catch (e: TimeoutCancellationException) {
        logError(e) { "Update timed out." }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logError(e) { "Update failed." }
    }
}

private suspend fun updateAll(sources: Iterable<KpIndexSource>, database: Database) {
    val jobs = supervisorScope {
        sources.map { source ->
            val handler = CoroutineExceptionHandler { _, t ->
                logError(t) { "${source.name} failed to update." }
            }
            launch(handler) {
                val report = source.get()
                val fetchTime = Instant.now()
                val entry = Database.Entry(source.name, report, fetchTime)
                database.update(entry)
            }
        }
    }
    jobs.joinAll()
}

private fun getPortFromEnv(key: String): Int? {
    val string = System.getenv(key)?.trim()
    if (string.isNullOrBlank()) {
        logWarn { "No port set in $$key." }
        return null
    }
    val port = string.toIntOrNull()
    if (port==null) {
        logWarn { "Failed to read port $string from $$key." }
        return null
    }
    logInfo { "Read port $port from $$key." }
    return port
}
