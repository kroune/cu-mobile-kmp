package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.TimetableCourse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header

private val logger = KotlinLogging.logger {}

/**
 * API service for fetching the student timetable from the LMS API.
 */
internal class TimetableApiService(
    private val httpClient: HttpClient,
) {
    /**
     * Fetches the current student's full timetable.
     * Returns `null` on failure.
     */
    suspend fun fetchTimetable(cookie: String): List<TimetableCourse>? =
        safeApiCall(logger, "fetch timetable") {
            httpClient.get(ApiEndpoints.Timetable.ME) {
                header("Cookie", cookieHeader(cookie))
            }
        }
}
