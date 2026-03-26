package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.CalendarEvent
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.model.TimetableCourse
import kotlinx.coroutines.flow.Flow

/**
 * Repository for fetching timetable/schedule data.
 *
 * Primary source: LMS timetable API (works for all authenticated students).
 * Secondary source: iCal feed URL (user-configured, optional).
 */
interface CalendarRepository {
    /** Flow emitting the current configured iCal URL. */
    val calendarUrlFlow: Flow<String?>

    /** Saves the iCal feed URL to local storage. */
    suspend fun saveCalendarUrl(url: String?)

    /**
     * Fetches events from the configured iCal URL.
     * Returns an empty list if no URL is configured or on error.
     */
    suspend fun fetchCalendar(): List<CalendarEvent>

    /**
     * Fetches the student's timetable from the LMS API.
     * Returns `null` on failure (not authenticated, network error, etc.).
     */
    suspend fun fetchTimetable(): List<TimetableCourse>?

    /**
     * Fetches classes for a specific date using the LMS timetable API.
     */
    suspend fun getClassesForDate(dateMillis: Long): List<ClassData>
}
