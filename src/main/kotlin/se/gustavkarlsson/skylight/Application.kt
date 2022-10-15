@file:JvmName("Application")

package se.gustavkarlsson.skylight

import com.bugsnag.Bugsnag
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.launch
import se.gustavkarlsson.skylight.database.InMemoryRepository
import se.gustavkarlsson.skylight.database.Repository
import se.gustavkarlsson.skylight.logging.BugsnagLogger
import se.gustavkarlsson.skylight.logging.Slf4jLogger
import se.gustavkarlsson.skylight.logging.addLogger
import se.gustavkarlsson.skylight.logging.logDebug
import se.gustavkarlsson.skylight.logging.logInfo
import se.gustavkarlsson.skylight.logging.logWarn
import se.gustavkarlsson.skylight.sources.potsdam.PotsdamKpIndexSource
import se.gustavkarlsson.skylight.sources.swpc.SwpcKpIndexForecastSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


fun main() {
    setupLogging()

    val portKey = "PORT"
    val port = readIntFromEnv(portKey) ?: error("Failed to read port from $$portKey")
    logInfo { "Port: $port" }

    embeddedServer(Netty, port = port) {
        install(ContentNegotiation) { json() }
        install(CallLogging)
        setupKpIndexRoute(
            sources = listOf(element = PotsdamKpIndexSource()),
            repo = InMemoryRepository(),
            timeBetweenUpdates = 15.minutes,
        )
        setupForecastRoute(
            sources = listOf(element = SwpcKpIndexForecastSource()),
            repo = InMemoryRepository(),
            timeBetweenUpdates = 20.minutes,
        )
    }.start(wait = true)
}

private fun Application.setupKpIndexRoute(
    sources: Iterable<Source<KpIndexReport>>,
    repo: Repository<KpIndexReport>,
    timeBetweenUpdates: Duration,
) {
    logInfo { "Loaded Kp index sources: ${sources.map { it.name }}" }
    logInfo { "Loaded Kp index repo: ${repo.javaClass.name}" }
    logInfo { "Kp index time between updates: $timeBetweenUpdates" }

    launch { continuouslyUpdate(sources, repo, timeBetweenUpdates, "Kp index") }
    routing {
        get("/kp-index") {
            when (val entry = repo.getEntries().firstOrNull()) {
                null -> {
                    call.respond(HttpStatusCode.NotFound)
                }
                else -> {
                    val response = KpIndexResponse(entry.report.kpIndex.value, entry.fetchTime.toEpochMilli())
                    call.respond(response)
                }
            }
        }
    }
}

private fun Application.setupForecastRoute(
    sources: Iterable<Source<KpIndexForecastReport>>,
    repo: Repository<KpIndexForecastReport>,
    timeBetweenUpdates: Duration,
) {
    logInfo { "Loaded forecast sources: ${sources.map { it.name }}" }
    logInfo { "Loaded forecast repo: ${repo.javaClass.name}" }
    logInfo { "Forecast time between updates: $timeBetweenUpdates" }

    launch { continuouslyUpdate(sources, repo, timeBetweenUpdates, "Forecast") }
    routing {
        get("/kp-index-forecast") {
            when (val entry = repo.getEntries().firstOrNull()) {
                null -> {
                    call.respond(HttpStatusCode.NotFound)
                }
                else -> {
                    val kpIndexes = entry.report.map.entries
                        .map { (timestamp, kpIndex) ->
                            KpIndexResponse(kpIndex.value, timestamp.toEpochMilli())
                        }
                        .sortedBy { it.timestamp }
                    val response = KpIndexForecastResponse(kpIndexes)
                    call.respond(response)
                }
            }
        }
    }
}

private fun setupLogging() {
    addLogger(Slf4jLogger)
    val bugsnag = trySetupBugsnag()
    if (bugsnag != null) {
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

private fun readIntFromEnv(key: String): Int? {
    val string = System.getenv(key)?.trim()
    if (string.isNullOrBlank()) {
        logWarn { "No value set for $$key" }
        return null
    }
    val int = string.toIntOrNull()
    if (int == null) {
        logWarn { "Failed to parse $$key=$string as integer" }
        return null
    }
    logDebug { "Read $int from $$key" }
    return int
}

private fun readStringFromEnv(key: String): String? {
    val string = System.getenv(key)?.trim()
    if (string.isNullOrBlank()) {
        logWarn { "No value set for $$key" }
        return null
    }
    logDebug { "Read '$string' from $$key" }
    return string
}
