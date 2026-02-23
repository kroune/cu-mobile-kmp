package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.CourseExercisesResponse
import io.github.kroune.cumobile.data.model.CourseStudentPerformanceResponse
import io.github.kroune.cumobile.data.model.GradebookResponse
import io.github.kroune.cumobile.data.model.StudentPerformanceResponse
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import kotlinx.coroutines.flow.first

/**
 * Implementation of [PerformanceRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class PerformanceRepositoryImpl(
    private val authLocal: AuthLocalDataSource,
    private val apiService: ApiService,
) : PerformanceRepository {
    private suspend fun cookie(): String? = authLocal.cookieFlow.first()

    override suspend fun fetchPerformance(): StudentPerformanceResponse? {
        val c = cookie() ?: return null
        return apiService.fetchPerformance(c)
    }

    override suspend fun fetchCourseExercises(courseId: Int): CourseExercisesResponse? {
        val c = cookie() ?: return null
        return apiService.fetchCourseExercises(c, courseId)
    }

    override suspend fun fetchCoursePerformance(courseId: Int): CourseStudentPerformanceResponse? {
        val c = cookie() ?: return null
        return apiService.fetchCoursePerformance(c, courseId)
    }

    override suspend fun fetchGradebook(): GradebookResponse? {
        val c = cookie() ?: return null
        return apiService.fetchGradebook(c)
    }
}
