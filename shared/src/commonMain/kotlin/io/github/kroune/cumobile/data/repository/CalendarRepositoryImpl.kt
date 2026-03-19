package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.CalendarLocalDataSource
import io.github.kroune.cumobile.data.model.CalendarEvent
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.network.IcalApiService
import io.github.kroune.cumobile.domain.repository.CalendarRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

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

        val targetDate = Instant
            .fromEpochMilliseconds(dateMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        return events
            .filter { event ->
                eventOccursOn(event, targetDate)
            }.map { event ->
                mapToClassData(event)
            }.sortedBy { it.startTime }
    }

    private fun eventOccursOn(
        event: CalendarEvent,
        targetDate: kotlinx.datetime.LocalDate,
    ): Boolean =
        try {
            val startDt = parseIcalDateTime(event.dtStart)
            val startLocalDate = startDt.toLocalDateTime(TimeZone.currentSystemDefault()).date
            startLocalDate == targetDate
        } catch (e: Exception) {
            logger.warn(e) { "Failed to check if event occurs on $targetDate" }
            false
        }

    private fun mapToClassData(event: CalendarEvent): ClassData {
        val startDt = parseIcalDateTime(event.dtStart).toLocalDateTime(TimeZone.currentSystemDefault())
        val endDt = parseIcalDateTime(event.dtEnd).toLocalDateTime(TimeZone.currentSystemDefault())

        val roomRegex = Regex("""(\d{3}[а-яА-Я]?)""")
        val room = roomRegex.find(event.summary)?.value
            ?: roomRegex.find(event.location.orEmpty())?.value
            ?: event.location.orEmpty()

        return ClassData(
            startTime = formatTimeComponent(startDt.hour, startDt.minute),
            endTime = formatTimeComponent(endDt.hour, endDt.minute),
            room = room,
            type = if (event.summary.contains("лекция", ignoreCase = true)) "Лекция" else "Практика",
            title = event.summary,
            link = event.url,
        )
    }

    private fun formatTimeComponent(
        hour: Int,
        minute: Int,
    ): String =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    private fun parseIcalDateTime(value: String): Instant {
        val clean = value.replace(Regex("[^0-9T]"), "")
        return if (clean.length < IcalFormat.DateTimeLen) {
            parseIcalDate(clean, value)
        } else {
            parseIcalFullDateTime(clean)
        }
    }

    private fun parseIcalDate(
        clean: String,
        original: String,
    ): Instant {
        if (clean.length >= IcalFormat.DateLen) {
            val year = clean.substring(0, IcalFormat.YearEnd).toInt()
            val month = clean.substring(IcalFormat.YearEnd, IcalFormat.MonthEnd).toInt()
            val day = clean.substring(IcalFormat.MonthEnd, IcalFormat.DayEnd).toInt()
            return LocalDateTime(year, month, day, 0, 0, 0).toInstant(TimeZone.UTC)
        }
        throw IllegalArgumentException("Invalid iCal date: $original")
    }

    private fun parseIcalFullDateTime(clean: String): Instant {
        val year = clean.substring(0, IcalFormat.YearEnd).toInt()
        val month = clean.substring(IcalFormat.YearEnd, IcalFormat.MonthEnd).toInt()
        val day = clean.substring(IcalFormat.MonthEnd, IcalFormat.DayEnd).toInt()
        val hour = clean.substring(IcalFormat.HourStart, IcalFormat.HourEnd).toInt()
        val minute = clean.substring(IcalFormat.HourEnd, IcalFormat.MinuteEnd).toInt()
        val second = clean.substring(IcalFormat.MinuteEnd, IcalFormat.SecondEnd).toInt()
        return LocalDateTime(year, month, day, hour, minute, second).toInstant(TimeZone.UTC)
    }

    private object IcalFormat {
        const val YearEnd = 4
        const val MonthEnd = 6
        const val DayEnd = 8
        const val DateLen = 8
        const val HourStart = 9
        const val HourEnd = 11
        const val MinuteEnd = 13
        const val SecondEnd = 15
        const val DateTimeLen = 15
    }
}
