@file:Suppress("MaxLineLength")

package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.CourseOverview
import io.github.kroune.cumobile.data.network.CourseApiService
import io.github.kroune.cumobile.domain.repository.CourseRepository

/**
 * Implementation of [CourseRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [CourseApiService].
 */
internal class CourseRepositoryImpl(
    authLocal: AuthLocalDataSource,
    private val courseApi: CourseApiService,
) : CookieAwareRepository(authLocal),
    CourseRepository {
    override suspend fun fetchCourses(): List<Course>? = withCookie { courseApi.fetchCourses(it) }

    override suspend fun fetchCourseOverview(courseId: Int): CourseOverview? = withCookie { courseApi.fetchCourseOverview(it, courseId) }
}
