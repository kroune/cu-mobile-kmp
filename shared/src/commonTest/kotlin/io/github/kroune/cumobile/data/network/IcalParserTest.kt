package io.github.kroune.cumobile.data.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IcalParserTest {
    private val parser = IcalParser()

    @Test
    fun parseSingleEvent() {
        val ics =
            """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:event-1
            SUMMARY:Математический анализ
            DTSTART:20260301T090000Z
            DTEND:20260301T103000Z
            LOCATION:А-301
            END:VEVENT
            END:VCALENDAR
            """.trimIndent()

        val events = parser.parse(ics)
        assertEquals(1, events.size)

        val event = events.first()
        assertEquals("event-1", event.uid)
        assertEquals("Математический анализ", event.summary)
        assertEquals("20260301T090000Z", event.dtStart)
        assertEquals("20260301T103000Z", event.dtEnd)
        assertEquals("А-301", event.location)
    }

    @Test
    fun parseMultipleEvents() {
        val ics =
            """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:event-1
            SUMMARY:Алгебра
            DTSTART:20260301T090000Z
            DTEND:20260301T103000Z
            END:VEVENT
            BEGIN:VEVENT
            UID:event-2
            SUMMARY:Физика
            DTSTART:20260301T110000Z
            DTEND:20260301T123000Z
            END:VEVENT
            END:VCALENDAR
            """.trimIndent()

        val events = parser.parse(ics)
        assertEquals(2, events.size)
        assertEquals("Алгебра", events[0].summary)
        assertEquals("Физика", events[1].summary)
    }

    @Test
    fun parseEventWithRRule() {
        val ics =
            """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:recurring-1
            SUMMARY:Лекция по программированию
            DTSTART:20260901T090000Z
            DTEND:20260901T103000Z
            RRULE:FREQ=WEEKLY;BYDAY=MO;UNTIL=20261231T000000Z
            END:VEVENT
            END:VCALENDAR
            """.trimIndent()

        val events = parser.parse(ics)
        assertEquals(1, events.size)
        assertEquals("FREQ=WEEKLY;BYDAY=MO;UNTIL=20261231T000000Z", events[0].rRule)
    }

    @Test
    fun parseEventWithExDates() {
        val ics =
            """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:exdate-1
            SUMMARY:Семинар
            DTSTART:20260901T090000Z
            DTEND:20260901T103000Z
            RRULE:FREQ=WEEKLY;BYDAY=TU
            EXDATE:20260908T090000Z
            EXDATE:20260922T090000Z
            END:VEVENT
            END:VCALENDAR
            """.trimIndent()

        val events = parser.parse(ics)
        assertEquals(1, events.size)
        assertEquals(2, events[0].exDates.size)
        assertEquals("20260908T090000Z", events[0].exDates[0])
        assertEquals("20260922T090000Z", events[0].exDates[1])
    }

    @Test
    fun parseLineUnfolding() {
        val ics = "BEGIN:VCALENDAR\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:fold-1\r\n" +
            "SUMMARY:Very Long\r\n" +
            "  Event Name\r\n" +
            "DTSTART:20260301T090000Z\r\n" +
            "DTEND:20260301T103000Z\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR"

        val events = parser.parse(ics)
        assertEquals(1, events.size)
        assertEquals("Very Long Event Name", events[0].summary)
    }

    @Test
    fun parseEmptyCalendar() {
        val ics =
            """
            BEGIN:VCALENDAR
            END:VCALENDAR
            """.trimIndent()

        val events = parser.parse(ics)
        assertTrue(events.isEmpty())
    }

    @Test
    fun parseEventWithOptionalFields() {
        val ics =
            """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:opt-1
            SUMMARY:Test
            DTSTART:20260301T090000Z
            DTEND:20260301T103000Z
            DESCRIPTION:Some description
            URL:https://meet.example.com/123
            END:VEVENT
            END:VCALENDAR
            """.trimIndent()

        val events = parser.parse(ics)
        assertEquals(1, events.size)
        assertEquals("Some description", events[0].description)
        assertEquals("https://meet.example.com/123", events[0].url)
    }

    @Test
    fun parseEventMissingOptionalFieldsReturnsNulls() {
        val ics =
            """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:min-1
            SUMMARY:Minimal
            DTSTART:20260301T090000Z
            DTEND:20260301T103000Z
            END:VEVENT
            END:VCALENDAR
            """.trimIndent()

        val events = parser.parse(ics)
        assertEquals(1, events.size)
        assertNull(events[0].description)
        assertNull(events[0].location)
        assertNull(events[0].url)
        assertNull(events[0].rRule)
        assertTrue(events[0].exDates.isEmpty())
    }

    @Test
    fun parseEventWithParameterizedProperty() {
        val ics =
            """
            BEGIN:VCALENDAR
            BEGIN:VEVENT
            UID:param-1
            SUMMARY:Test
            DTSTART;TZID=Europe/Moscow:20260301T090000
            DTEND;TZID=Europe/Moscow:20260301T103000
            END:VEVENT
            END:VCALENDAR
            """.trimIndent()

        val events = parser.parse(ics)
        assertEquals(1, events.size)
        assertEquals("20260301T090000", events[0].dtStart)
        assertEquals("20260301T103000", events[0].dtEnd)
    }
}
