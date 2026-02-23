package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Full course overview with themes and longreads.
 *
 * API endpoint: `GET /micro-lms/courses/{courseId}/overview`
 */
@Serializable
data class CourseOverview(
    val id: Int = 0,
    val name: String = "",
    val isArchived: Boolean = false,
    val themes: List<CourseTheme> = emptyList(),
)

/** Theme within a [CourseOverview]. */
@Serializable
data class CourseTheme(
    val id: Int = 0,
    val name: String = "",
    val order: Int = 0,
    val state: String = "",
    val longreads: List<Longread> = emptyList(),
) {
    /** Total number of exercises across all longreads in this theme. */
    val totalExercises: Int
        get() = longreads.sumOf { it.exercises.size }

    val hasExercises: Boolean
        get() = totalExercises > 0
}

/**
 * Longread entry within a [CourseTheme].
 *
 * Known [type] values: `"markdown"`, `"file"`, `"coding"`, `"questions"`.
 */
@Serializable
data class Longread(
    val id: Int = 0,
    val type: String = "",
    val name: String = "",
    val state: String = "",
    val exercises: List<ThemeExercise> = emptyList(),
)

/** Exercise entry within a [Longread]. */
@Serializable
data class ThemeExercise(
    val id: Int = 0,
    val name: String = "",
    val maxScore: Int = 0,
    /** ISO 8601 datetime string, e.g. `"2025-06-01T23:59:00Z"`. */
    val deadline: String? = null,
    val activity: ExerciseActivity? = null,
)

/** Grading activity descriptor for a [ThemeExercise]. */
@Serializable
data class ExerciseActivity(
    val id: Int = 0,
    val name: String = "",
    val weight: Double = 0.0,
)
