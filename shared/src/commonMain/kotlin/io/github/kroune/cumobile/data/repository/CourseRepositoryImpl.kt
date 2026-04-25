package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.local.CourseLocalDataSource
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.CourseOverview
import io.github.kroune.cumobile.data.network.CourseApiService
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.util.AppDispatchers
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of [CourseRepository].
 *
 * Retrieves the auth cookie from [AuthLocalDataSource] and delegates
 * all network calls to [CourseApiService].
 */
internal class CourseRepositoryImpl(
    authLocal: Lazy<AuthLocalDataSource>,
    private val courseLocalLazy: Lazy<CourseLocalDataSource>,
    private val courseApiLazy: Lazy<CourseApiService>,
    dispatchers: Lazy<AppDispatchers>,
) : CookieAwareRepository(authLocal, dispatchers),
    CourseRepository {
    override suspend fun fetchCourses(): List<Course>? =
        withCookie {
            courseApiLazy().fetchCourses(it)
        }

    override suspend fun fetchCourseOverview(courseId: String): CourseOverview? =
        withCookie {
            courseApiLazy().fetchCourseOverview(it, courseId)
        }

    override val courseIdOrderFlow: Flow<List<String>> by lazy { courseLocalLazy().courseIdOrderFlow }

    override suspend fun saveCourseIdOrder(ids: List<String>) =
        courseLocalLazy().saveCourseIdOrder(ids)
}
