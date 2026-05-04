package io.github.kroune.cumobile.domain.model

import kotlin.time.Instant

data class TaskEventDomain(
    val id: String,
    val occurredOn: Instant?,
    val type: String,
    val actorEmail: String?,
    val actorName: String?,
    val content: TaskEventContentDomain,
)

data class TaskEventContentDomain(
    val state: String?,
    val score: TaskEventScoreDomain?,
    val estimation: TaskEventEstimationDomain?,
    val solution: TaskEventSolutionDomain?,
    val reviewer: TaskEventActorDomain?,
    val reviewers: List<TaskEventActorDomain>?,
    val task: TaskEventTaskDomain?,
    val name: String?,
    val lateDays: Int?,
    val deadline: Instant?,
    val attached: List<MaterialAttachmentDomain>?,
)

data class TaskEventScoreDomain(
    val level: String?,
    val value: Double?,
)

data class TaskEventEstimationDomain(
    val deadline: Instant?,
    val maxScore: Int?,
    val activityName: String?,
    val activityWeight: Double?,
)

data class TaskEventSolutionDomain(
    val solutionUrl: String?,
    val attachments: List<MaterialAttachmentDomain>,
)

data class TaskEventActorDomain(
    val lastName: String?,
    val firstName: String?,
    val middleName: String?,
)

data class TaskEventTaskDomain(
    val state: String?,
    val deadline: Instant?,
    val estimation: TaskEventEstimationDomain?,
)
