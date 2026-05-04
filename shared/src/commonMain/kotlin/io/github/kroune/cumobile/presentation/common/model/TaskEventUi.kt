package io.github.kroune.cumobile.presentation.common.model

import kotlinx.collections.immutable.ImmutableList

data class TaskEventUi(
    val id: String,
    val occurredOnFormatted: String?,
    val type: String,
    val typeLabel: String,
    val actorName: String?,
    val content: TaskEventContentUi,
)

data class TaskEventContentUi(
    val statusStyle: StatusStyle?,
    val statusLabel: String?,
    val scoreFormatted: String?,
    val scoreValue: Double?,
    val scoreLevel: String?,
    val estimationDeadlineFormatted: String?,
    val estimationMaxScore: Int?,
    val estimationActivityName: String?,
    val solutionUrl: String?,
    val solutionAttachments: ImmutableList<MaterialAttachmentUi>,
    val reviewerName: String?,
    val reviewerNames: ImmutableList<String>,
    val name: String?,
    val lateDaysFormatted: String?,
    val deadlineFormatted: String?,
    val attached: ImmutableList<MaterialAttachmentUi>,
)
