package io.github.kroune.cumobile.domain.model

data class TimetableCourseDomain(
    val courseId: String,
    val courseName: String,
    val eventRows: List<TimetableEventRowDomain>,
)

data class TimetableEventRowDomain(
    val eventType: String,
    val eventRowNumber: Int,
    val calendarEvent: TimetableCalendarEventDomain?,
)

data class TimetableCalendarEventDomain(
    val calendarEventId: String,
    val eventType: String,
    val location: String?,
    val hostName: String?,
    val hostEmail: String?,
    val startDate: String,
    val endDate: String,
    val startTime: String,
    val endTime: String,
    val dayOfWeek: String,
    val interval: Int,
    val comment: String?,
)
