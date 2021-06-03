@file:JvmName("Application")

package se.gustavkarlsson.skylight

import com.bugsnag.Bugsnag
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
import se.gustavkarlsson.skylight.logging.*
import se.gustavkarlsson.skylight.sources.potsdam.PotsdamKpIndexSource
import java.time.Instant
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


@OptIn(ExperimentalTime::class)
fun main() {
    setupLogging()
    val port = readIntFromEnv("PORT")
    if (port==null) {
        logError { "Failed to read port" }
        exitProcess(1)
    }
    logInfo { "Read port: $port" }
    val updateDelay = Duration.minutes(15)
    logInfo { "Got update delay: $updateDelay" }
    val sources = listOf(PotsdamKpIndexSource())
    logInfo {
        val sourcesNames = sources.map { it.name }
        "Loaded sources: $sourcesNames"
    }
    val database = InMemoryDatabase()
    logInfo { "Loaded database: ${database.javaClass.name}" }
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

private fun setupLogging() {
    addLogger(Slf4jLogger)
    val bugsnag = trySetupBugsnag()
    if (bugsnag!=null) {
        val bugsnagLogger = BugsnagLogger(bugsnag)
        addLogger(bugsnagLogger)
    }
}

private fun trySetupBugsnag(): Bugsnag? {
    val apiKeyKey = "BUGSNAG_API_KEY"
    val apiKey = readStringFromEnv(apiKeyKey) ?: return null
    val releaseStageKey = "BUGSNAG_RELEASE_STAGE"
    val releaseStage = readStringFromEnv(releaseStageKey)?.trim()?.lowercase()
    val validStages = listOf("production", "develop")
    when (releaseStage) {
        in validStages -> Unit
        null -> {
            val message = "$apiKeyKey is set without a valid $releaseStageKey ($releaseStage). " +
                "Must be one of: $validStages"
            error(message)
        }
        else -> {
            val message = "$apiKeyKey is set without $releaseStageKey. " +
                "Must be one of: $validStages"
            error(message)
        }
    }
    val bugsnag = Bugsnag(apiKey, true)
    bugsnag.setReleaseStage(releaseStage)
    return bugsnag
}

@OptIn(ExperimentalTime::class)
private fun CoroutineScope.continuouslyUpdateInBackground(
    timeBetweenUpdates: Duration,
    sources: Iterable<KpIndexSource>,
    database: Database,
): Job = launch(CoroutineName("Update")) {
    while (true) {
        logInfo { "Updating..." }
        val elapsed = measureTime {
            updateSafe(timeBetweenUpdates, sources, database)
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
    timeout: Duration,
    sources: Iterable<KpIndexSource>,
    database: Database,
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

private fun readStringFromEnv(key: String): String? {
    val string = System.getenv(key)?.trim()
    if (string.isNullOrBlank()) {
        logWarn { "No value set for $$key" }
        return null
    }
    logDebug { "Read $string from $$key" }
    return string
}

private fun readIntFromEnv(key: String): Int? {
    val string = System.getenv(key)?.trim()
    if (string.isNullOrBlank()) {
        logWarn { "No value set for $$key" }
        return null
    }
    val int = string.toIntOrNull()
    if (int==null) {
        logWarn { "Failed to parse $$key=$string as integer" }
        return null
    }
    logDebug { "Read $int from $$key" }
    return int
}
