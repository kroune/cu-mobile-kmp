package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.local.CalendarLocalDataSource
import io.github.kroune.cumobile.data.model.CalendarEvent
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.model.TimetableCourse
import io.github.kroune.cumobile.data.network.IcalApiService
import io.github.kroune.cumobile.data.network.TimetableApiService
import io.github.kroune.cumobile.domain.repository.CalendarRepository
import io.github.kroune.cumobile.domain.usecase.GetClassesForDateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Implementation of [CalendarRepository].
 *
 * Primary source: LMS timetable API (works automatically for authenticated students).
 * Fallback: iCal feed (user-configured URL).
 */
internal class CalendarRepositoryImpl(
    private val localDataSource: CalendarLocalDataSource,
    private val icalApi: IcalApiService,
    private val timetableApi: TimetableApiService,
    private val authLocal: AuthLocalDataSource,
    private val getClassesForDate: GetClassesForDateUseCase,
) : CalendarRepository {
    override val calendarUrlFlow: Flow<String?> = localDataSource.calendarUrlFlow

    override suspend fun saveCalendarUrl(url: String?) {
        localDataSource.saveCalendarUrl(url)
    }

    override suspend fun fetchCalendar(): List<CalendarEvent> {
        val url = calendarUrlFlow.first() ?: return emptyList()
        return icalApi.fetchCalendar(url)
    }

    override suspend fun fetchTimetable(): List<TimetableCourse>? {
        val cookie = authLocal.cookieFlow.first() ?: return null
        return timetableApi.fetchTimetable(cookie)
    }

    override suspend fun getClassesForDate(dateMillis: Long): List<ClassData> {
        val timetable = fetchTimetable()
        if (timetable != null) {
            return getClassesForDate.executeFromTimetable(timetable, dateMillis)
        }
        return emptyList()
    }
}
