package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.domain.model.ClassDataDomain
import io.github.kroune.cumobile.domain.model.TimetableCourseDomain

/**
 * Repository for fetching timetable/schedule data from the LMS API.
 */
interface CalendarRepository {
    /**
     * Fetches the student's timetable from the LMS API.
     * Returns `null` on failure (not authenticated, network error, etc.).
     */
    suspend fun fetchTimetable(): List<TimetableCourseDomain>?

    /**
     * Fetches classes for a specific date using the LMS timetable API.
     */
    suspend fun getClassesForDate(dateMillis: Long): List<ClassDataDomain>
}
