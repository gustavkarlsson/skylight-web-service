package se.gustavkarlsson.skylight

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import se.gustavkarlsson.skylight.logging.logDebug

object SimpleHttpClient {
    private val client = HttpClient(OkHttp)
    suspend fun get(url: String): String {
        logDebug { "Requesting data from $url" }
        val body = client.get(url).bodyAsText()
        logDebug { "Successfully got data from $url" }
        return body
    }
}
