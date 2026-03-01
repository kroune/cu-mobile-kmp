@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.common

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
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
        val dt = parseIsoDateTime(isoDate)
        val day = dt.dayOfMonth.toString().padStart(2, '0')
        val month = dt.monthNumber.toString().padStart(2, '0')
        val hour = dt.hour.toString().padStart(2, '0')
        val minute = dt.minute.toString().padStart(2, '0')
        "$day.$month $hour:$minute"
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
        val dt = parseIsoDateTime(dateTime)
        val day = dt.dayOfMonth.toString().padStart(2, '0')
        val month = dt.monthNumber.toString().padStart(2, '0')
        val hour = dt.hour.toString().padStart(2, '0')
        val minute = dt.minute.toString().padStart(2, '0')
        "$day.$month $hour:$minute"
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
    return try {
        val dt = parseIsoDateTime(isoDate)
        val day = dt.dayOfMonth.toString().padStart(2, '0')
        val month = dt.monthNumber.toString().padStart(2, '0')
        val hour = dt.hour.toString().padStart(2, '0')
        val minute = dt.minute.toString().padStart(2, '0')
        "$day.$month.${dt.year} $hour:$minute"
    } catch (e: Exception) {
        logger.error(e) { "Failed to format full datetime: $isoDate" }
        isoDate
    }
}

/**
 * Formats an ISO 8601 deadline string to a short day-month display.
 *
 * Example: `"2025-06-01T23:59:00Z"` → `"01.06"`
 *
 * Falls back to the raw string on parse errors.
 */
fun formatDeadlineShort(deadline: String): String {
    return try {
        val dt = parseIsoDateTime(deadline)
        val day = dt.dayOfMonth.toString().padStart(2, '0')
        val month = dt.monthNumber.toString().padStart(2, '0')
        "$day.$month"
    } catch (e: Exception) {
        logger.error(e) { "Failed to format short deadline: $deadline" }
        deadline
    }
}

private fun parseIsoDateTime(iso: String): LocalDateTime {
    // kotlinx-datetime parser is strict, handle common variants
    val normalized = if (iso.endsWith("Z")) {
        iso.removeSuffix("Z")
    } else {
        iso
    }
    // If it's just a date, append T00:00
    return if (!normalized.contains("T")) {
        LocalDateTime.parse("${normalized}T00:00:00")
    } else {
        LocalDateTime.parse(normalized)
    }
}

// ──────────────────────────────────────────────────────
// Epoch millis → date
// ──────────────────────────────────────────────────────

/**
 * Formats a millisecond timestamp to `"dd.MM.yyyy"`.
 *
 * Uses kotlinx-datetime for reliable cross-platform formatting.
 */
fun formatEpochDate(millis: Long): String {
    if (millis < 0L) return ""
    return try {
        val instant = Instant.fromEpochMilliseconds(millis)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val day = localDate.dayOfMonth.toString().padStart(2, '0')
        val month = localDate.monthNumber.toString().padStart(2, '0')
        "$day.$month.${localDate.year}"
    } catch (e: Exception) {
        logger.error(e) { "Failed to format epoch date: $millis" }
        ""
    }
}

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
