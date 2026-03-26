package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.CalendarEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * Expands iCal RRULE recurrence rules to determine whether an event
 * occurs on a specific target date.
 *
 * Supports:
 * - `FREQ=WEEKLY` and `FREQ=DAILY`
 * - `INTERVAL=N`
 * - `UNTIL=<ical-date>`
 * - `COUNT=N`
 * - `BYDAY=MO,WE,FR`
 * - EXDATE exclusions
 */
internal object RRuleExpander {
    private const val DaysPerWeek = 7

    /**
     * Checks whether [event] occurs on [targetDate], considering its RRULE and EXDATE list.
     *
     * - If the event has no RRULE, only the literal start date is checked.
     * - If RRULE is present, recurrences are expanded up to [targetDate] to determine a match.
     */
    fun eventOccursOn(
        event: CalendarEvent,
        targetDate: LocalDate,
    ): Boolean {
        val startInstant = IcalDateParser.parse(event.dtStart)
        val startLocal = startInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date

        if (event.rRule.isNullOrBlank()) {
            return startLocal == targetDate
        }

        return occursOnWithRRule(event.rRule, event.exDates, startLocal, targetDate)
    }

    private fun occursOnWithRRule(
        rRule: String,
        rawExDates: List<String>,
        startLocal: LocalDate,
        targetDate: LocalDate,
    ): Boolean {
        if (targetDate < startLocal) return false

        val rule = parseRRule(rRule)
        val exDates = parseExDates(rawExDates)

        if (targetDate in exDates) return false

        return when (rule.freq) {
            Frequency.WEEKLY -> matchesWeekly(startLocal, targetDate, rule)
            Frequency.DAILY -> matchesDaily(startLocal, targetDate, rule)
        }
    }

    private fun matchesWeekly(
        startLocal: LocalDate,
        targetDate: LocalDate,
        rule: ParsedRRule,
    ): Boolean {
        val byDay = rule.byDay.ifEmpty { setOf(startLocal.dayOfWeek) }

        if (targetDate.dayOfWeek !in byDay) return false

        if (rule.until != null && targetDate > rule.until) return false

        val startMonday = mondayOf(startLocal)
        val targetMonday = mondayOf(targetDate)
        val weeksDiff = (targetMonday.toEpochDays() - startMonday.toEpochDays()) / DaysPerWeek

        if (weeksDiff % rule.interval != 0L) return false

        if (rule.count != null) {
            val occurrencesBefore = countOccurrencesBefore(startLocal, targetDate, rule, byDay)
            if (occurrencesBefore >= rule.count) return false
        }

        return true
    }

    /**
     * Counts how many BYDAY occurrences happen from [startLocal] up to
     * (but not including) [targetDate] to support COUNT limits.
     */
    private fun countOccurrencesBefore(
        startLocal: LocalDate,
        targetDate: LocalDate,
        rule: ParsedRRule,
        byDay: Set<DayOfWeek>,
    ): Int {
        var count = 0
        var weekMonday = mondayOf(startLocal)
        val targetMonday = mondayOf(targetDate)

        while (weekMonday <= targetMonday) {
            for (day in byDay) {
                val candidate = weekMonday.plus(day.ordinal, DateTimeUnit.DAY)
                if (candidate < startLocal) continue
                if (candidate >= targetDate) return count
                count++
            }
            weekMonday = weekMonday.plus(rule.interval * DaysPerWeek, DateTimeUnit.DAY)
        }
        return count
    }

    private fun mondayOf(date: LocalDate): LocalDate =
        date.plus(-(date.dayOfWeek.ordinal), DateTimeUnit.DAY)

    private fun matchesDaily(
        startLocal: LocalDate,
        targetDate: LocalDate,
        rule: ParsedRRule,
    ): Boolean {
        val interval = rule.interval.toLong()
        val daysDiff = targetDate.toEpochDays() - startLocal.toEpochDays()

        if (daysDiff % interval != 0L) return false

        if (rule.until != null && targetDate > rule.until) return false

        if (rule.count != null) {
            val occurrenceIndex = daysDiff / interval
            if (occurrenceIndex >= rule.count) return false
        }

        return true
    }

    internal fun parseRRule(rrule: String): ParsedRRule {
        val parts = rrule
            .split(';')
            .mapNotNull { part ->
                val segments = part.split('=', limit = 2)
                if (segments.size == 2) {
                    segments[0].uppercase() to segments[1]
                } else {
                    logger.warn { "Malformed RRULE part (missing '='): $part" }
                    null
                }
            }.toMap()

        val freq = when (parts["FREQ"]?.uppercase()) {
            "WEEKLY" -> Frequency.WEEKLY
            "DAILY" -> Frequency.DAILY
            else -> {
                logger.warn { "Unsupported RRULE FREQ: ${parts["FREQ"]}, defaulting to WEEKLY" }
                Frequency.WEEKLY
            }
        }

        val interval = parts["INTERVAL"]?.toIntOrNull() ?: 1

        val until = parts["UNTIL"]?.let { untilStr ->
            try {
                IcalDateParser
                    .parse(untilStr)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            } catch (e: Exception) {
                logger.warn(e) { "Failed to parse UNTIL: $untilStr" }
                null
            }
        }

        val count = parts["COUNT"]?.toIntOrNull()

        val byDay = parts["BYDAY"]
            ?.split(',')
            ?.mapNotNull { dayAbbrevToKotlin(it.trim()) }
            ?.toSet()
            .orEmpty()

        return ParsedRRule(
            freq = freq,
            interval = interval,
            until = until,
            count = count,
            byDay = byDay,
        )
    }

    internal fun parseExDates(exDates: List<String>): Set<LocalDate> =
        exDates
            .mapNotNull { raw ->
                try {
                    IcalDateParser
                        .parse(raw.trim())
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to parse EXDATE: $raw" }
                    null
                }
            }.toSet()

    private fun dayAbbrevToKotlin(abbrev: String): DayOfWeek? =
        when (abbrev.uppercase()) {
            "MO" -> DayOfWeek.MONDAY
            "TU" -> DayOfWeek.TUESDAY
            "WE" -> DayOfWeek.WEDNESDAY
            "TH" -> DayOfWeek.THURSDAY
            "FR" -> DayOfWeek.FRIDAY
            "SA" -> DayOfWeek.SATURDAY
            "SU" -> DayOfWeek.SUNDAY
            else -> {
                logger.warn { "Unknown BYDAY abbreviation: $abbrev" }
                null
            }
        }

    internal data class ParsedRRule(
        val freq: Frequency,
        val interval: Int = 1,
        val until: LocalDate? = null,
        val count: Int? = null,
        val byDay: Set<DayOfWeek> = emptySet(),
    )

    internal enum class Frequency {
        WEEKLY,
        DAILY,
    }
}
