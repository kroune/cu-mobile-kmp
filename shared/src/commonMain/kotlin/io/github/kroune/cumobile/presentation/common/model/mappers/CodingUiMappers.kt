package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.TaskCommentDomain
import io.github.kroune.cumobile.domain.model.TaskDetailsDomain
import io.github.kroune.cumobile.domain.model.TaskEventActorDomain
import io.github.kroune.cumobile.domain.model.TaskEventDomain
import io.github.kroune.cumobile.presentation.common.formatDeadlineInstant
import io.github.kroune.cumobile.presentation.common.model.TaskCommentUi
import io.github.kroune.cumobile.presentation.common.model.TaskDetailsExerciseUi
import io.github.kroune.cumobile.presentation.common.model.TaskDetailsSolutionUi
import io.github.kroune.cumobile.presentation.common.model.TaskDetailsUi
import io.github.kroune.cumobile.presentation.common.model.TaskEventContentUi
import io.github.kroune.cumobile.presentation.common.model.TaskEventUi
import io.github.kroune.cumobile.presentation.common.model.label
import io.github.kroune.cumobile.presentation.common.model.toStatusStyle
import kotlinx.collections.immutable.toImmutableList

private val eventTypeLabels = mapOf(
    "taskStarted" to "Начато",
    "taskSubmitted" to "Отправлено",
    "taskEvaluated" to "Принято",
    "taskRejected" to "Доработка",
    "taskFailed" to "Не сдано",
    "taskReset" to "Статус изменён",
    "taskExtraScoreGranted" to "Доп. баллы",
    "maxScoreChanged" to "Макс. балл изменён",
    "exerciseMaxScoreChanged" to "Макс. балл изменён",
    "exerciseEstimated" to "Задание выдано",
    "exerciseDeadlineChanged" to "Дедлайн изменён",
    "assistantAssigned" to "Назначен проверяющий",
    "reviewerAssigned" to "Назначен проверяющий",
    "taskProlonged" to "Дедлайн изменён",
    "solutionAttached" to "Файлы прикреплены",
    "taskLateDaysReset" to "Late days сброшены",
    "taskLateDaysCancelled" to "Late days возвращены",
    "taskLateDaysProlong" to "Late days списаны",
)

fun TaskDetailsDomain.toUi(): TaskDetailsUi {
    val statusStyle = status?.toStatusStyle()
    return TaskDetailsUi(
        id = id,
        scoreText = score?.let { "${it.toInt()}" },
        extraScoreText = extraScore?.let { "${it.toInt()}" },
        scoreSkillLevel = scoreSkillLevel,
        statusLabel = statusStyle?.label(),
        statusStyle = statusStyle,
        submitAtFormatted = formatDeadlineInstant(submitAt),
        isLateDaysEnabled = isLateDaysEnabled,
        lateDays = lateDays,
        deadline = deadline,
        deadlineFormatted = formatDeadlineInstant(deadline),
        startedAtFormatted = formatDeadlineInstant(startedAt),
        attemptStartedAtFormatted = formatDeadlineInstant(attemptStartedAt),
        quizSessionId = quizSessionId,
        currentAttemptId = currentAttemptId,
        evaluatedAttemptId = evaluatedAttemptId,
        lastAttemptId = lastAttemptId,
        exercise = exercise?.let {
            TaskDetailsExerciseUi(
                id = it.id,
                name = it.name,
                type = it.type,
                timer = it.timer,
                maxScore = it.maxScore,
                attemptsLimit = it.attemptsLimit,
                evaluationStrategy = it.evaluationStrategy,
            )
        },
        solution = solution?.let {
            TaskDetailsSolutionUi(
                solutionUrl = it.solutionUrl,
                attachments = it.attachments.map { a -> a.toUi() }.toImmutableList(),
                answers = it.answers.map { a -> a.toUi() }.toImmutableList(),
            )
        },
        studentLateDaysBalance = studentLateDaysBalance,
    )
}

fun TaskEventDomain.toUi(): TaskEventUi =
    TaskEventUi(
        id = id,
        occurredOnFormatted = formatDeadlineInstant(occurredOn),
        type = type,
        typeLabel = eventTypeLabels.getOrElse(type) { type },
        actorName = actorName,
        content = TaskEventContentUi(
            state = content.state,
            scoreValue = content.score?.value,
            scoreLevel = content.score?.level,
            estimationDeadlineFormatted = formatDeadlineInstant(content.estimation?.deadline),
            estimationMaxScore = content.estimation?.maxScore,
            estimationActivityName = content.estimation?.activityName,
            solutionUrl = content.solution?.solutionUrl,
            solutionAttachments = content.solution
                ?.attachments
                .orEmpty()
                .map { it.toUi() }
                .toImmutableList(),
            reviewerName = content.reviewer?.formatName(),
            reviewerNames = content.reviewers
                ?.mapNotNull { it.formatName() }
                .orEmpty()
                .toImmutableList(),
            name = content.name,
            lateDaysRaw = content.lateDaysRaw,
            deadlineFormatted = formatDeadlineInstant(content.deadline),
            attached = content.attached
                .orEmpty()
                .map { it.toUi() }
                .toImmutableList(),
        ),
    )

fun TaskCommentDomain.toUi(): TaskCommentUi =
    TaskCommentUi(
        id = id,
        content = content,
        senderName = sender.name,
        senderEmail = sender.email,
        createdAtFormatted = formatDeadlineInstant(createdAt),
        attachments = attachments.map { it.toUi() }.toImmutableList(),
        isEditable = isEditable,
        isDeletable = isDeletable,
    )

private fun TaskEventActorDomain.formatName(): String? {
    val parts = listOfNotNull(lastName, firstName, middleName)
    return parts.joinToString(" ").takeIf { it.isNotBlank() }
}
