package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.CalendarEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

private val logger = KotlinLogging.logger {}

/**
 * Service for fetching and parsing iCal (.ics) calendar feeds.
 */
internal class IcalApiService(
    private val client: HttpClient,
    private val parser: IcalParser,
) {
    /**
     * Fetches a calendar feed from the given [url] and returns
     * the parsed events. Returns empty list on failure.
     */
    suspend fun fetchCalendar(url: String): List<CalendarEvent> {
        return try {
            val response = client.get(url)
            val body = response.bodyAsText()
            parser.parse(body)
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch calendar from $url" }
            emptyList()
        }
    }
}
