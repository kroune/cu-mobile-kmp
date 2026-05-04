package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Detailed task information.
 *
 * The raw JSON contains nested `exercise`, `solution`, and `student` objects.
 * These are represented as separate DTO classes to enable direct deserialization.
 *
 * Known [scoreSkillLevel] values: `"basic"` / `"base"` (level 1),
 * `"medium"` / `"intermediate"` (level 2), `"advanced"` / `"pro"` (level 3).
 * May also arrive as an integer (1, 2, 3).
 */
@Serializable
data class TaskDetailsApi(
    val id: String = "",
    val score: Double? = null,
    val extraScore: Double? = null,
    val scoreSkillLevel: String? = null,
    val state: String? = null,
    /** ISO 8601 datetime string. */
    val submitAt: String? = null,
    val isLateDaysEnabled: Boolean = false,
    val lateDays: Int? = null,
    /** ISO 8601 datetime string. */
    val deadline: String? = null,
    /** ISO 8601 datetime string — when the task was started. */
    val startedAt: String? = null,
    /** ISO 8601 datetime string — when the current attempt was started. */
    val attemptStartedAt: String? = null,
    val quizSessionId: String? = null,
    val currentAttemptId: String? = null,
    val evaluatedAttemptId: String? = null,
    val lastAttemptId: String? = null,
    val exercise: TaskDetailsExerciseApi? = null,
    val solution: TaskDetailsSolutionApi? = null,
    val student: TaskDetailsStudentApi? = null,
) {
    /** Convenience accessor for `exercise.maxScore`. */
    val maxScore: Double?
        get() = exercise?.maxScore

    /** Whether a solution has been submitted. */
    val hasSolution: Boolean
        get() = submitAt != null

    /** Convenience accessor for `solution.solutionUrl`. */
    val solutionUrl: String?
        get() = solution?.solutionUrl

    /** Convenience accessor for `solution.attachments`. */
    val solutionAttachments: List<MaterialAttachmentApi>
        get() = solution?.attachments.orEmpty()

    /** Convenience accessor for `student.lateDaysBalance`. */
    val lateDaysBalance: Int?
        get() = student?.lateDaysBalance
}

/** Nested exercise info within [TaskDetailsApi] JSON. */
@Serializable
data class TaskDetailsExerciseApi(
    val id: String? = null,
    val name: String? = null,
    val type: String? = null,
    val timer: String? = null,
    val maxScore: Double? = null,
    val settings: ExerciseSettingsApi? = null,
)

@Serializable
data class ExerciseSettingsApi(
    val attemptsLimit: Int? = null,
    val evaluationStrategy: EvaluationStrategy? = null,
)

/** Nested solution info within [TaskDetailsApi] JSON. */
@Serializable
data class TaskDetailsSolutionApi(
    val solutionUrl: String? = null,
    val attachments: List<MaterialAttachmentApi> = emptyList(),
    val answers: List<QuizAnswerResultApi> = emptyList(),
)

/** Nested student info within [TaskDetailsApi] JSON. */
@Serializable
data class TaskDetailsStudentApi(
    val lateDaysBalance: Int? = null,
)
