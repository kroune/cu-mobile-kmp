package io.github.kroune.cumobile.domain.model

import kotlin.time.Instant

data class TaskDetailsDomain(
    val id: String,
    val score: Double?,
    val extraScore: Double?,
    val scoreSkillLevel: String?,
    val status: TaskStatus?,
    val submitAt: Instant?,
    val isLateDaysEnabled: Boolean,
    val lateDays: Int?,
    val deadline: Instant?,
    val startedAt: Instant?,
    val attemptStartedAt: Instant?,
    val quizSessionId: String?,
    val currentAttemptId: String?,
    val evaluatedAttemptId: String?,
    val lastAttemptId: String?,
    val exercise: TaskDetailsExerciseDomain?,
    val solution: TaskDetailsSolutionDomain?,
    val studentLateDaysBalance: Int?,
)

data class TaskDetailsExerciseDomain(
    val id: String?,
    val name: String?,
    val type: String?,
    val timer: String?,
    val maxScore: Double?,
    val attemptsLimit: Int?,
    val evaluationStrategy: String?,
)

data class TaskDetailsSolutionDomain(
    val solutionUrl: String?,
    val attachments: List<MaterialAttachmentDomain>,
    val answers: List<QuizAnswerResultDomain>,
)
