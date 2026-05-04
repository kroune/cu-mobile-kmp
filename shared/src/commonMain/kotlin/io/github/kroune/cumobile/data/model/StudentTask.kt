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
data class TaskApi(
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
    val exercise: TaskExerciseApi = TaskExerciseApi(),
    val course: TaskCourseApi = TaskCourseApi(),
    val theme: TaskThemeApi = TaskThemeApi(),
    val longread: TaskLongreadApi = TaskLongreadApi(),
    val isLateDaysEnabled: Boolean = false,
    val lateDays: Int? = null,
    /**
     * Reviewer info — polymorphic (may be an object, string, or null).
     * Used for presence-checking only.
     */
    val reviewer: JsonElement? = null,
)

/**
 * Exercise summary embedded in a [TaskApi].
 *
 * Known [type] values: `"coding"`, `"questions"`.
 */
@Serializable
data class TaskExerciseApi(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val maxScore: Int = 0,
    /** ISO 8601 datetime string. */
    val deadline: String? = null,
    /** Duration string in "HH:MM:SS" format. */
    val timer: String? = null,
    val activity: TaskExerciseActivityApi? = null,
)

/** Course summary embedded in a [TaskApi]. */
@Serializable
data class TaskCourseApi(
    val id: String = "",
    val name: String = "",
    val isArchived: Boolean = false,
)

/** Activity info embedded in a [TaskExerciseApi]. */
@Serializable
data class TaskExerciseActivityApi(
    val name: String = "",
    val weight: Double? = null,
    val isLateDaysEnabled: Boolean = false,
)

/** Theme summary embedded in a [TaskApi]. */
@Serializable
data class TaskThemeApi(
    val id: String = "",
    val name: String = "",
)

/** Longread reference embedded in a [TaskApi]. */
@Serializable
data class TaskLongreadApi(
    val id: String = "",
)
