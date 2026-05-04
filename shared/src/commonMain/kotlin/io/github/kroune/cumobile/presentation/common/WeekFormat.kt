package io.github.kroune.cumobile.presentation.common

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.plus

private val dayAndMonthFormat = LocalDate.Format {
    day(Padding.NONE)
    char(' ')
    monthName(russianMonthsFull)
}

/**
 * Formats a week range starting at [weekStart] (Monday) as e.g. `"1 - 7 февраля"`.
 * If the week spans two months, returns `"30 января - 5 февраля"`.
 */
fun formatWeekRange(weekStart: LocalDate): String {
    val endDate = weekStart + DatePeriod(days = 6)
    val startDay = weekStart.day
    val endDay = endDate.day

    val endFormatted = dayAndMonthFormat.format(endDate)
    val monthName = endFormatted.substringAfter(' ')

    return if (weekStart.month == endDate.month) {
        "$startDay - $endDay $monthName"
    } else {
        val startFormatted = dayAndMonthFormat.format(weekStart)
        "$startFormatted - $endFormatted"
    }
}
