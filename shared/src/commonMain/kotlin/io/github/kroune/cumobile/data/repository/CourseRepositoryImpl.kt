package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.local.CourseLocalDataSource
import io.github.kroune.cumobile.data.model.mappers.toDomain
import io.github.kroune.cumobile.data.network.CourseApiService
import io.github.kroune.cumobile.domain.model.CourseDomain
import io.github.kroune.cumobile.domain.model.CourseOverviewDomain
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.kroune.cumobile.util.invoke
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
    override suspend fun fetchCourses(): List<CourseDomain>? =
        withCookie {
            courseApiLazy().fetchCourses(it)?.map { course -> course.toDomain() }
        }

    override suspend fun fetchCourseOverview(courseId: String): CourseOverviewDomain? =
        withCookie {
            courseApiLazy().fetchCourseOverview(it, courseId)?.toDomain()
        }

    override val courseIdOrderFlow: Flow<List<String>> by lazy { courseLocalLazy().courseIdOrderFlow }

    override suspend fun saveCourseIdOrder(ids: List<String>) =
        courseLocalLazy().saveCourseIdOrder(ids)
}
