package se.gustavkarlsson.skylight

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import se.gustavkarlsson.skylight.logging.logDebug

object SimpleHttpClient {
    private val client = HttpClient(OkHttp)
    suspend fun get(url: String): String {
        logDebug { "Requesting data from $url" }
        val body = client.get<String>(url)
        logDebug { "Successfully got data from $url" }
        return body
    }
}
