@file:JvmName("Application")

package se.gustavkarlsson.skylight

import com.bugsnag.Bugsnag
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import se.gustavkarlsson.skylight.database.Database
import se.gustavkarlsson.skylight.database.InMemoryDatabase
import se.gustavkarlsson.skylight.logging.*
import se.gustavkarlsson.skylight.routes.kpIndexRoute
import se.gustavkarlsson.skylight.sources.potsdam.PotsdamKpIndexSource
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
fun main() {
    setupLogging()

    val portKey = "PORT"
    val port = readIntFromEnv(portKey) ?: error("Failed to read port from $$portKey")
    logInfo { "Port: $port" }

    val sources: Iterable<KpIndexSource> = listOf(PotsdamKpIndexSource())
    logInfo {
        val sourcesNames = sources.map { it.name }
        "Loaded sources: $sourcesNames"
    }

    val database: Database = InMemoryDatabase()
    logInfo { "Loaded database: ${database.javaClass.name}" }

    val updateDelay = Duration.minutes(15)
    logInfo { "Update delay: $updateDelay" }

    embeddedServer(Netty, port = port) {
        continuouslyUpdateInBackground(sources, database, updateDelay)
        install(ContentNegotiation) { json() }
        install(CallLogging)
        routing {
            kpIndexRoute(database)
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

private fun readStringFromEnv(key: String): String? {
    val string = System.getenv(key)?.trim()
    if (string.isNullOrBlank()) {
        logWarn { "No value set for $$key" }
        return null
    }
    logDebug { "Read '$string' from $$key" }
    return string
}
