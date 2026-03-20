package io.github.kroune.cumobile.presentation.common

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatUtilsTest {
    @Test
    fun testFormatDeadline() {
        assertEquals("15.02 14:00", formatDeadline("2026-02-15T14:00:00"))
        assertEquals("Без дедлайна", formatDeadline(null))
        assertEquals("invalid", formatDeadline("invalid"))
    }

    @Test
    fun testFormatDeadlineWithTimezoneOffset() {
        assertEquals("16.03 12:20", formatDeadline("2026-03-16T12:20:00+00:00"))
        assertEquals("01.04 23:59", formatDeadline("2026-04-01T23:59:00+03:00"))
        assertEquals("15.02 14:00", formatDeadline("2026-02-15T14:00:00-05:00"))
    }

    @Test
    fun testFormatDateTime() {
        assertEquals("15.02 14:00", formatDateTime("2026-02-15T14:00:00.000Z"))
    }

    @Test
    fun testFormatDateTimeWithTimezoneOffset() {
        assertEquals("16.03 12:20", formatDateTime("2026-03-16T12:20:00+00:00"))
    }

    @Test
    fun testFormatDateTimeFull() {
        assertEquals("22.02.2026 14:30", formatDateTimeFull("2026-02-22T14:30:00Z"))
    }

    @Test
    fun testFormatDateTimeFullWithTimezoneOffset() {
        assertEquals("22.02.2026 14:30", formatDateTimeFull("2026-02-22T14:30:00+00:00"))
    }

    @Test
    fun testFormatDeadlineShortWithTimezoneOffset() {
        assertEquals("16.03", formatDeadlineShort("2026-03-16T12:20:00+00:00"))
    }

    @Test
    fun testFormatSizeBytes() {
        assertEquals("500 Б", formatSizeBytes(500))
        assertEquals("1.0 КБ", formatSizeBytes(1024))
        assertEquals("1.5 КБ", formatSizeBytes(1536))
        assertEquals("1.0 МБ", formatSizeBytes(1024 * 1024))
    }

    @Test
    fun testClock() {
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(1000)
        assertEquals(1000, instant.toEpochMilliseconds())
    }

    @Test
    fun testFormatEpochDate() {
        // 0 is 01.01.1970
        assertEquals("01.01.1970", formatEpochDate(0))
        // 1740268800000 is 23.02.2025
        assertEquals("23.02.2025", formatEpochDate(1740268800000L))
    }
}
