package io.github.kroune.cumobile.domain.usecase

import io.github.kroune.cumobile.data.model.CalendarEvent
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.network.IcalDateParser
import io.github.kroune.cumobile.data.network.RRuleExpander
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Filters a list of [CalendarEvent]s for a specific date and maps them to [ClassData].
 *
 * Handles recurring events via [RRuleExpander] and parses iCal dates via [IcalDateParser].
 */
internal class GetClassesForDateUseCase {
    /**
     * Returns classes occurring on the given [dateMillis], sorted by start time.
     */
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

        val room = extractRoom(event.summary, event.location)
        val type = detectType(event.summary)

        return ClassData(
            startTime = formatTime(startDt.hour, startDt.minute),
            endTime = formatTime(endDt.hour, endDt.minute),
            room = room,
            type = type,
            title = event.summary,
            link = event.url,
        )
    }

    companion object {
        private val roomRegex = Regex("""(\d{3}[а-яА-Я]?)""")

        internal fun extractRoom(
            summary: String,
            location: String?,
        ): String =
            roomRegex.find(summary)?.value
                ?: roomRegex.find(location.orEmpty())?.value
                ?: location.orEmpty()

        internal fun detectType(summary: String): String =
            if (summary.contains("лекция", ignoreCase = true)) "Лекция" else "Практика"

        internal fun formatTime(
            hour: Int,
            minute: Int,
        ): String =
            "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }
}
