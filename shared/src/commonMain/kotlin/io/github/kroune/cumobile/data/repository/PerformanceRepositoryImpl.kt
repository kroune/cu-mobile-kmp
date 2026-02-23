package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.CourseExercisesResponse
import io.github.kroune.cumobile.data.model.CourseStudentPerformanceResponse
import io.github.kroune.cumobile.data.model.GradebookResponse
import io.github.kroune.cumobile.data.model.StudentPerformanceResponse
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.PerformanceRepository

/**
 * Implementation of [PerformanceRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class PerformanceRepositoryImpl(
    authLocal: AuthLocalDataSource,
    apiService: ApiService,
) : CookieAwareRepository(authLocal, apiService),
    PerformanceRepository {
    override suspend fun fetchPerformance(): StudentPerformanceResponse? =
        withCookie {
            apiService.fetchPerformance(it)
        }

    override suspend fun fetchCourseExercises(courseId: Int): CourseExercisesResponse? =
        withCookie {
            apiService.fetchCourseExercises(it, courseId)
        }

    override suspend fun fetchCoursePerformance(courseId: Int): CourseStudentPerformanceResponse? =
        withCookie {
            apiService.fetchCoursePerformance(it, courseId)
        }

    override suspend fun fetchGradebook(): GradebookResponse? =
        withCookie {
            apiService.fetchGradebook(it)
        }
}
