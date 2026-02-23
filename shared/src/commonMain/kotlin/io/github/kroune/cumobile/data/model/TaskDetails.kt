package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Detailed task information.
 *
 * API endpoint: `GET /micro-lms/tasks/{taskId}`
 *
 * The raw JSON contains nested `exercise`, `solution`, and `student` objects.
 * These are represented as separate DTO classes to enable direct deserialization.
 *
 * Known [scoreSkillLevel] values: `"basic"` / `"base"` (level 1),
 * `"medium"` / `"intermediate"` (level 2), `"advanced"` / `"pro"` (level 3).
 * May also arrive as an integer (1, 2, 3).
 */
@Serializable
data class TaskDetails(
    val id: Int = 0,
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
    val exercise: TaskDetailsExercise? = null,
    val solution: TaskDetailsSolution? = null,
    val student: TaskDetailsStudent? = null,
) {
    /** Convenience accessor for `exercise.maxScore`. */
    val maxScore: Int?
        get() = exercise?.maxScore

    /** Whether a solution has been submitted. */
    val hasSolution: Boolean
        get() = submitAt != null

    /** Convenience accessor for `solution.solutionUrl`. */
    val solutionUrl: String?
        get() = solution?.solutionUrl

    /** Convenience accessor for `solution.attachments`. */
    val solutionAttachments: List<MaterialAttachment>
        get() = solution?.attachments.orEmpty()

    /** Convenience accessor for `student.lateDaysBalance`. */
    val lateDaysBalance: Int?
        get() = student?.lateDaysBalance
}

/** Nested exercise info within [TaskDetails] JSON. */
@Serializable
data class TaskDetailsExercise(
    val maxScore: Int? = null,
)

/** Nested solution info within [TaskDetails] JSON. */
@Serializable
data class TaskDetailsSolution(
    val solutionUrl: String? = null,
    val attachments: List<MaterialAttachment> = emptyList(),
)

/** Nested student info within [TaskDetails] JSON. */
@Serializable
data class TaskDetailsStudent(
    val lateDaysBalance: Int? = null,
)
