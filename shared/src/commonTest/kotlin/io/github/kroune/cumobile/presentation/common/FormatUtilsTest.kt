package io.github.kroune.cumobile.presentation.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormatUtilsTest {

    @Test
    fun testFormatDeadline() {
        assertEquals("15.02 14:00", formatDeadline("2026-02-15T14:00:00"))
        assertEquals("Без дедлайна", formatDeadline(null))
        assertEquals("invalid", formatDeadline("invalid"))
    }

    @Test
    fun testFormatDateTime() {
        assertEquals("15.02 14:00", formatDateTime("2026-02-15T14:00:00.000Z"))
    }

    @Test
    fun testFormatDateTimeFull() {
        assertEquals("22.02.2026 14:30", formatDateTimeFull("2026-02-22T14:30:00Z"))
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
