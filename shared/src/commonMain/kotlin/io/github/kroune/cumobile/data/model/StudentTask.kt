package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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
    val id: String = "",
    val state: String = "",
    val score: Double? = null,
    val extraScore: Double? = null,
    val scoreSkillLevel: String? = null,
    /** ISO 8601 datetime string. */
    val deadline: String? = null,
    /** ISO 8601 datetime string. */
    val submitAt: String? = null,
    /** ISO 8601 datetime string — when the task was started. */
    val startedAt: String? = null,
    val exercise: TaskExercise = TaskExercise(),
    val course: TaskCourse = TaskCourse(),
    val isLateDaysEnabled: Boolean = false,
    val lateDays: Int? = null,
    /**
     * Reviewer info — polymorphic (may be an object, string, or null).
     * Used for presence-checking only.
     */
    val reviewer: JsonElement? = null,
)

/**
 * Exercise summary embedded in a [StudentTask].
 *
 * Known [type] values: `"coding"`, `"questions"`.
 */
@Serializable
data class TaskExercise(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val maxScore: Int = 0,
    /** ISO 8601 datetime string. */
    val deadline: String? = null,
    /** Duration string in "HH:MM:SS" format. */
    val timer: String? = null,
    val activity: TaskExerciseActivity? = null,
)

/** Course summary embedded in a [StudentTask]. */
@Serializable
data class TaskCourse(
    val id: String = "",
    val name: String = "",
    val isArchived: Boolean = false,
)

/** Activity info embedded in a [TaskExercise]. */
@Serializable
data class TaskExerciseActivity(
    val name: String = "",
    val weight: Double? = null,
    val isLateDaysEnabled: Boolean = false,
)
