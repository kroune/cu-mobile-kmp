package io.github.kroune.cumobile.data.model

/**
 * Display-ready representation of a class event.
 *
 * Extracted from [CalendarEvent] summary, location, and description.
 */
data class ClassData(
    val startTime: String, // "13:00"
    val endTime: String, // "14:30"
    val room: String,
    val type: String,
    val title: String,
    val professor: String? = null,
    val link: String? = null,
    val badge: String? = null,
    val badgeColor: Long? = null,
)
