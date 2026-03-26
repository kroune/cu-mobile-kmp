package io.github.kroune.cumobile.domain.usecase

import io.github.kroune.cumobile.data.model.TimetableCalendarEvent
import io.github.kroune.cumobile.data.model.TimetableCourse
import io.github.kroune.cumobile.data.model.TimetableEventRow
import io.github.kroune.cumobile.data.model.TimetableHost
import io.github.kroune.cumobile.data.model.TimetableSchedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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

    // --- Timetable filtering ---

    @Test
    fun returnsClassesForMatchingDay() {
        val courses = listOf(makeTimetableCourse("Алгебра", "monday", "09:00", "10:30"))
        // March 30 2026 is a Monday
        val result = useCase.executeFromTimetable(courses, dateMillis(2026, 3, 30))
        assertEquals(1, result.size)
        assertEquals("Алгебра", result[0].title)
        assertEquals("09:00", result[0].startTime)
    }

    @Test
    fun filtersOutWrongDay() {
        val courses = listOf(makeTimetableCourse("Алгебра", "monday", "09:00", "10:30"))
        // March 31 2026 is a Tuesday
        val result = useCase.executeFromTimetable(courses, dateMillis(2026, 3, 31))
        assertTrue(result.isEmpty())
    }

    @Test
    fun filtersOutOutOfDateRange() {
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
    fun intervalTwoSkipsAlternateWeeks() {
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
        assertEquals(1, useCase.executeFromTimetable(courses, dateMillis(2026, 2, 6)).size)
        // Feb 13 = week 1, should NOT match (interval 2)
        assertTrue(useCase.executeFromTimetable(courses, dateMillis(2026, 2, 13)).isEmpty())
        // Feb 20 = week 2, should match
        assertEquals(1, useCase.executeFromTimetable(courses, dateMillis(2026, 2, 20)).size)
    }

    @Test
    fun multipleCoursesOnSameDay() {
        val courses = listOf(
            makeTimetableCourse("Алгебра", "monday", "09:00", "10:30"),
            makeTimetableCourse("Физика", "monday", "11:00", "12:30"),
        )
        val result = useCase.executeFromTimetable(courses, dateMillis(2026, 3, 30))
        assertEquals(2, result.size)
    }

    @Test
    fun resultsSortedByStartTime() {
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
        val result = useCase.executeFromTimetable(courses, dateMillis(2026, 3, 30))
        assertEquals(2, result.size)
        assertEquals("09:00", result[0].startTime)
        assertEquals("14:00", result[1].startTime)
    }

    @Test
    fun returnsEmptyForEmptyCourses() {
        val result = useCase.executeFromTimetable(emptyList(), dateMillis(2026, 3, 30))
        assertTrue(result.isEmpty())
    }

    @Test
    fun skipsRowWithNullCalendarEvent() {
        val courses = listOf(
            TimetableCourse(
                courseId = 1,
                courseName = "Test",
                eventRows = listOf(
                    TimetableEventRow(
                        eventType = "lecture",
                        eventRowNumber = 1,
                        calendarEvent = null,
                    ),
                ),
            ),
        )
        val result = useCase.executeFromTimetable(courses, dateMillis(2026, 3, 30))
        assertTrue(result.isEmpty())
    }

    // --- Mapping ---

    @Test
    fun mapsLectureEvent() {
        val row = makeTimetableRow("lecture", "11:30", "12:50", "monday")
        val classData = GetClassesForDateUseCase.mapTimetableToClassData(row, "Математика")
        assertEquals("Математика", classData.title)
        assertEquals("B101", classData.room)
        assertEquals("Лекция", classData.type)
        assertEquals("11:30", classData.startTime)
        assertEquals("12:50", classData.endTime)
    }

    @Test
    fun mapsSeminarEvent() {
        val row = makeTimetableRow("seminar", "13:00", "14:20", "tuesday")
        val classData = GetClassesForDateUseCase.mapTimetableToClassData(row, "Экономика")
        assertEquals("Семинар", classData.type)
    }

    @Test
    fun mapsEventWithHost() {
        val row = TimetableEventRow(
            eventType = "lecture",
            eventRowNumber = 1,
            calendarEvent = TimetableCalendarEvent(
                calendarEventId = "test",
                eventType = "lecture",
                location = "B702",
                host = TimetableHost(name = "Иванов Иван ", email = "i@cu.ru"),
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
        assertEquals("Иванов Иван", classData.professor)
    }

    @Test
    fun mapsEventWithNullScheduleGracefully() {
        val row = TimetableEventRow(
            eventType = "lecture",
            eventRowNumber = 1,
            calendarEvent = TimetableCalendarEvent(
                calendarEventId = "test",
                eventType = "lecture",
                schedule = null,
            ),
        )
        val classData = GetClassesForDateUseCase.mapTimetableToClassData(row, "Test")
        assertEquals("", classData.startTime)
        assertEquals("", classData.endTime)
        assertEquals("", classData.room)
        assertNull(classData.professor)
    }

    // --- parseDayOfWeek ---

    @Test
    fun parseDayOfWeekAllDays() {
        assertEquals(DayOfWeek.MONDAY, GetClassesForDateUseCase.parseDayOfWeek("monday"))
        assertEquals(DayOfWeek.TUESDAY, GetClassesForDateUseCase.parseDayOfWeek("tuesday"))
        assertEquals(DayOfWeek.WEDNESDAY, GetClassesForDateUseCase.parseDayOfWeek("wednesday"))
        assertEquals(DayOfWeek.THURSDAY, GetClassesForDateUseCase.parseDayOfWeek("thursday"))
        assertEquals(DayOfWeek.FRIDAY, GetClassesForDateUseCase.parseDayOfWeek("friday"))
        assertEquals(DayOfWeek.SATURDAY, GetClassesForDateUseCase.parseDayOfWeek("saturday"))
        assertEquals(DayOfWeek.SUNDAY, GetClassesForDateUseCase.parseDayOfWeek("sunday"))
    }

    @Test
    fun parseDayOfWeekCaseInsensitive() {
        assertEquals(DayOfWeek.MONDAY, GetClassesForDateUseCase.parseDayOfWeek("Monday"))
        assertEquals(DayOfWeek.FRIDAY, GetClassesForDateUseCase.parseDayOfWeek("FRIDAY"))
    }

    @Test
    fun parseDayOfWeekInvalidReturnsNull() {
        assertNull(GetClassesForDateUseCase.parseDayOfWeek("invalid"))
        assertNull(GetClassesForDateUseCase.parseDayOfWeek(""))
    }

    // --- Helpers ---

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
}
