package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.CourseExercisesResponseApi
import io.github.kroune.cumobile.data.model.CourseStudentPerformanceResponseApi
import io.github.kroune.cumobile.data.model.GradebookResponseApi
import io.github.kroune.cumobile.data.model.StudentPerformanceResponseApi
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header

private val logger = KotlinLogging.logger {}

/**
 * API service for performance and gradebook endpoints.
 */
internal class PerformanceApiService(
    httpClient: Lazy<HttpClient>,
) {
    private val httpClient by httpClient

    /** Fetches overall student performance summary. */
    suspend fun fetchPerformance(cookie: String): StudentPerformanceResponseApi? =
        safeApiCall(logger, "fetch performance") {
            httpClient.get(ApiEndpoints.Performance.STUDENT) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Fetches exercises for a specific course. */
    suspend fun fetchCourseExercises(
        cookie: String,
        courseId: String,
    ): CourseExercisesResponseApi? =
        safeApiCall(logger, "fetch course exercises for courseId=$courseId") {
            httpClient.get(ApiEndpoints.Courses.exercises(courseId)) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Fetches per-student performance for a course. */
    suspend fun fetchCoursePerformance(
        cookie: String,
        courseId: String,
    ): CourseStudentPerformanceResponseApi? =
        safeApiCall(logger, "fetch course performance for courseId=$courseId") {
            httpClient.get(ApiEndpoints.Performance.coursePerformance(courseId)) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** Fetches the student's gradebook. */
    suspend fun fetchGradebook(cookie: String): GradebookResponseApi? =
        safeApiCall(logger, "fetch gradebook") {
            httpClient.get(ApiEndpoints.Performance.GRADEBOOK) {
                header("Cookie", cookieHeader(cookie))
            }
        }
}
