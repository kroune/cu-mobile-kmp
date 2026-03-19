package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Student task from the tasks list.
 *
 * Known [state] values: `"backlog"`, `"inProgress"`, `"review"`,
 * `"revision"`, `"rework"`, `"failed"`, `"rejected"`, `"evaluated"`.
 *
 * The UI also derives a `"hasSolution"` virtual state when [state] is
 * `"inProgress"` and [submitAt] is not null.
 */
@Serializable
data class StudentTask(
    val id: Int = 0,
    val state: String = "",
    val score: Double? = null,
    /** ISO 8601 datetime string. */
    val deadline: String? = null,
    /** ISO 8601 datetime string. */
    val submitAt: String? = null,
    val exercise: TaskExercise = TaskExercise(),
    val course: TaskCourse = TaskCourse(),
    val isLateDaysEnabled: Boolean = false,
    val lateDays: Int? = null,
)

/**
 * Exercise summary embedded in a [StudentTask].
 *
 * Known [type] values: `"coding"`, `"quiz"`, `"essay"`.
 */
@Serializable
data class TaskExercise(
    val id: Int = 0,
    val name: String = "",
    val type: String = "",
    val maxScore: Int = 0,
    /** ISO 8601 datetime string. */
    val deadline: String? = null,
)

/** Course summary embedded in a [StudentTask]. */
@Serializable
data class TaskCourse(
    val id: Int = 0,
    val name: String = "",
    val isArchived: Boolean = false,
)
