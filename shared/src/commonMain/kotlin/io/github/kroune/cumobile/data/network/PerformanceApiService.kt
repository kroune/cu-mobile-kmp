package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.CourseExercisesResponse
import io.github.kroune.cumobile.data.model.CourseStudentPerformanceResponse
import io.github.kroune.cumobile.data.model.GradebookResponse
import io.github.kroune.cumobile.data.model.StudentPerformanceResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header

private val logger = KotlinLogging.logger {}

/**
 * API service for performance and gradebook endpoints.
 */
internal class PerformanceApiService(
    private val httpClient: HttpClient,
) {
    /** GET /micro-lms/performance/student */
    suspend fun fetchPerformance(cookie: String): StudentPerformanceResponse? =
        safeApiCall(logger, "fetch performance") {
            httpClient.get("micro-lms/performance/student") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** GET /micro-lms/courses/{courseId}/exercises */
    suspend fun fetchCourseExercises(
        cookie: String,
        courseId: Int,
    ): CourseExercisesResponse? =
        safeApiCall(logger, "fetch course exercises for courseId=$courseId") {
            httpClient.get("micro-lms/courses/$courseId/exercises") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** GET /micro-lms/courses/{courseId}/student-performance */
    suspend fun fetchCoursePerformance(
        cookie: String,
        courseId: Int,
    ): CourseStudentPerformanceResponse? =
        safeApiCall(logger, "fetch course performance for courseId=$courseId") {
            httpClient.get("micro-lms/courses/$courseId/student-performance") {
                header("Cookie", cookieHeader(cookie))
            }
        }

    /** GET /micro-lms/gradebook */
    suspend fun fetchGradebook(cookie: String): GradebookResponse? =
        safeApiCall(logger, "fetch gradebook") {
            httpClient.get("micro-lms/gradebook") {
                header("Cookie", cookieHeader(cookie))
            }
        }
}
