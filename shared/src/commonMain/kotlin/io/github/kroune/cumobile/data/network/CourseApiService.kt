package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.CourseOverview
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header

private val logger = KotlinLogging.logger {}

/**
 * API service for course-related endpoints.
 */
internal class CourseApiService(
    private val httpClient: HttpClient,
) {
    /** GET /micro-lms/courses/student?limit=10000 → list of [Course] */
    suspend fun fetchCourses(cookie: String): List<Course>? =
        safeApiCall(logger, "fetch courses") {
            httpClient.get("micro-lms/courses/student?limit=$MaxListLimit") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** GET /micro-lms/courses/{courseId}/overview → [CourseOverview] */
    suspend fun fetchCourseOverview(
        cookie: String,
        courseId: Int,
    ): CourseOverview? =
        safeApiCall(logger, "fetch course overview for courseId=$courseId") {
            httpClient.get("micro-lms/courses/$courseId/overview") {
                header("Cookie", cookieHeader(cookie))
            }
        }
}
