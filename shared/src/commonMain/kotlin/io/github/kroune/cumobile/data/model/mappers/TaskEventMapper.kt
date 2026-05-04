package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.TaskEventApi
import io.github.kroune.cumobile.data.model.TaskEventContentApi
import io.github.kroune.cumobile.data.model.TaskEventEstimationApi
import io.github.kroune.cumobile.domain.model.TaskEventActorDomain
import io.github.kroune.cumobile.domain.model.TaskEventContentDomain
import io.github.kroune.cumobile.domain.model.TaskEventDomain
import io.github.kroune.cumobile.domain.model.TaskEventEstimationDomain
import io.github.kroune.cumobile.domain.model.TaskEventScoreDomain
import io.github.kroune.cumobile.domain.model.TaskEventSolutionDomain
import io.github.kroune.cumobile.domain.model.TaskEventTaskDomain

fun TaskEventApi.toDomain(): TaskEventDomain =
    TaskEventDomain(
        id = id,
        occurredOn = parseInstant(occurredOn),
        type = type,
        actorEmail = actorEmail,
        actorName = actorName,
        content = content.toDomain(),
    )

private fun TaskEventContentApi.toDomain(): TaskEventContentDomain =
    TaskEventContentDomain(
        state = state,
        score = score?.let { TaskEventScoreDomain(level = it.level, value = it.value) },
        estimation = estimation?.toDomain(),
        solution = solution?.let {
            TaskEventSolutionDomain(
                solutionUrl = it.solutionUrl,
                attachments = it.attachments.map { a -> a.toDomain() },
            )
        },
        reviewer = reviewer?.let {
            TaskEventActorDomain(
                lastName = it.name?.last,
                firstName = it.name?.first,
                middleName = it.name?.middle,
            )
        },
        reviewers = reviewers?.map {
            TaskEventActorDomain(
                lastName = it.name?.last,
                firstName = it.name?.first,
                middleName = it.name?.middle,
            )
        },
        task = task?.let {
            TaskEventTaskDomain(
                state = it.state,
                deadline = parseDeadlineInstant(it.deadline),
                estimation = it.estimation?.toDomain(),
            )
        },
        name = name,
        lateDays = lateDaysValue,
        deadline = parseDeadlineInstant(deadline),
        attached = attached?.map { it.toDomain() },
    )

private fun TaskEventEstimationApi.toDomain(): TaskEventEstimationDomain =
    TaskEventEstimationDomain(
        deadline = parseDeadlineInstant(deadline),
        maxScore = maxScore,
        activityName = activity?.name,
        activityWeight = activity?.weight,
    )
