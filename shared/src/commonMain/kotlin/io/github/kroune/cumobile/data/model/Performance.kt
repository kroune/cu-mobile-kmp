package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

// ── Student Performance (overall) ────────────────────────────────────

/**
 * Overall student performance across all courses.
 */
@Serializable
data class StudentPerformanceResponseApi(
    val courses: List<PerformanceCourseApi> = emptyList(),
)

/** Per-course total score in [StudentPerformanceResponseApi]. */
@Serializable
data class PerformanceCourseApi(
    val id: String,
    val name: String,
    val description: String? = null,
    val total: Int = 0,
)

// ── Course Exercises ─────────────────────────────────────────────────

/**
 * Course exercises response.
 */
@Serializable
data class CourseExercisesResponseApi(
    val id: String,
    val name: String,
    val isArchived: Boolean = false,
    val exercises: List<CourseExerciseApi> = emptyList(),
)

/** Single exercise within a course. */
@Serializable
data class CourseExerciseApi(
    val id: String,
    val name: String,
    val type: String = "",
    val activity: CourseExerciseActivityApi? = null,
    val theme: CourseExerciseThemeApi? = null,
)

/** Activity descriptor for a [CourseExerciseApi]. */
@Serializable
data class CourseExerciseActivityApi(
    val id: String,
    val name: String,
)

/** Theme descriptor for a [CourseExerciseApi]. */
@Serializable
data class CourseExerciseThemeApi(
    val id: String,
    val name: String,
)

// ── Course Student Performance ───────────────────────────────────────

/**
 * Per-course student performance with individual task scores.
 */
@Serializable
data class CourseStudentPerformanceResponseApi(
    val tasks: List<TaskScoreApi> = emptyList(),
)

/**
 * Individual task score within [CourseStudentPerformanceResponseApi].
 *
 * Known [state] values match [TaskApi] states.
 * Known [scoreSkillLevel] values: `"basic"`, `"medium"`, `"advanced"`.
 */
@Serializable
data class TaskScoreApi(
    val id: String,
    val state: String = "",
    val score: Double = 0.0,
    val scoreSkillLevel: String? = null,
    val extraScore: Double? = null,
    val exerciseId: String,
    val maxScore: Int = 10,
    val activity: TaskScoreActivityApi,
)

/** Activity descriptor for a [TaskScoreApi]. */
@Serializable
data class TaskScoreActivityApi(
    val id: String,
    val name: String,
    val weight: Double = 0.0,
    val averageScoreThreshold: Double? = null,
)
