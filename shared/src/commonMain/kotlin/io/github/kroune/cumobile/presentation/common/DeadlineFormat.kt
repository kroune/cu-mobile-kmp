package io.github.kroune.cumobile.presentation.common

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

private val hourMinuteFormat = LocalDateTime.Format {
    hour()
    char(':')
    minute()
}

private val dayShortMonthFormat = LocalDateTime.Format {
    day(Padding.NONE)
    char(' ')
    monthName(russianMonthsShort)
}

/**
 * Parses an ISO 8601 deadline into an [Instant] in the current timezone.
 * Returns `null` on error or for a null input; never throws.
 */
fun parseDeadlineInstant(isoDate: String?): Instant? {
    if (isoDate == null) return null
    return try {
        parseIsoDateTime(isoDate).toInstant(TimeZone.currentSystemDefault())
    } catch (e: Exception) {
        logger.error(e) { "Failed to parse deadline: $isoDate" }
        null
    }
}

/** `true` when the deadline is strictly in the past relative to [now]. */
fun isOverdue(
    isoDate: String?,
    now: Instant,
): Boolean {
    val instant = parseDeadlineInstant(isoDate) ?: return false
    return instant < now
}

/** Deadline time as `"HH:mm"`. Returns `"—"` when the deadline is null or unparseable. */
fun formatDeadlineTime(isoDate: String?): String {
    val local = parseDeadlineInstant(isoDate)
        ?.toLocalDateTime(TimeZone.currentSystemDefault())
        ?: return "—"
    return hourMinuteFormat.format(local)
}

/** Deadline date as `"d мес"` (short month). Returns `""` when unavailable. */
fun formatDeadlineDayShortMonth(isoDate: String?): String {
    val local = parseDeadlineInstant(isoDate)
        ?.toLocalDateTime(TimeZone.currentSystemDefault())
        ?: return ""
    return dayShortMonthFormat.format(local)
}
