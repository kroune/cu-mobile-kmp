package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.TimetableCourseApi
import io.github.kroune.cumobile.domain.model.TimetableCalendarEventDomain
import io.github.kroune.cumobile.domain.model.TimetableCourseDomain
import io.github.kroune.cumobile.domain.model.TimetableEventRowDomain

fun TimetableCourseApi.toDomain(): TimetableCourseDomain =
    TimetableCourseDomain(
        courseId = courseId.toString(),
        courseName = courseName,
        eventRows = eventRows.map { row ->
            TimetableEventRowDomain(
                eventType = row.eventType,
                eventRowNumber = row.eventRowNumber,
                calendarEvent = row.calendarEvent?.let { event ->
                    TimetableCalendarEventDomain(
                        calendarEventId = event.calendarEventId,
                        eventType = event.eventType,
                        location = event.location,
                        hostName = event.host?.name,
                        hostEmail = event.host?.email,
                        startDate = event.schedule?.startDate.orEmpty(),
                        endDate = event.schedule?.endDate.orEmpty(),
                        startTime = event.schedule?.startTime.orEmpty(),
                        endTime = event.schedule?.endTime.orEmpty(),
                        dayOfWeek = event.schedule?.dayOfWeek.orEmpty(),
                        interval = event.schedule?.interval ?: 1,
                        comment = event.schedule?.comment,
                    )
                },
            )
        },
    )
