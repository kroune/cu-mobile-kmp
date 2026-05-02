package io.github.kroune.cumobile.data.model.mappers

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.toInstant
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

private const val EndOfDayHour = 23
private const val EndOfDayMinute = 59
private const val EndOfDaySecond = 59

internal fun parseInstant(value: String?): Instant? =
    value?.let { runCatching { Instant.parse(it) }.getOrNull() }

internal fun parseDeadlineInstant(isoDate: String?): Instant? {
    if (isoDate == null) return null
    return try {
        parseIsoDateTime(isoDate).toInstant(TimeZone.currentSystemDefault())
    } catch (e: Exception) {
        logger.error(e) { "Failed to parse deadline: $isoDate" }
        null
    }
}

private fun parseIsoDateTime(iso: String): LocalDateTime {
    if (!iso.contains('T')) {
        return LocalDate.parse(iso).atTime(EndOfDayHour, EndOfDayMinute, EndOfDaySecond)
    }
    return runCatching { LocalDateTime.parse(iso) }.getOrElse {
        DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
            .parse(iso)
            .toLocalDateTime()
    }
}
