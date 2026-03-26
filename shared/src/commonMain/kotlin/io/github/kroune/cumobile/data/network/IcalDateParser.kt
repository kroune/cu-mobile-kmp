package io.github.kroune.cumobile.data.network

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Instant

/**
 * Parses iCal date/time strings into [Instant] values.
 *
 * Supports both full date-time ("20260301T130000Z") and date-only ("20260301") formats.
 */
internal object IcalDateParser {
    private const val YEAR_END = 4
    private const val MONTH_END = 6
    private const val DAY_END = 8
    private const val DATE_LEN = 8
    private const val HOUR_START = 9
    private const val HOUR_END = 11
    private const val MINUTE_END = 13
    private const val SECOND_END = 15
    private const val DATE_TIME_LEN = 15

    /**
     * Parses an iCal date/time string into an [Instant].
     *
     * Accepted formats:
     * - Full: `20260301T130000Z` or `20260301T130000`
     * - Date only: `20260301`
     *
     * The "Z" suffix and any non-digit/T characters are stripped before parsing.
     * All values are interpreted as UTC.
     */
    fun parse(value: String): Instant {
        val clean = value.replace(Regex("[^0-9T]"), "")
        return if (clean.length < DATE_TIME_LEN) {
            parseDate(clean, value)
        } else {
            parseFullDateTime(clean)
        }
    }

    private fun parseDate(
        clean: String,
        original: String,
    ): Instant {
        if (clean.length >= DATE_LEN) {
            val year = clean.substring(0, YEAR_END).toInt()
            val month = clean.substring(YEAR_END, MONTH_END).toInt()
            val day = clean.substring(MONTH_END, DAY_END).toInt()
            return LocalDateTime(year, month, day, 0, 0, 0).toInstant(TimeZone.UTC)
        }
        throw IllegalArgumentException("Invalid iCal date: $original")
    }

    private fun parseFullDateTime(clean: String): Instant {
        val year = clean.substring(0, YEAR_END).toInt()
        val month = clean.substring(YEAR_END, MONTH_END).toInt()
        val day = clean.substring(MONTH_END, DAY_END).toInt()
        val hour = clean.substring(HOUR_START, HOUR_END).toInt()
        val minute = clean.substring(HOUR_END, MINUTE_END).toInt()
        val second = clean.substring(MINUTE_END, SECOND_END).toInt()
        return LocalDateTime(year, month, day, hour, minute, second).toInstant(TimeZone.UTC)
    }
}
