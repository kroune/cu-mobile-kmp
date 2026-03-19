package io.github.kroune.cumobile.presentation.common

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock.System

/**
 * Provider for current date and time information.
 *
 * Uses kotlinx-datetime for cross-platform compatibility.
 */
class DateTimeProvider {
    /** Returns the current time in milliseconds since epoch. */
    fun nowMillis(): Long =
        System.now().toEpochMilliseconds()

    /** Returns milliseconds for today at 00:00:00 in the system default timezone. */
    fun todayMillis(): Long {
        val now = System.now()
        val localDateTime: LocalDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val todayStart = LocalDateTime(
            year = localDateTime.year,
            month = localDateTime.month,
            day = localDateTime.day,
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0,
        )
        return todayStart.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    /** Returns milliseconds for a specific date at 00:00:00. */
    fun dateToMillis(
        year: Int,
        month: Int,
        day: Int,
    ): Long {
        val dt = LocalDateTime(year, month, day, 0, 0, 0, 0)
        return dt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }
}
