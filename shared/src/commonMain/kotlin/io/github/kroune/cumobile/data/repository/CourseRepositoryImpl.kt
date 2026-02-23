package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.CourseOverview
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.CourseRepository

/**
 * Implementation of [CourseRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [ApiService].
 */
class CourseRepositoryImpl(
    authLocal: AuthLocalDataSource,
    apiService: ApiService,
) : CookieAwareRepository(authLocal, apiService),
    CourseRepository {
    override suspend fun fetchCourses(): List<Course>? =
        withCookie {
            apiService.fetchCourses(it)
        }

    override suspend fun fetchCourseOverview(courseId: Int): CourseOverview? =
        withCookie {
            apiService.fetchCourseOverview(it, courseId)
        }
}
