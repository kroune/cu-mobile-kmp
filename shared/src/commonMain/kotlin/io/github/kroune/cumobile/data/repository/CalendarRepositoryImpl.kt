package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.mappers.toDomain
import io.github.kroune.cumobile.data.network.TimetableApiService
import io.github.kroune.cumobile.domain.model.ClassDataDomain
import io.github.kroune.cumobile.domain.model.TimetableCourseDomain
import io.github.kroune.cumobile.domain.repository.CalendarRepository
import io.github.kroune.cumobile.domain.usecase.GetClassesForDateUseCase
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.kroune.cumobile.util.invoke
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Implementation of [CalendarRepository] using the LMS timetable API.
 */
internal class CalendarRepositoryImpl(
    private val timetableApi: Lazy<TimetableApiService>,
    private val authLocal: Lazy<AuthLocalDataSource>,
    private val getClassesForDate: Lazy<GetClassesForDateUseCase>,
    private val dispatchers: Lazy<AppDispatchers>,
) : CalendarRepository {
    override suspend fun fetchTimetable(): List<TimetableCourseDomain>? =
        withContext(dispatchers().io) {
            val cookie = authLocal().cookieFlow.first() ?: return@withContext null
            timetableApi().fetchTimetable(cookie)?.map { it.toDomain() }
        }

    override suspend fun getClassesForDate(dateMillis: Long): List<ClassDataDomain> =
        withContext(dispatchers().io) {
            val timetable = fetchTimetable() ?: return@withContext emptyList()
            getClassesForDate().executeFromTimetable(timetable, dateMillis)
        }
}
