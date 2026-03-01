package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.CalendarLocalDataSource
import io.github.kroune.cumobile.data.model.CalendarEvent
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.network.IcalApiService
import io.github.kroune.cumobile.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Implementation of [CalendarRepository] using [CalendarLocalDataSource]
 * for storage and [IcalApiService] for fetching feeds.
 */
internal class CalendarRepositoryImpl(
    private val localDataSource: CalendarLocalDataSource,
    private val icalApi: IcalApiService,
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
        if (events.isEmpty()) return emptyList()

        val targetDate = Instant.fromEpochMilliseconds(dateMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        return events.filter { event ->
            eventOccursOn(event, targetDate)
        }.map { event ->
            mapToClassData(event, targetDate)
        }.sortedBy { it.startTime }
    }

    private fun eventOccursOn(event: CalendarEvent, date: kotlinx.datetime.LocalDate): Boolean {
        // Basic implementation for now: check if start date matches
        // TODO: Implement full RRULE expansion if needed for repeating classes
        return try {
            val startDt = parseIcalDateTime(event.dtStart)
            val startLocalDate = startDt.toLocalDateTime(TimeZone.currentSystemDefault()).date
            startLocalDate == date
        } catch (e: Exception) {
            false
        }
    }

    private fun mapToClassData(event: CalendarEvent, date: kotlinx.datetime.LocalDate): ClassData {
        val startDt = parseIcalDateTime(event.dtStart).toLocalDateTime(TimeZone.currentSystemDefault())
        val endDt = parseIcalDateTime(event.dtEnd).toLocalDateTime(TimeZone.currentSystemDefault())

        // Extract room from SUMMARY or LOCATION (often like "Room 301")
        val roomRegex = Regex("""(\d{3}[а-яА-Я]?)""")
        val room = roomRegex.find(event.summary)?.value
            ?: roomRegex.find(event.location ?: "")?.value
            ?: event.location ?: ""

        return ClassData(
            startTime = "${startDt.hour.toString().padStart(2, '0')}:${startDt.minute.toString().padStart(2, '0')}",
            endTime = "${endDt.hour.toString().padStart(2, '0')}:${endDt.minute.toString().padStart(2, '0')}",
            room = room,
            type = if (event.summary.contains("лекция", ignoreCase = true)) "Лекция" else "Практика",
            title = event.summary,
            link = event.url,
        )
    }

    private fun parseIcalDateTime(value: String): Instant {
        // iCal format: "20260223T090000Z" or "20260223T090000"
        val clean = value.replace(Regex("[^0-9T]"), "")
        if (clean.length < 15) {
            // Probably just date? Handle it
            if (clean.length >= 8) {
                val year = clean.substring(0, 4).toInt()
                val month = clean.substring(4, 6).toInt()
                val day = clean.substring(6, 8).toInt()
                return LocalDateTime(year, month, day, 0, 0, 0).toInstant(TimeZone.UTC)
            }
            throw IllegalArgumentException("Invalid iCal date: $value")
        }
        val year = clean.substring(0, 4).toInt()
        val month = clean.substring(4, 6).toInt()
        val day = clean.substring(6, 8).toInt()
        val hour = clean.substring(9, 11).toInt()
        val minute = clean.substring(11, 13).toInt()
        val second = clean.substring(13, 15).toInt()
        return LocalDateTime(year, month, day, hour, minute, second).toInstant(TimeZone.UTC)
    }
}
