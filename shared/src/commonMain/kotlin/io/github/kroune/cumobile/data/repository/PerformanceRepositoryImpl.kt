package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.mappers.toDomain
import io.github.kroune.cumobile.data.network.PerformanceApiService
import io.github.kroune.cumobile.domain.model.CourseExerciseDomain
import io.github.kroune.cumobile.domain.model.CourseGradeDomain
import io.github.kroune.cumobile.domain.model.ExerciseScoreDomain
import io.github.kroune.cumobile.domain.model.GradebookResponseDomain
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.kroune.cumobile.util.invoke

/**
 * Implementation of [PerformanceRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [PerformanceApiService].
 */
internal class PerformanceRepositoryImpl(
    authLocal: Lazy<AuthLocalDataSource>,
    private val performanceApi: Lazy<PerformanceApiService>,
    dispatchers: Lazy<AppDispatchers>,
) : CookieAwareRepository(authLocal, dispatchers),
    PerformanceRepository {
    override suspend fun fetchPerformance(): List<CourseGradeDomain>? {
        val response = withCookie { performanceApi().fetchPerformance(it) }
        return response?.courses?.map { it.toDomain() }
    }

    override suspend fun fetchCourseExercises(courseId: String): List<CourseExerciseDomain>? {
        val response = withCookie { performanceApi().fetchCourseExercises(it, courseId) }
        return response?.exercises?.map { it.toDomain() }
    }

    override suspend fun fetchCoursePerformance(courseId: String): List<ExerciseScoreDomain>? {
        val response = withCookie { performanceApi().fetchCoursePerformance(it, courseId) }
        return response?.tasks?.map { it.toDomain() }
    }

    override suspend fun fetchGradebook(): GradebookResponseDomain? =
        withCookie { performanceApi().fetchGradebook(it) }?.toDomain()
}
