@file:JvmName("Application")

package se.gustavkarlsson.skylight

import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlin.system.exitProcess

public fun main() {
    val port = getPortFromEnv("PORT")
    if (port==null) {
        logError { "Failed to read port." }
        exitProcess(1)
    }
    embeddedServer(Netty, port = port) {
        routing {
            get("/kp-index") {

            }
        }
    }.start(wait = true)
}

private fun getPortFromEnv(key: String): Int? {
    val portString = System.getenv(key)?.trim()
    if (portString.isNullOrBlank()) {
        logWarn { "No port set in $$key." }
        return null
    }
    val port = portString.toIntOrNull()
    if (port==null) {
        logWarn { "Failed to read port $portString from $$key." }
        return null
    }
    logInfo { "Read port $port from $$key." }
    return port
}

public fun logError(message: () -> String) {
    println(message())
}

public fun logWarn(message: () -> String) {
    println(message())
}

public fun logInfo(message: () -> String) {
    println(message())
}

public fun logDebug(message: () -> String) {
    println(message())
}
