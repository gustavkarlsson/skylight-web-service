@file:JvmName("Application")

package se.gustavkarlsson.skylight

import com.rollbar.notifier.Rollbar
import com.rollbar.notifier.config.ConfigBuilder
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
import se.gustavkarlsson.skylight.logging.RollbarLogger
import se.gustavkarlsson.skylight.logging.Slf4jLogger
import se.gustavkarlsson.skylight.logging.addLogger
import se.gustavkarlsson.skylight.logging.logDebug
import se.gustavkarlsson.skylight.logging.logError
import se.gustavkarlsson.skylight.logging.logInfo
import se.gustavkarlsson.skylight.logging.logWarn
import se.gustavkarlsson.skylight.sources.potsdam.PotsdamKpIndexSource
import se.gustavkarlsson.skylight.sources.swpc.SwpcKpIndexForecastSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val PORT_KEY = "PORT"
private const val ADMIN_PORT_KEY = "ADMIN_PORT"
private const val ROLLBAR_ACCESS_TOKEN_KEY = "ROLLBAR_ACCESS_TOKEN"
private const val ROLLBAR_ENVIRONMENT_KEY = "ROLLBAR_ENVIRONMENT"

fun main() {
    withMonitoring {
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
}

private fun withMonitoring(block: () -> Unit) {
    addLogger(Slf4jLogger)
    val rollbar = trySetupRollbar()
    if (rollbar != null) {
        val rollbarLogger = RollbarLogger(rollbar)
        addLogger(rollbarLogger)
        logInfo { "Rollbar enabled" }
    } else {
        logWarn { "Rollbar disabled" }
    }
    try {
        block()
    } catch (e: Exception) {
        logError(e) {
            "Application terminated unexpectedly"
        }
    } finally {
        rollbar?.close(true)
    }
}

private fun trySetupRollbar(): Rollbar? {
    val accessToken = readStringFromEnv(ROLLBAR_ACCESS_TOKEN_KEY, redact = true) ?: return null
    val environment = readStringFromEnv(ROLLBAR_ENVIRONMENT_KEY)?.trim()?.lowercase()
    val validStages = listOf("production", "develop")
    when (environment) {
        in validStages -> Unit
        null -> {
            val message = "$ROLLBAR_ACCESS_TOKEN_KEY is set without $ROLLBAR_ENVIRONMENT_KEY. " +
                "Must be one of: $validStages"
            error(message)
        }

        else -> {
            val message = "$ROLLBAR_ACCESS_TOKEN_KEY is set without a valid $ROLLBAR_ENVIRONMENT_KEY ($environment). " +
                "Must be one of: $validStages"
            error(message)
        }
    }
    // TODO Add code version with git hash
    val config = ConfigBuilder
        .withAccessToken(accessToken)
        .build()
    return Rollbar.init(config)
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

private fun readStringFromEnv(key: String, redact: Boolean = false): String? {
    val string = System.getenv(key)?.trim()
    if (string.isNullOrBlank()) {
        logWarn { "No value set for $$key" }
        return null
    }
    val logString = if (redact) "**********" else string
    logDebug { "Read '$logString' from $$key" }
    return string
}
