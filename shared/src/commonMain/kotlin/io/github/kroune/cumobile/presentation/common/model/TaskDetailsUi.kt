package io.github.kroune.cumobile.presentation.common.model

import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Instant

data class TaskDetailsUi(
    val id: String,
    val scoreText: String?,
    val extraScoreText: String?,
    val scoreSkillLevel: String?,
    val statusLabel: String?,
    val statusStyle: StatusStyle?,
    val submitAtFormatted: String?,
    val isLateDaysEnabled: Boolean,
    val lateDays: Int?,
    val deadline: Instant?,
    val deadlineFormatted: String?,
    val startedAtFormatted: String?,
    val attemptStartedAtFormatted: String?,
    val quizSessionId: String?,
    val currentAttemptId: String?,
    val evaluatedAttemptId: String?,
    val lastAttemptId: String?,
    val exercise: TaskDetailsExerciseUi?,
    val solution: TaskDetailsSolutionUi?,
    val studentLateDaysBalance: Int?,
)

data class TaskDetailsExerciseUi(
    val id: String?,
    val name: String?,
    val type: String?,
    val timer: String?,
    val maxScore: Double?,
    val attemptsLimit: Int?,
    val evaluationStrategy: String?,
)

data class TaskDetailsSolutionUi(
    val solutionUrl: String?,
    val attachments: ImmutableList<MaterialAttachmentUi>,
    val answers: ImmutableList<QuizAnswerResultUi>,
)
