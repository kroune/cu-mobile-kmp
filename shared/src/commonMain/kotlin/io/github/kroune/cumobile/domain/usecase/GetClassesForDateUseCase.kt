package io.github.kroune.cumobile.domain.usecase

import io.github.kroune.cumobile.data.model.CalendarEvent
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.model.TimetableCourse
import io.github.kroune.cumobile.data.model.TimetableEventRow
import io.github.kroune.cumobile.data.model.TimetableSchedule
import io.github.kroune.cumobile.data.network.IcalDateParser
import io.github.kroune.cumobile.data.network.RRuleExpander
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Filters timetable or iCal events for a specific date and maps them to [ClassData].
 */
internal class GetClassesForDateUseCase {
    /**
     * Returns classes from the LMS timetable API for the given [dateMillis].
     */
    fun executeFromTimetable(
        courses: List<TimetableCourse>,
        dateMillis: Long,
    ): List<ClassData> {
        val targetDate = Instant
            .fromEpochMilliseconds(dateMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        return courses
            .flatMap { course ->
                course.eventRows
                    .filter { row -> timetableEventOccursOn(row, targetDate) }
                    .map { row -> mapTimetableToClassData(row, course.courseName) }
            }.sortedBy { it.startTime }
    }

    internal fun timetableEventOccursOn(
        row: TimetableEventRow,
        targetDate: LocalDate,
    ): Boolean {
        val schedule = row.calendarEvent?.schedule ?: return false
        return try {
            scheduleOccursOn(schedule, targetDate)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to check timetable event on $targetDate" }
            false
        }
    }

    // --- iCal support (kept for fallback) ---

    fun execute(
        events: List<CalendarEvent>,
        dateMillis: Long,
    ): List<ClassData> {
        val targetDate = Instant
            .fromEpochMilliseconds(dateMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        return events
            .filter { event -> eventOccursOn(event, targetDate) }
            .map { event -> mapToClassData(event) }
            .sortedBy { it.startTime }
    }

    internal fun eventOccursOn(
        event: CalendarEvent,
        targetDate: LocalDate,
    ): Boolean =
        try {
            RRuleExpander.eventOccursOn(event, targetDate)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to check if event occurs on $targetDate" }
            false
        }

    internal fun mapToClassData(event: CalendarEvent): ClassData {
        val startDt = IcalDateParser
            .parse(event.dtStart)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val endDt = try {
            IcalDateParser
                .parse(event.dtEnd)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            logger.warn(e) { "Failed to parse dtEnd '${event.dtEnd}', falling back to startDt" }
            startDt
        }

        return ClassData(
            startTime = formatTime(startDt.hour, startDt.minute),
            endTime = formatTime(endDt.hour, endDt.minute),
            room = extractRoom(event.summary, event.location),
            type = detectType(event.summary),
            title = event.summary,
            link = event.url,
        )
    }

    companion object {
        private const val DaysInWeek = 7

        internal fun scheduleOccursOn(
            schedule: TimetableSchedule,
            targetDate: LocalDate,
        ): Boolean {
            val start = LocalDate.parse(schedule.startDate)
            val end = LocalDate.parse(schedule.endDate)

            if (targetDate < start || targetDate > end) return false

            val scheduleDow = parseDayOfWeek(schedule.dayOfWeek) ?: return false
            if (targetDate.dayOfWeek != scheduleDow) return false

            if (schedule.interval > 1) {
                val daysDiff = targetDate.toEpochDays() - start.toEpochDays()
                val weeksDiff = daysDiff / DaysInWeek
                if (weeksDiff % schedule.interval != 0L) return false
            }

            return true
        }

        internal fun mapTimetableToClassData(
            row: TimetableEventRow,
            courseName: String,
        ): ClassData {
            val event = row.calendarEvent
            val schedule = event?.schedule

            val type = when (row.eventType) {
                "lecture" -> "Лекция"
                "seminar" -> "Семинар"
                else -> row.eventType.replaceFirstChar { it.uppercase() }
            }

            return ClassData(
                startTime = schedule?.startTime.orEmpty(),
                endTime = schedule?.endTime.orEmpty(),
                room = event?.location.orEmpty(),
                type = type,
                title = courseName,
                professor = event?.host?.name?.trim(),
                link = null,
            )
        }

        internal fun parseDayOfWeek(day: String): DayOfWeek? =
            when (day.lowercase()) {
                "monday" -> DayOfWeek.MONDAY
                "tuesday" -> DayOfWeek.TUESDAY
                "wednesday" -> DayOfWeek.WEDNESDAY
                "thursday" -> DayOfWeek.THURSDAY
                "friday" -> DayOfWeek.FRIDAY
                "saturday" -> DayOfWeek.SATURDAY
                "sunday" -> DayOfWeek.SUNDAY
                else -> null
            }

        internal fun extractRoom(
            summary: String,
            location: String?,
        ): String {
            val roomRegex = Regex("""(\d{3}[а-яА-Я]?)""")
            return roomRegex.find(summary)?.value
                ?: roomRegex.find(location.orEmpty())?.value
                ?: location.orEmpty()
        }

        internal fun detectType(summary: String): String =
            if (summary.contains("лекция", ignoreCase = true)) "Лекция" else "Практика"

        internal fun formatTime(
            hour: Int,
            minute: Int,
        ): String =
            "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }
}
