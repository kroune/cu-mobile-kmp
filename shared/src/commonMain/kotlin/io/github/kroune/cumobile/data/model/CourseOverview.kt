package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Full course overview with themes and longreads.
 */
@Serializable
data class CourseOverviewApi(
    val id: String = "",
    val name: String = "",
    val isArchived: Boolean = false,
    val themes: List<CourseThemeApi> = emptyList(),
)

/** Theme within a [CourseOverviewApi]. */
@Serializable
data class CourseThemeApi(
    val id: String = "",
    val name: String = "",
    val order: Int = 0,
    val state: String = "",
    val longreads: List<LongreadApi> = emptyList(),
) {
    /** Total number of exercises across all longreads in this theme. */
    val totalExercises: Int
        get() = longreads.sumOf { it.exercises.size }

    val hasExercises: Boolean
        get() = totalExercises > 0
}

/**
 * Longread entry within a [CourseThemeApi].
 *
 * Known [type] values: `"markdown"`, `"file"`, `"coding"`, `"questions"`.
 */
@Serializable
data class LongreadApi(
    val id: String = "",
    val type: String = "",
    val name: String = "",
    val state: String = "",
    val exercises: List<ThemeExerciseApi> = emptyList(),
)

/** Exercise entry within a [LongreadApi]. */
@Serializable
data class ThemeExerciseApi(
    val id: String = "",
    val name: String = "",
    val maxScore: Int = 0,
    /** ISO 8601 datetime string, e.g. `"2025-06-01T23:59:00Z"`. */
    val deadline: String? = null,
    val activity: ExerciseActivityApi? = null,
)

/** Grading activity descriptor for a [ThemeExerciseApi]. */
@Serializable
data class ExerciseActivityApi(
    val id: String = "",
    val name: String = "",
    val weight: Double = 0.0,
)
