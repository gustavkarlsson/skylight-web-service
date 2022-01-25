package se.gustavkarlsson.skylight.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import se.gustavkarlsson.skylight.KpIndex
import se.gustavkarlsson.skylight.KpIndexForecastResponse
import se.gustavkarlsson.skylight.KpIndexResponse
import se.gustavkarlsson.skylight.database.Database
import java.time.Instant

fun Routing.kpIndexRoute(database: Database) {
    get("/kp-index") {
        when (val entry = database.entries.firstOrNull()) {
            null -> {
                call.respond(HttpStatusCode.NotFound)
            }
            else -> {
                val response = KpIndexResponse(entry.kpIndexResult.kpIndex.value, entry.fetchTime.toEpochMilli())
                call.respond(response)
            }
        }
    }
    get("/kp-index-forecast") {
        when (val forecast = TODO("FIXME get from database") as Map<Instant, KpIndex>) {
            null -> {
                call.respond(HttpStatusCode.NotFound)
            }
            else -> {
                val map = forecast.entries.associate { (k, v) -> k.toEpochMilli() to v.value }
                val response = KpIndexForecastResponse(map)
                call.respond(response)
            }
        }
    }
}
