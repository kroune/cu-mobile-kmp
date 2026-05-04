package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Top-level response from `GET /students/me/timetables`.
 * Each entry represents a course with its scheduled event rows.
 */
@Serializable
data class TimetableCourseApi(
    val courseId: Long,
    val courseName: String,
    val eventRows: List<TimetableEventRowApi>,
)

@Serializable
data class TimetableEventRowApi(
    val eventType: String,
    val eventRowNumber: Int,
    val calendarEvent: TimetableCalendarEventApi? = null,
)

@Serializable
data class TimetableCalendarEventApi(
    val calendarEventId: String,
    val eventType: String,
    val location: String? = null,
    val host: TimetableHostApi? = null,
    val schedule: TimetableScheduleApi? = null,
)

@Serializable
data class TimetableHostApi(
    val name: String? = null,
    val email: String? = null,
)

@Serializable
data class TimetableScheduleApi(
    val startDate: String,
    val endDate: String,
    val startTime: String,
    val endTime: String,
    val dayOfWeek: String,
    val interval: Int = 1,
    val comment: String? = null,
)
