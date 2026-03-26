package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Top-level response from `GET /students/me/timetables`.
 * Each entry represents a course with its scheduled event rows.
 */
@Serializable
data class TimetableCourse(
    val courseId: Long,
    val courseName: String,
    val eventRows: List<TimetableEventRow>,
)

@Serializable
data class TimetableEventRow(
    val eventType: String,
    val eventRowNumber: Int,
    val calendarEvent: TimetableCalendarEvent? = null,
)

@Serializable
data class TimetableCalendarEvent(
    val calendarEventId: String,
    val eventType: String,
    val location: String? = null,
    val host: TimetableHost? = null,
    val schedule: TimetableSchedule? = null,
)

@Serializable
data class TimetableHost(
    val name: String? = null,
    val email: String? = null,
)

@Serializable
data class TimetableSchedule(
    val startDate: String,
    val endDate: String,
    val startTime: String,
    val endTime: String,
    val dayOfWeek: String,
    val interval: Int = 1,
    val comment: String? = null,
)
