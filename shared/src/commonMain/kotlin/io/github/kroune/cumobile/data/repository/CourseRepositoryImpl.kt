package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.local.CourseLocalDataSource
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.CourseOverview
import io.github.kroune.cumobile.data.network.CourseApiService
import io.github.kroune.cumobile.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of [CourseRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [CourseApiService].
 */
internal class CourseRepositoryImpl(
    authLocal: AuthLocalDataSource,
    private val courseLocal: CourseLocalDataSource,
    private val courseApi: CourseApiService,
) : CookieAwareRepository(authLocal), CourseRepository {
    override suspend fun fetchCourses(): List<Course>? = withCookie {
        courseApi.fetchCourses(it)
    }

    override suspend fun fetchCourseOverview(courseId: String): CourseOverview? = withCookie {
        courseApi.fetchCourseOverview(it, courseId)
    }

    override val courseIdOrderFlow: Flow<List<String>> = courseLocal.courseIdOrderFlow

    override suspend fun saveCourseIdOrder(
        ids: List<String>,
    ) = courseLocal.saveCourseIdOrder(ids)
}
