package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a single event from an iCal (.ics) feed.
 *
 * Stores raw date/time strings in iCal format (e.g., "20260301T130000Z")
 * and RRULE string for recurring events.
 */
@Serializable
data class CalendarEvent(
    val uid: String,
    val summary: String,
    val description: String? = null,
    val location: String? = null,
    val dtStart: String = "",
    val dtEnd: String = "",
    val url: String? = null,
    val rRule: String? = null,
    val exDates: List<String> = emptyList(),
)
