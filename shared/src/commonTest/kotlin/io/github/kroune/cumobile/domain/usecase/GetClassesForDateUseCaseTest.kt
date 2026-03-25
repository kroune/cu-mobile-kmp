package io.github.kroune.cumobile.domain.usecase

import io.github.kroune.cumobile.data.model.CalendarEvent
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetClassesForDateUseCaseTest {
    private val useCase = GetClassesForDateUseCase()

    private fun dateMillis(year: Int, month: Int, day: Int): Long {
        val dt = LocalDateTime(year, month, day, 0, 0, 0)
        return dt.toInstant(TimeZone.UTC).toEpochMilliseconds()
    }

    // --- Filtering ---

    @Test
    fun returnsClassesForMatchingDate() {
        val events = listOf(
            makeEvent("20260301T090000Z", "20260301T103000Z", "Алгебра"),
            makeEvent("20260301T110000Z", "20260301T123000Z", "Физика"),
        )
        val result = useCase.execute(events, dateMillis(2026, 3, 1))
        assertEquals(2, result.size)
    }

    @Test
    fun filtersOutEventsOnDifferentDate() {
        val events = listOf(
            makeEvent("20260301T090000Z", "20260301T103000Z", "Алгебра"),
            makeEvent("20260302T110000Z", "20260302T123000Z", "Физика"),
        )
        val result = useCase.execute(events, dateMillis(2026, 3, 1))
        assertEquals(1, result.size)
        assertEquals("Алгебра", result[0].title)
    }

    @Test
    fun returnsEmptyForNoMatchingDate() {
        val events = listOf(
            makeEvent("20260301T090000Z", "20260301T103000Z", "Алгебра"),
        )
        val result = useCase.execute(events, dateMillis(2026, 3, 5))
        assertTrue(result.isEmpty())
    }

    @Test
    fun recurringEventShowsOnSubsequentWeek() {
        // Sept 1, 2026 is Tuesday
        val events = listOf(
            makeEvent(
                dtStart = "20260901T090000Z",
                dtEnd = "20260901T103000Z",
                summary = "Программирование",
                rRule = "FREQ=WEEKLY",
            ),
        )
        // Sept 8 is next Tuesday
        val result = useCase.execute(events, dateMillis(2026, 9, 8))
        assertEquals(1, result.size)
        assertEquals("Программирование", result[0].title)
    }

    @Test
    fun recurringEventWithExDateIsExcluded() {
        val events = listOf(
            makeEvent(
                dtStart = "20260901T090000Z",
                dtEnd = "20260901T103000Z",
                summary = "Семинар",
                rRule = "FREQ=WEEKLY",
                exDates = listOf("20260908T090000Z"),
            ),
        )
        // Sept 8 is excluded
        val result = useCase.execute(events, dateMillis(2026, 9, 8))
        assertTrue(result.isEmpty())
    }

    // --- Sorting ---

    @Test
    fun resultsSortedByStartTime() {
        val events = listOf(
            makeEvent("20260301T140000Z", "20260301T153000Z", "Поздний"),
            makeEvent("20260301T090000Z", "20260301T103000Z", "Ранний"),
        )
        val result = useCase.execute(events, dateMillis(2026, 3, 1))
        assertEquals("Ранний", result[0].title)
        assertEquals("Поздний", result[1].title)
    }

    // --- Room extraction ---

    @Test
    fun extractsRoomFromSummary() {
        assertEquals("301", GetClassesForDateUseCase.extractRoom("Алгебра 301", null))
    }

    @Test
    fun extractsRoomFromLocation() {
        assertEquals("205", GetClassesForDateUseCase.extractRoom("Алгебра", "корпус А 205"))
    }

    @Test
    fun extractsRoomWithCyrillicSuffix() {
        assertEquals("301а", GetClassesForDateUseCase.extractRoom("Алгебра 301а", null))
    }

    @Test
    fun fallsBackToLocationWhenNoRoomMatch() {
        assertEquals("Online", GetClassesForDateUseCase.extractRoom("Алгебра", "Online"))
    }

    @Test
    fun returnsEmptyWhenNoRoomAndNoLocation() {
        assertEquals("", GetClassesForDateUseCase.extractRoom("Алгебра", null))
    }

    // --- Type detection ---

    @Test
    fun detectsLecture() {
        assertEquals("Лекция", GetClassesForDateUseCase.detectType("Лекция по алгебре"))
    }

    @Test
    fun detectsLectureCaseInsensitive() {
        assertEquals("Лекция", GetClassesForDateUseCase.detectType("ЛЕКЦИЯ ПО АЛГЕБРЕ"))
    }

    @Test
    fun detectsSeminar() {
        assertEquals("Практика", GetClassesForDateUseCase.detectType("Семинар по физике"))
    }

    // --- Time formatting ---

    @Test
    fun formatsTimeSingleDigitHour() {
        assertEquals("09:05", GetClassesForDateUseCase.formatTime(9, 5))
    }

    @Test
    fun formatsTimeDoubleDigitHour() {
        assertEquals("14:30", GetClassesForDateUseCase.formatTime(14, 30))
    }

    @Test
    fun formatsMidnight() {
        assertEquals("00:00", GetClassesForDateUseCase.formatTime(0, 0))
    }

    // --- Mapping ---

    @Test
    fun mapsEventToClassData() {
        val event = CalendarEvent(
            uid = "map-1",
            summary = "Лекция по анализу 305",
            dtStart = "20260301T090000Z",
            dtEnd = "20260301T103000Z",
            location = "корпус А",
            url = "https://meet.example.com/123",
        )
        val classData = useCase.mapToClassData(event)
        assertEquals("Лекция по анализу 305", classData.title)
        assertEquals("305", classData.room)
        assertEquals("Лекция", classData.type)
        assertEquals("https://meet.example.com/123", classData.link)
    }

    private fun makeEvent(
        dtStart: String,
        dtEnd: String,
        summary: String,
        rRule: String? = null,
        exDates: List<String> = emptyList(),
    ): CalendarEvent =
        CalendarEvent(
            uid = "test-${summary.hashCode()}",
            summary = summary,
            dtStart = dtStart,
            dtEnd = dtEnd,
            rRule = rRule,
            exDates = exDates,
        )
}
