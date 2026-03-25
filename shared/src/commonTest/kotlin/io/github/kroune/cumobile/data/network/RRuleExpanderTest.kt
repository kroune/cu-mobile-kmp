package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.CalendarEvent
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RRuleExpanderTest {

    // --- Non-recurring events ---

    @Test
    fun nonRecurringEventMatchesStartDate() {
        val event = makeEvent(
            dtStart = "20260901T090000Z",
            dtEnd = "20260901T103000Z",
        )
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 1)))
    }

    @Test
    fun nonRecurringEventDoesNotMatchOtherDate() {
        val event = makeEvent(
            dtStart = "20260901T090000Z",
            dtEnd = "20260901T103000Z",
        )
        assertFalse(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 2)))
    }

    // --- FREQ=WEEKLY ---

    @Test
    fun weeklyRecurrenceMatchesNextWeek() {
        // Sept 1, 2026 is a Tuesday
        val event = makeEvent(
            dtStart = "20260901T090000Z",
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=WEEKLY",
        )
        // Next Tuesday = Sept 8
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 8)))
    }

    @Test
    fun weeklyRecurrenceDoesNotMatchWrongDay() {
        val event = makeEvent(
            dtStart = "20260901T090000Z", // Tuesday
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=WEEKLY",
        )
        // Wednesday Sept 2
        assertFalse(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 2)))
    }

    @Test
    fun weeklyRecurrenceMatchesSeveralWeeksLater() {
        val event = makeEvent(
            dtStart = "20260901T090000Z", // Tuesday
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=WEEKLY",
        )
        // Sept 29 is also a Tuesday (4 weeks later)
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 29)))
    }

    // --- FREQ=WEEKLY with BYDAY ---

    @Test
    fun weeklyWithByDayMatchesSpecifiedDays() {
        // Sept 1, 2026 is Tuesday; BYDAY=MO,WE means Monday and Wednesday
        val event = makeEvent(
            dtStart = "20260901T090000Z",
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=WEEKLY;BYDAY=MO,WE",
        )
        // Sept 2 = Wednesday (same week)
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 2)))
        // Sept 7 = Monday (next week)
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 7)))
        // Sept 3 = Thursday
        assertFalse(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 3)))
    }

    // --- INTERVAL ---

    @Test
    fun weeklyWithIntervalTwo() {
        val event = makeEvent(
            dtStart = "20260901T090000Z", // Tuesday
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=WEEKLY;INTERVAL=2",
        )
        // Week 1 (Sept 1) - matches
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 1)))
        // Week 2 (Sept 8) - skipped
        assertFalse(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 8)))
        // Week 3 (Sept 15) - matches
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 15)))
    }

    // --- UNTIL ---

    @Test
    fun weeklyWithUntilStopsAfterDate() {
        val event = makeEvent(
            dtStart = "20260901T090000Z", // Tuesday
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=WEEKLY;UNTIL=20260915T000000Z",
        )
        // Sept 8 - before UNTIL, matches
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 8)))
        // Sept 22 - after UNTIL, does not match
        assertFalse(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 22)))
    }

    // --- COUNT ---

    @Test
    fun weeklyWithCountLimitsOccurrences() {
        val event = makeEvent(
            dtStart = "20260901T090000Z", // Tuesday
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=WEEKLY;COUNT=3",
        )
        // Occurrence 1: Sept 1
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 1)))
        // Occurrence 2: Sept 8
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 8)))
        // Occurrence 3: Sept 15
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 15)))
        // Occurrence 4: Sept 22 - exceeds COUNT
        assertFalse(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 22)))
    }

    // --- EXDATE ---

    @Test
    fun exDateExcludesSpecificDate() {
        val event = makeEvent(
            dtStart = "20260901T090000Z", // Tuesday
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=WEEKLY",
            exDates = listOf("20260908T090000Z"),
        )
        // Sept 1 - not excluded
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 1)))
        // Sept 8 - excluded
        assertFalse(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 8)))
        // Sept 15 - not excluded
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 15)))
    }

    // --- FREQ=DAILY ---

    @Test
    fun dailyRecurrenceMatchesNextDay() {
        val event = makeEvent(
            dtStart = "20260901T090000Z",
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=DAILY",
        )
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 2)))
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 10)))
    }

    @Test
    fun dailyWithIntervalThree() {
        val event = makeEvent(
            dtStart = "20260901T090000Z",
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=DAILY;INTERVAL=3",
        )
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 1)))
        assertFalse(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 2)))
        assertFalse(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 3)))
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 4)))
    }

    @Test
    fun dailyWithCountLimitsOccurrences() {
        val event = makeEvent(
            dtStart = "20260901T090000Z",
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=DAILY;COUNT=3",
        )
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 1)))
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 2)))
        assertTrue(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 3)))
        assertFalse(RRuleExpander.eventOccursOn(event, LocalDate(2026, 9, 4)))
    }

    // --- Date before start ---

    @Test
    fun dateBeforeStartNeverMatches() {
        val event = makeEvent(
            dtStart = "20260901T090000Z",
            dtEnd = "20260901T103000Z",
            rRule = "FREQ=WEEKLY",
        )
        assertFalse(RRuleExpander.eventOccursOn(event, LocalDate(2026, 8, 25)))
    }

    // --- parseRRule ---

    @Test
    fun parseRRuleBasic() {
        val parsed = RRuleExpander.parseRRule("FREQ=WEEKLY;BYDAY=MO,WE,FR;INTERVAL=2;COUNT=10")
        assertEquals(RRuleExpander.Frequency.WEEKLY, parsed.freq)
        assertEquals(2, parsed.interval)
        assertEquals(10, parsed.count)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), parsed.byDay)
    }

    @Test
    fun parseRRuleWithUntil() {
        val parsed = RRuleExpander.parseRRule("FREQ=DAILY;UNTIL=20261231T000000Z")
        assertEquals(RRuleExpander.Frequency.DAILY, parsed.freq)
        assertEquals(1, parsed.interval)
    }

    // --- parseExDates ---

    @Test
    fun parseExDatesMultiple() {
        val exDates = RRuleExpander.parseExDates(
            listOf("20260908T090000Z", "20260922T090000Z"),
        )
        assertEquals(2, exDates.size)
    }

    @Test
    fun parseExDatesEmpty() {
        val exDates = RRuleExpander.parseExDates(emptyList())
        assertTrue(exDates.isEmpty())
    }

    private fun makeEvent(
        dtStart: String,
        dtEnd: String,
        rRule: String? = null,
        exDates: List<String> = emptyList(),
    ): CalendarEvent =
        CalendarEvent(
            uid = "test-uid",
            summary = "Test Event",
            dtStart = dtStart,
            dtEnd = dtEnd,
            rRule = rRule,
            exDates = exDates,
        )
}
