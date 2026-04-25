package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.model.TimetableCourse
import io.github.kroune.cumobile.data.network.TimetableApiService
import io.github.kroune.cumobile.domain.repository.CalendarRepository
import io.github.kroune.cumobile.domain.usecase.GetClassesForDateUseCase
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.util.AppDispatchers
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
    override suspend fun fetchTimetable(): List<TimetableCourse>? =
        withContext(dispatchers().io) {
            val cookie = authLocal().cookieFlow.first() ?: return@withContext null
            timetableApi().fetchTimetable(cookie)
        }

    override suspend fun getClassesForDate(dateMillis: Long): List<ClassData> =
        withContext(dispatchers().io) {
            val timetable = fetchTimetable() ?: return@withContext emptyList()
            getClassesForDate().executeFromTimetable(timetable, dateMillis)
        }
}
