package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.CalendarEvent
import io.github.kroune.cumobile.data.model.ClassData
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing the calendar feed URL and fetching events.
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
     * Fetches and parses classes for a specific date (represented by epoch millis).
     */
    suspend fun getClassesForDate(dateMillis: Long): List<ClassData>
}
