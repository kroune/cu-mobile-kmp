package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.CourseOverview
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

private val logger = KotlinLogging.logger {}

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

/**
 * API service for course-related endpoints.
 */
internal class CourseApiService(
    private val httpClient: Lazy<HttpClient>,
) {
    /**
     * Fetches all courses for the current student.
     *
     * The API may return either a bare JSON array or a wrapper object
     * with an `"items"` key containing the array.
     */
    suspend fun fetchCourses(cookie: String): List<Course>? =
        runCatchingCancellable {
            val response = httpClient().get("${ApiEndpoints.Courses.STUDENT}?limit=$MaxListLimit") {
                header("Cookie", cookieHeader(cookie))
            }
            if (response.status != HttpStatusCode.OK) {
                logger.warn { "fetch courses returned ${response.status}" }
                return@runCatchingCancellable null
            }
            val text = response.bodyAsText()
            val element = json.parseToJsonElement(text)
            when (element) {
                is JsonArray -> json.decodeFromString<List<Course>>(text)
                is JsonObject -> {
                    val items = element["items"]?.jsonArray
                    items
                        ?.let { json.decodeFromString<List<Course>>(it.toString()) }
                        .orEmpty()
                }
                else -> emptyList()
            }
        }.getOrElse { e ->
            logger.error(e) { "Failed to fetch courses" }
            null
        }

    /** Fetches the overview for a specific course. */
    suspend fun fetchCourseOverview(
        cookie: String,
        courseId: String,
    ): CourseOverview? =
        safeApiCall(logger, "fetch course overview for courseId=$courseId") {
            httpClient().get(ApiEndpoints.Courses.overview(courseId)) {
                header("Cookie", cookieHeader(cookie))
            }
        }
}
