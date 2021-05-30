@file:JvmName("Application")

package se.gustavkarlsson.skylight

import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.time.withTimeout
import se.gustavkarlsson.skylight.sources.potsdam.PotsdamKpIndexSource
import java.time.Duration
import kotlin.system.exitProcess

fun main() {
    val port = getPortFromEnv("PORT")
    if (port==null) {
        logError { "Failed to read port." }
        exitProcess(1)
    }
    val updateDelay = Duration.ofMinutes(15)
    val sources = listOf(PotsdamKpIndexSource())
    embeddedServer(Netty, port = port) {
        updateInBackground(updateDelay, sources)
        routing {
            get("/kp-index") {
                // FIXME get from database
            }
        }
    }.start(wait = true)
}

private fun CoroutineScope.updateInBackground(updateDelay: Duration, sources: Iterable<KpIndexSource>) {
    val timeout = Duration.ofMinutes(1)
    launch {
        while (true) {
            sources
                .map { source ->
                    source to async { source.get() }
                }
                .forEach { (source, job) ->
                    try {
                        val kpIndex = withTimeout(timeout) {
                            job.await()
                        }
                        // FIXME save in database
                    } catch (e: TimeoutCancellationException) {
                        logError { "Timed out after $timeout when getting Kp index from ${source.name}" }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logError(e) { "Failed to get Kp index from ${source.name}" }
                    }
                }
            delay(updateDelay)
        }
    }
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

fun logError(e: Exception? = null, message: () -> String) {
    println(message())
    e?.printStackTrace(System.out)
}

fun logWarn(e: Exception? = null, message: () -> String) {
    println(message())
    e?.printStackTrace(System.out)
}

fun logInfo(message: () -> String) {
    println(message())
}

fun logDebug(message: () -> String) {
    println(message())
}
