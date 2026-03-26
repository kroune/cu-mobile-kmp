package io.github.kroune.cumobile.data.network

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IcalDateParserTest {
    @Test
    fun parseFullDateTimeWithZ() {
        val result = IcalDateParser.parse("20260301T130000Z")
        val expected = LocalDateTime(2026, 3, 1, 13, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, result)
    }

    @Test
    fun parseFullDateTimeWithoutZ() {
        val result = IcalDateParser.parse("20260915T083000")
        val expected = LocalDateTime(2026, 9, 15, 8, 30, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, result)
    }

    @Test
    fun parseDateOnly() {
        val result = IcalDateParser.parse("20260301")
        val expected = LocalDateTime(2026, 3, 1, 0, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, result)
    }

    @Test
    fun parseDateWithTimezoneParam() {
        // iCal sometimes has DTSTART;TZID=Europe/Moscow:20260301T130000
        // After the parser extracts the value part, it looks like "20260301T130000"
        val result = IcalDateParser.parse("20260301T130000")
        val expected = LocalDateTime(2026, 3, 1, 13, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, result)
    }

    @Test
    fun parseWithSeconds() {
        val result = IcalDateParser.parse("20261231T235959Z")
        val expected = LocalDateTime(2026, 12, 31, 23, 59, 59).toInstant(TimeZone.UTC)
        assertEquals(expected, result)
    }

    @Test
    fun parseInvalidDateThrows() {
        assertFailsWith<IllegalArgumentException> {
            IcalDateParser.parse("2026")
        }
    }

    @Test
    fun parseMidnightDateTime() {
        val result = IcalDateParser.parse("20260101T000000Z")
        val expected = LocalDateTime(2026, 1, 1, 0, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, result)
    }
}
