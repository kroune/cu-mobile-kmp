package io.github.kroune.cumobile.presentation.common

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

private const val BytesPerKb = 1024L
private const val DecimalBase = 10

// ──────────────────────────────────────────────────────
// Format definitions (kotlinx-datetime Format DSL)
// ──────────────────────────────────────────────────────

/** `dd.MM HH:mm` */
private val dayMonthTimeFormat = LocalDateTime.Format {
    day()
    char('.')
    monthNumber()
    char(' ')
    hour()
    char(':')
    minute()
}

/** `dd.MM.yyyy HH:mm` */
private val dayMonthYearTimeFormat = LocalDateTime.Format {
    day()
    char('.')
    monthNumber()
    char('.')
    year()
    char(' ')
    hour()
    char(':')
    minute()
}

/** `dd.MM` */
private val dayMonthFormat = LocalDateTime.Format {
    day()
    char('.')
    monthNumber()
}

/** `dd.MM.yyyy` */
private val dayMonthYearDateFormat = LocalDate.Format {
    day()
    char('.')
    monthNumber()
    char('.')
    year()
}

// ──────────────────────────────────────────────────────
// ISO 8601 date/time formatting
// ──────────────────────────────────────────────────────

private fun parseIsoDateTime(iso: String): LocalDateTime =
    runCatching {
        LocalDateTime.parse(iso)
    }.getOrElse {
        DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
            .parse(iso)
            .toLocalDateTime()
    }

private inline fun formatIsoOrFallback(
    iso: String,
    fallback: String = iso,
    format: (LocalDateTime) -> String,
): String =
    try {
        format(parseIsoDateTime(iso))
    } catch (e: Exception) {
        logger.error(e) { "Failed to format ISO datetime: $iso" }
        fallback
    }

/**
 * Formats an ISO 8601 deadline to `"dd.MM HH:mm"`.
 * Returns `"Без дедлайна"` when [isoDate] is null, raw string on parse errors.
 */
fun formatDeadline(isoDate: String?): String {
    if (isoDate == null) return "Без дедлайна"
    return formatIsoOrFallback(isoDate) { dayMonthTimeFormat.format(it) }
}

/** Formats an ISO 8601 datetime to `"dd.MM HH:mm"`. Falls back to the raw string. */
fun formatDateTime(dateTime: String): String =
    formatIsoOrFallback(dateTime) { dayMonthTimeFormat.format(it) }

/** Formats an ISO 8601 datetime to `"dd.MM.yyyy HH:mm"`. Falls back to the raw string. */
fun formatDateTimeFull(isoDate: String): String =
    formatIsoOrFallback(isoDate) { dayMonthYearTimeFormat.format(it) }

/** Formats an ISO 8601 deadline to `"dd.MM"`. Falls back to the raw string. */
fun formatDeadlineShort(deadline: String): String =
    formatIsoOrFallback(deadline) { dayMonthFormat.format(it) }

/**
 * Adds [daysToAdd] to an ISO 8601 deadline and returns formatted `"dd.MM HH:mm"`.
 * Returns `null` when [isoDate] is null or on parse errors.
 */
fun formatDeadlinePlusDays(
    isoDate: String?,
    daysToAdd: Int,
): String? {
    if (isoDate == null) return null
    return try {
        val dt = parseIsoDateTime(isoDate)
        val zone = TimeZone.currentSystemDefault()
        val shifted = (dt.toInstant(zone) + daysToAdd.days)
            .toLocalDateTime(zone)
        dayMonthTimeFormat.format(shifted)
    } catch (e: Exception) {
        logger.error(e) { "Failed to compute deadline + $daysToAdd days: $isoDate" }
        null
    }
}

// ──────────────────────────────────────────────────────
// Epoch millis → date
// ──────────────────────────────────────────────────────

/** Formats a millisecond timestamp to `"dd.MM.yyyy"`. */
fun formatEpochDate(millis: Long): String {
    if (millis < 0L) return ""
    return try {
        Instant
            .fromEpochMilliseconds(millis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .format(dayMonthYearDateFormat)
    } catch (e: Exception) {
        logger.error(e) { "Failed to format epoch date: $millis" }
        ""
    }
}

// ──────────────────────────────────────────────────────
// File size formatting
// ──────────────────────────────────────────────────────

/** Formats a byte count into a human-readable string (Б, КБ, МБ). */
fun formatSizeBytes(bytes: Long): String {
    if (bytes < BytesPerKb) return "$bytes Б"
    val kb = bytes.toDouble() / BytesPerKb
    if (kb < BytesPerKb) return "${roundOneDecimal(kb)} КБ"
    val mb = kb / BytesPerKb
    return "${roundOneDecimal(mb)} МБ"
}

private fun roundOneDecimal(value: Double): String {
    val rounded = (value * DecimalBase).roundToLong()
    val intPart = rounded / DecimalBase
    val decPart = rounded % DecimalBase
    return "$intPart.$decPart"
}
