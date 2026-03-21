package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.CourseExercisesResponse
import io.github.kroune.cumobile.data.model.CourseStudentPerformanceResponse
import io.github.kroune.cumobile.data.model.GradebookResponse
import io.github.kroune.cumobile.data.model.StudentPerformanceResponse
import io.github.kroune.cumobile.data.network.PerformanceApiService
import io.github.kroune.cumobile.domain.repository.PerformanceRepository

/**
 * Implementation of [PerformanceRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [PerformanceApiService].
 */
internal class PerformanceRepositoryImpl(
    authLocal: AuthLocalDataSource,
    private val performanceApi: PerformanceApiService,
) : CookieAwareRepository(authLocal),
    PerformanceRepository {
    override suspend fun fetchPerformance(): StudentPerformanceResponse? =
        withCookie { performanceApi.fetchPerformance(it) }

    override suspend fun fetchCourseExercises(courseId: Int): CourseExercisesResponse? =
        withCookie { performanceApi.fetchCourseExercises(it, courseId) }

    override suspend fun fetchCoursePerformance(courseId: Int): CourseStudentPerformanceResponse? =
        withCookie { performanceApi.fetchCoursePerformance(it, courseId) }

    override suspend fun fetchGradebook(): GradebookResponse? =
        withCookie { performanceApi.fetchGradebook(it) }
}
