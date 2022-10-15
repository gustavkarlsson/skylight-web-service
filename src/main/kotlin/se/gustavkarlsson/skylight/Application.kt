@file:JvmName("Application")

package se.gustavkarlsson.skylight

import com.bugsnag.Bugsnag
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngineEnvironmentBuilder
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.port
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
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

private const val PORT_KEY = "PORT"
private const val ADMIN_PORT_KEY = "ADMIN_PORT"

fun main() {
    setupLogging()

    val port = readIntFromEnv(PORT_KEY) ?: 8080
    logInfo { "Application port: $port" }

    val adminPort = readIntFromEnv(ADMIN_PORT_KEY) ?: 9090
    logInfo { "Admin port: $adminPort" }

    val environment = applicationEngineEnvironment {
        setupAppModule(port)
        setupAdminModule(adminPort)
    }
    embeddedServer(CIO, environment).start(wait = true)
}

private fun setupLogging() {
    addLogger(Slf4jLogger)
    val bugsnag = trySetupBugsnag()
    if (bugsnag != null) {
        val bugsnagLogger = BugsnagLogger(bugsnag)
        addLogger(bugsnagLogger)
        logInfo { "Bugsnag enabled" }
    } else {
        logWarn { "Bugsnag disabled" }
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
            val message = "$apiKeyKey is set without $releaseStageKey. Must be one of: $validStages"
            error(message)
        }

        else -> {
            val message = "$apiKeyKey is set without a valid $releaseStageKey ($releaseStage). " +
                "Must be one of: $validStages"
            error(message)
        }
    }
    val bugsnag = Bugsnag(apiKey, true)
    bugsnag.setReleaseStage(releaseStage)
    return bugsnag
}

private fun ApplicationEngineEnvironmentBuilder.setupAppModule(port: Int) {
    connector {
        this.port = port
    }
    module {
        install(ContentNegotiation) { json() }
        install(CallLogging)
        routing {
            port(port) {
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
            }
        }
    }
}

private fun Route.setupKpIndexRoute(
    sources: Iterable<Source<KpIndexReport>>,
    repo: Repository<KpIndexReport>,
    timeBetweenUpdates: Duration,
) {
    logInfo { "Loaded Kp index sources: ${sources.map { it.name }}" }
    logInfo { "Loaded Kp index repo: ${repo.javaClass.name}" }
    logInfo { "Kp index time between updates: $timeBetweenUpdates" }

    application.launch { continuouslyUpdate(sources, repo, timeBetweenUpdates, "Kp index") }

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

private fun Route.setupForecastRoute(
    sources: Iterable<Source<KpIndexForecastReport>>,
    repo: Repository<KpIndexForecastReport>,
    timeBetweenUpdates: Duration,
) {
    logInfo { "Loaded forecast sources: ${sources.map { it.name }}" }
    logInfo { "Loaded forecast repo: ${repo.javaClass.name}" }
    logInfo { "Forecast time between updates: $timeBetweenUpdates" }

    application.launch { continuouslyUpdate(sources, repo, timeBetweenUpdates, "Forecast") }

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

private fun ApplicationEngineEnvironmentBuilder.setupAdminModule(port: Int) {
    connector {
        this.port = port
    }
    module {
        val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        install(MicrometerMetrics) {
            registry = appMicrometerRegistry
            meterBinders = listOf(
                JvmGcMetrics(),
                JvmInfoMetrics(),
                JvmMemoryMetrics(),
                JvmThreadMetrics(),
                ProcessorMetrics(),
            )
        }
        routing {
            port(port) {
                setupPrometheusScrapeRoute(appMicrometerRegistry)
            }
        }
    }
}

private fun Route.setupPrometheusScrapeRoute(registry: PrometheusMeterRegistry) {
    get("/metrics") {
        call.respond(registry.scrape())
    }
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
