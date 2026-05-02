package io.github.kroune.cumobile.domain.usecase

import io.github.kroune.cumobile.domain.model.ClassDataDomain
import io.github.kroune.cumobile.domain.model.TimetableCalendarEventDomain
import io.github.kroune.cumobile.domain.model.TimetableCourseDomain
import io.github.kroune.cumobile.domain.model.TimetableEventRowDomain
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Filters LMS timetable events for a specific date and maps them to [ClassDataDomain].
 */
internal class GetClassesForDateUseCase {
    /**
     * Returns classes from the LMS timetable API for the given [dateMillis].
     */
    fun executeFromTimetable(
        courses: List<TimetableCourseDomain>,
        dateMillis: Long,
    ): List<ClassDataDomain> {
        val targetDate = Instant
            .fromEpochMilliseconds(dateMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        return courses
            .flatMap { course ->
                course.eventRows
                    .filter { row -> timetableEventOccursOn(row, targetDate) }
                    .map { row -> mapTimetableToClassDataDomain(row, course.courseName) }
            }.sortedBy { it.startTime }
    }

    internal fun timetableEventOccursOn(
        row: TimetableEventRowDomain,
        targetDate: LocalDate,
    ): Boolean {
        val event = row.calendarEvent ?: return false
        return try {
            scheduleOccursOn(event, targetDate)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to check timetable event on $targetDate" }
            false
        }
    }

    companion object {
        private const val DaysInWeek = 7

        internal fun scheduleOccursOn(
            event: TimetableCalendarEventDomain,
            targetDate: LocalDate,
        ): Boolean {
            val start = LocalDate.parse(event.startDate)
            val end = LocalDate.parse(event.endDate)

            if (targetDate !in start..end) return false

            val scheduleDow = parseDayOfWeek(event.dayOfWeek) ?: return false
            if (targetDate.dayOfWeek != scheduleDow) return false

            if (event.interval > 1) {
                val daysDiff = targetDate.toEpochDays() - start.toEpochDays()
                val weeksDiff = daysDiff / DaysInWeek
                if (weeksDiff % event.interval != 0L) return false
            }

            return true
        }

        internal fun mapTimetableToClassDataDomain(
            row: TimetableEventRowDomain,
            courseName: String,
        ): ClassDataDomain {
            val event = row.calendarEvent

            val type = when (row.eventType) {
                "lecture" -> "Лекция"
                "seminar" -> "Семинар"
                else -> row.eventType.replaceFirstChar { it.uppercase() }
            }

            return ClassDataDomain(
                startTime = event?.startTime.orEmpty(),
                endTime = event?.endTime.orEmpty(),
                room = event?.location.orEmpty(),
                type = type,
                title = courseName,
                professor = event?.hostName?.trim(),
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
    }
}
