package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.model.TimetableCourse

/**
 * Repository for fetching timetable/schedule data from the LMS API.
 */
interface CalendarRepository {
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
