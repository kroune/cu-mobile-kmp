package io.github.kroune.cumobile.domain.usecase

import io.github.kroune.cumobile.data.model.CalendarEvent
import io.github.kroune.cumobile.data.model.TimetableCalendarEvent
import io.github.kroune.cumobile.data.model.TimetableCourse
import io.github.kroune.cumobile.data.model.TimetableEventRow
import io.github.kroune.cumobile.data.model.TimetableHost
import io.github.kroune.cumobile.data.model.TimetableSchedule
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetClassesForDateUseCaseTest {
    private val useCase = GetClassesForDateUseCase()

    private fun dateMillis(
        year: Int,
        month: Int,
        day: Int,
    ): Long {
        val dt = LocalDateTime(year, month, day, 0, 0, 0)
        return dt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
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

    @Test
    fun mapsEventWithBadDtEndFallsBackToStartTime() {
        val event = CalendarEvent(
            uid = "bad-end",
            summary = "Broken event",
            dtStart = "20260301T090000Z",
            dtEnd = "invalid",
        )
        val classData = useCase.mapToClassData(event)
        assertEquals("Broken event", classData.title)
        assertEquals(classData.startTime, classData.endTime)
    }

    @Test
    fun mapsEventWithEmptyDtEndFallsBackToStartTime() {
        val event = CalendarEvent(
            uid = "empty-end",
            summary = "No end time",
            dtStart = "20260301T140000Z",
            dtEnd = "",
        )
        val classData = useCase.mapToClassData(event)
        assertEquals(classData.startTime, classData.endTime)
    }

    // --- Timetable API tests ---

    @Test
    fun timetableReturnsClassesForMatchingDay() {
        val courses = listOf(makeTimetableCourse("Алгебра", "monday", "09:00", "10:30"))
        // March 30 2026 is a Monday
        val result = useCase.executeFromTimetable(courses, dateMillis(2026, 3, 30))
        assertEquals(1, result.size)
        assertEquals("Алгебра", result[0].title)
        assertEquals("09:00", result[0].startTime)
    }

    @Test
    fun timetableFiltersOutWrongDay() {
        val courses = listOf(makeTimetableCourse("Алгебра", "monday", "09:00", "10:30"))
        // March 31 2026 is a Tuesday
        val result = useCase.executeFromTimetable(courses, dateMillis(2026, 3, 31))
        assertTrue(result.isEmpty())
    }

    @Test
    fun timetableFiltersOutOutOfRange() {
        val courses = listOf(
            makeTimetableCourse(
                name = "Алгебра",
                dayOfWeek = "monday",
                startTime = "09:00",
                endTime = "10:30",
                startDate = "2026-02-02",
                endDate = "2026-03-22",
            ),
        )
        // March 30 is a Monday but after endDate
        val result = useCase.executeFromTimetable(courses, dateMillis(2026, 3, 30))
        assertTrue(result.isEmpty())
    }

    @Test
    fun timetableIntervalTwoSkipsAlternateWeeks() {
        val courses = listOf(
            makeTimetableCourse(
                name = "Семинар",
                dayOfWeek = "friday",
                startTime = "13:00",
                endTime = "15:50",
                startDate = "2026-02-02",
                endDate = "2026-05-31",
                interval = 2,
            ),
        )
        // Feb 6 2026 is first Friday = week 0, should match
        val result1 = useCase.executeFromTimetable(courses, dateMillis(2026, 2, 6))
        assertEquals(1, result1.size)
        // Feb 13 = week 1, should NOT match (interval 2)
        val result2 = useCase.executeFromTimetable(courses, dateMillis(2026, 2, 13))
        assertTrue(result2.isEmpty())
        // Feb 20 = week 2, should match
        val result3 = useCase.executeFromTimetable(courses, dateMillis(2026, 2, 20))
        assertEquals(1, result3.size)
    }

    @Test
    fun timetableMapsEventType() {
        val row = TimetableEventRow(
            eventType = "lecture",
            eventRowNumber = 1,
            calendarEvent = TimetableCalendarEvent(
                calendarEventId = "test",
                eventType = "lecture",
                location = "B702",
                host = TimetableHost(name = "Иванов Иван", email = "i@cu.ru"),
                schedule = TimetableSchedule(
                    startDate = "2026-02-02",
                    endDate = "2026-05-31",
                    startTime = "11:30",
                    endTime = "12:50",
                    dayOfWeek = "monday",
                ),
            ),
        )
        val classData = GetClassesForDateUseCase.mapTimetableToClassData(row, "Математика")
        assertEquals("Математика", classData.title)
        assertEquals("B702", classData.room)
        assertEquals("Лекция", classData.type)
        assertEquals("Иванов Иван", classData.professor)
        assertEquals("11:30", classData.startTime)
        assertEquals("12:50", classData.endTime)
    }

    @Test
    fun parseDayOfWeekValid() {
        assertEquals(
            kotlinx.datetime.DayOfWeek.THURSDAY,
            GetClassesForDateUseCase.parseDayOfWeek("thursday"),
        )
    }

    @Test
    fun parseDayOfWeekInvalid() {
        assertEquals(null, GetClassesForDateUseCase.parseDayOfWeek("invalid"))
    }

    @Test
    fun timetableSortsByStartTime() {
        val courses = listOf(
            TimetableCourse(
                courseId = 1,
                courseName = "Courses",
                eventRows = listOf(
                    makeTimetableRow("lecture", "14:00", "15:30", "monday"),
                    makeTimetableRow("seminar", "09:00", "10:30", "monday"),
                ),
            ),
        )
        // March 30 2026 is Monday
        val result = useCase.executeFromTimetable(courses, dateMillis(2026, 3, 30))
        assertEquals(2, result.size)
        assertEquals("09:00", result[0].startTime)
        assertEquals("14:00", result[1].startTime)
    }

    private fun makeTimetableCourse(
        name: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        startDate: String = "2026-02-02",
        endDate: String = "2026-05-31",
        interval: Int = 1,
    ): TimetableCourse =
        TimetableCourse(
            courseId = name.hashCode().toLong(),
            courseName = name,
            eventRows = listOf(
                makeTimetableRow("seminar", startTime, endTime, dayOfWeek, startDate, endDate, interval),
            ),
        )

    private fun makeTimetableRow(
        eventType: String,
        startTime: String,
        endTime: String,
        dayOfWeek: String,
        startDate: String = "2026-02-02",
        endDate: String = "2026-05-31",
        interval: Int = 1,
    ): TimetableEventRow =
        TimetableEventRow(
            eventType = eventType,
            eventRowNumber = 1,
            calendarEvent = TimetableCalendarEvent(
                calendarEventId = "test-$startTime",
                eventType = eventType,
                location = "B101",
                schedule = TimetableSchedule(
                    startDate = startDate,
                    endDate = endDate,
                    startTime = startTime,
                    endTime = endTime,
                    dayOfWeek = dayOfWeek,
                    interval = interval,
                ),
            ),
        )

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
