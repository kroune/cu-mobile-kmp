package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.CourseOverview
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.CourseRepository
import kotlinx.coroutines.flow.first

/**
 * Implementation of [CourseRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class CourseRepositoryImpl(
    private val authLocal: AuthLocalDataSource,
    private val apiService: ApiService,
) : CourseRepository {
    private suspend fun cookie(): String? = authLocal.cookieFlow.first()

    override suspend fun fetchCourses(): List<Course>? {
        val c = cookie() ?: return null
        return apiService.fetchCourses(c)
    }

    override suspend fun fetchCourseOverview(courseId: Int): CourseOverview? {
        val c = cookie() ?: return null
        return apiService.fetchCourseOverview(c, courseId)
    }
}
