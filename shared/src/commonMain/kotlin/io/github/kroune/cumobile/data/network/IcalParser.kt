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
        val currentProps = mutableMapOf<String, Any>()

        for (line in unfoldedLines) {
            val trimmed = line.trim()
            when {
                trimmed == "BEGIN:VEVENT" -> {
                    inEvent = true
                    currentProps.clear()
                }
                trimmed == "END:VEVENT" -> {
                    if (inEvent) events.add(buildEvent(currentProps))
                    inEvent = false
                }
                inEvent -> parseProperty(trimmed, currentProps)
            }
        }
        return events
    }

    private fun buildEvent(map: Map<String, Any>): CalendarEvent =
        CalendarEvent(
            uid = (map["UID"] as? String).orEmpty(),
            summary = (map["SUMMARY"] as? String).orEmpty(),
            description = map["DESCRIPTION"] as? String,
            location = map["LOCATION"] as? String,
            dtStart = (map["DTSTART"] as? String).orEmpty(),
            dtEnd = (map["DTEND"] as? String).orEmpty(),
            url = map["URL"] as? String,
            rRule = map["RRULE"] as? String,
            exDates = (map["EXDATE"] as? List<*>)
                ?.filterIsInstance<String>()
                .orEmpty(),
        )

    private fun parseProperty(
        trimmed: String,
        currentMap: MutableMap<String, Any>,
    ) {
        val colonIndex = trimmed.indexOf(':')
        if (colonIndex <= 0) return
        val keyPart = trimmed.substring(0, colonIndex)
        val value = trimmed.substring(colonIndex + 1)
        val key = keyPart.substringBefore(';')

        when (key) {
            "EXDATE" -> {
                @Suppress("UNCHECKED_CAST")
                val list = currentMap["EXDATE"] as? MutableList<String>
                    ?: mutableListOf<String>().also {
                        currentMap["EXDATE"] = it
                    }
                list.addAll(value.split(','))
            }
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
