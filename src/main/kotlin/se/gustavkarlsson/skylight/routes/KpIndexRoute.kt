package se.gustavkarlsson.skylight.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import se.gustavkarlsson.skylight.KpIndexResponse
import se.gustavkarlsson.skylight.database.Database

fun Routing.kpIndexRoute(database: Database) {
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
