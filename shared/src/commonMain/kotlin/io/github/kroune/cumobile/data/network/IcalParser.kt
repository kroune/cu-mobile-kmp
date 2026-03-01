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
        var currentMap: MutableMap<String, Any>? = null

        for (line in unfoldedLines) {
            val trimmed = line.trim()
            when {
                trimmed == "BEGIN:VEVENT" -> currentMap = mutableMapOf()
                trimmed == "END:VEVENT" -> {
                    currentMap?.let { map ->
                        events.add(
                            CalendarEvent(
                                uid = map["UID"] as? String ?: "",
                                summary = map["SUMMARY"] as? String ?: "",
                                description = map["DESCRIPTION"] as? String,
                                location = map["LOCATION"] as? String,
                                dtStart = map["DTSTART"] as? String ?: "",
                                dtEnd = map["DTEND"] as? String ?: "",
                                url = map["URL"] as? String,
                                rRule = map["RRULE"] as? String,
                                exDates = (map["EXDATE"] as? List<*>)?.filterIsInstance<String>()
                                    ?: emptyList(),
                            ),
                        )
                    }
                    currentMap = null
                }
                currentMap != null -> {
                    val colonIndex = trimmed.indexOf(':')
                    if (colonIndex > 0) {
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
                }
            }
        }
        return events
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
