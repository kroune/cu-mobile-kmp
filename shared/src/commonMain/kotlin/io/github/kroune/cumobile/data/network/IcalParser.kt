package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.CalendarEvent

/**
 * Manual parser for iCal (.ics) files.
 *
 * Handles event extraction (BEGIN:VEVENT / END:VEVENT),
 * line unfolding, and basic property mapping.
 */
internal class IcalParser {
    fun parse(ics: String): List<CalendarEvent> {
        val unfoldedLines = unfold(ics)
        val events = mutableListOf<CalendarEvent>()
        var inEvent = false
        val currentProps = mutableMapOf<String, String>()
        val currentExDates = mutableListOf<String>()

        for (line in unfoldedLines) {
            val trimmed = line.trim()
            when {
                trimmed == "BEGIN:VEVENT" -> {
                    inEvent = true
                    currentProps.clear()
                    currentExDates.clear()
                }
                trimmed == "END:VEVENT" -> {
                    if (inEvent) events.add(buildEvent(currentProps, currentExDates))
                    inEvent = false
                }
                inEvent -> parseProperty(trimmed, currentProps, currentExDates)
            }
        }
        return events
    }

    private fun buildEvent(
        map: Map<String, String>,
        exDates: List<String>,
    ): CalendarEvent =
        CalendarEvent(
            uid = map["UID"].orEmpty(),
            summary = map["SUMMARY"].orEmpty(),
            description = map["DESCRIPTION"],
            location = map["LOCATION"],
            dtStart = map["DTSTART"].orEmpty(),
            dtEnd = map["DTEND"].orEmpty(),
            url = map["URL"],
            rRule = map["RRULE"],
            exDates = exDates,
        )

    private fun parseProperty(
        trimmed: String,
        currentMap: MutableMap<String, String>,
        exDates: MutableList<String>,
    ) {
        val colonIndex = trimmed.indexOf(':')
        if (colonIndex <= 0) return
        val keyPart = trimmed.substring(0, colonIndex)
        val value = trimmed.substring(colonIndex + 1)
        val key = keyPart.substringBefore(';')

        when (key) {
            "EXDATE" -> exDates.addAll(value.split(','))
            else -> currentMap[key] = value
        }
    }

    private fun unfold(ics: String): List<String> {
        val lines = mutableListOf<String>()
        for (line in ics.lines()) {
            if (line.startsWith(" ") || line.startsWith("\t")) {
                if (lines.isNotEmpty()) {
                    lines[lines.size - 1] = lines.last() + line.substring(1)
                }
            } else {
                lines.add(line)
            }
        }
        return lines
    }
}
