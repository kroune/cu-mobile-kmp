@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.common

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.roundToLong

private val logger = KotlinLogging.logger {}

// ──────────────────────────────────────────────────────
// ISO 8601 date/time formatting
// ──────────────────────────────────────────────────────

/**
 * Formats an ISO 8601 deadline string to a short display format.
 *
 * Example: `"2026-02-15T14:00:00"` → `"15.02 14:00"`
 *
 * Returns `"Без дедлайна"` when [isoDate] is null.
 * Falls back to the raw string on parse errors.
 */
fun formatDeadline(isoDate: String?): String {
    if (isoDate == null) return "Без дедлайна"
    return try {
        val parts = isoDate.split("T")
        if (parts.size < 2) return isoDate
        val dateParts = parts[0].split("-")
        val timeParts = parts[1].split(":")
        if (dateParts.size < 3 || timeParts.size < 2) return isoDate
        "${dateParts[2]}.${dateParts[1]} ${timeParts[0]}:${timeParts[1]}"
    } catch (e: Exception) {
        logger.error(e) { "Failed to format deadline: $isoDate" }
        isoDate
    }
}

/**
 * Formats an ISO 8601 datetime string to `"dd.MM HH:mm"`.
 *
 * Example: `"2026-02-15T14:00:00.000Z"` → `"15.02 14:00"`
 *
 * Falls back to the raw string on parse errors.
 */
fun formatDateTime(dateTime: String): String {
    return try {
        val parts = dateTime.split("T")
        if (parts.size < 2) return dateTime
        val dateParts = parts[0].split("-")
        val timeParts = parts[1].split(":")
        if (dateParts.size < 3 || timeParts.size < 2) return dateTime
        "${dateParts[2]}.${dateParts[1]} ${timeParts[0]}:${timeParts[1]}"
    } catch (e: Exception) {
        logger.error(e) { "Failed to format datetime: $dateTime" }
        dateTime
    }
}

/**
 * Formats an ISO 8601 datetime string to `"dd.MM.yyyy HH:mm"`.
 *
 * Example: `"2026-02-22T14:30:00Z"` → `"22.02.2026 14:30"`
 *
 * Falls back to the raw string on parse errors.
 */
fun formatDateTimeFull(isoDate: String): String {
    if (isoDate.length < 16) return isoDate
    val date = isoDate.substring(0, 10) // "2026-02-22"
    val time = isoDate.substring(11, 16) // "14:30"
    val parts = date.split("-")
    if (parts.size != 3) return isoDate
    return "${parts[2]}.${parts[1]}.${parts[0]} $time"
}

/**
 * Formats an ISO 8601 deadline string to a short day-month display.
 *
 * Example: `"2025-06-01T23:59:00Z"` → `"01.06"`
 *
 * Falls back to the raw string on parse errors.
 */
fun formatDeadlineShort(deadline: String): String {
    if (deadline.length < 10) return deadline
    val datePart = deadline.substring(0, 10)
    val parts = datePart.split("-")
    return if (parts.size == 3) {
        "${parts[2]}.${parts[1]}"
    } else {
        deadline
    }
}

// ──────────────────────────────────────────────────────
// Epoch millis → date
// ──────────────────────────────────────────────────────

/**
 * Formats a millisecond timestamp to `"dd.MM.yyyy"`.
 *
 * Uses manual calculation to avoid JVM-only APIs.
 */
fun formatEpochDate(millis: Long): String {
    if (millis <= 0L) return ""
    val totalDays = (millis / 86_400_000L).toInt()
    val (day, month, year) = daysToDate(totalDays)
    return "${day.toString().padStart(2, '0')}.${month.toString().padStart(2, '0')}.$year"
}

private fun daysToDate(totalDays: Int): Triple<Int, Int, Int> {
    var remaining = totalDays
    var year = 1970
    while (true) {
        val daysInYear = if (isLeapYear(year)) 366 else 365
        if (remaining < daysInYear) break
        remaining -= daysInYear
        year++
    }
    val monthDays = if (isLeapYear(year)) LEAP_MONTH_DAYS else MONTH_DAYS
    var month = 1
    for (days in monthDays) {
        if (remaining < days) break
        remaining -= days
        month++
    }
    return Triple(remaining + 1, month, year)
}

private fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

private val MONTH_DAYS = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
private val LEAP_MONTH_DAYS = intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

// ──────────────────────────────────────────────────────
// File size formatting
// ──────────────────────────────────────────────────────

/**
 * Formats a byte count into a human-readable string (Б, КБ, МБ).
 *
 * Works with both `Long` and `Int` inputs.
 */
fun formatSizeBytes(bytes: Long): String {
    if (bytes < 1024L) return "$bytes Б"
    val kb = bytes.toDouble() / 1024.0
    if (kb < 1024.0) return "${roundOneDecimal(kb)} КБ"
    val mb = kb / 1024.0
    return "${roundOneDecimal(mb)} МБ"
}

private fun roundOneDecimal(value: Double): String {
    val rounded = (value * 10).roundToLong()
    val intPart = rounded / 10
    val decPart = rounded % 10
    return "$intPart.$decPart"
}
