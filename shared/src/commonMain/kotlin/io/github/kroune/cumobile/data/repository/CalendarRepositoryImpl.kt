package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.model.TimetableCourse
import io.github.kroune.cumobile.data.network.TimetableApiService
import io.github.kroune.cumobile.domain.repository.CalendarRepository
import io.github.kroune.cumobile.domain.usecase.GetClassesForDateUseCase
import kotlinx.coroutines.flow.first

/**
 * Implementation of [CalendarRepository] using the LMS timetable API.
 */
internal class CalendarRepositoryImpl(
    private val timetableApi: TimetableApiService,
    private val authLocal: AuthLocalDataSource,
    private val getClassesForDate: GetClassesForDateUseCase,
) : CalendarRepository {
    override suspend fun fetchTimetable(): List<TimetableCourse>? {
        val cookie = authLocal.cookieFlow.first() ?: return null
        return timetableApi.fetchTimetable(cookie)
    }

    override suspend fun getClassesForDate(dateMillis: Long): List<ClassData> {
        val timetable = fetchTimetable() ?: return emptyList()
        return getClassesForDate.executeFromTimetable(timetable, dateMillis)
    }
}
