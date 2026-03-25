package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.CalendarLocalDataSource
import io.github.kroune.cumobile.data.model.CalendarEvent
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.network.IcalApiService
import io.github.kroune.cumobile.domain.repository.CalendarRepository
import io.github.kroune.cumobile.domain.usecase.GetClassesForDateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Implementation of [CalendarRepository] using [CalendarLocalDataSource]
 * for storage and [IcalApiService] for fetching feeds.
 *
 * Delegates event filtering and mapping to [GetClassesForDateUseCase].
 */
internal class CalendarRepositoryImpl(
    private val localDataSource: CalendarLocalDataSource,
    private val icalApi: IcalApiService,
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

    override suspend fun getClassesForDate(dateMillis: Long): List<ClassData> {
        val events = fetchCalendar()
        return getClassesForDate.execute(events, dateMillis)
    }
}
