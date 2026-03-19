package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

// ── Student Performance (overall) ────────────────────────────────────

/**
 * Overall student performance across all courses.
 */
@Serializable
data class StudentPerformanceResponse(
    val courses: List<StudentPerformanceCourse> = emptyList(),
)

/** Per-course total score in [StudentPerformanceResponse]. */
@Serializable
data class StudentPerformanceCourse(
    val id: Int,
    val name: String,
    val description: String? = null,
    val total: Int = 0,
)

// ── Course Exercises ─────────────────────────────────────────────────

/**
 * Course exercises response.
 */
@Serializable
data class CourseExercisesResponse(
    val id: Int,
    val name: String,
    val isArchived: Boolean = false,
    val exercises: List<CourseExercise> = emptyList(),
)

/** Single exercise within a course. */
@Serializable
data class CourseExercise(
    val id: Int,
    val name: String,
    val type: String = "",
    val activity: CourseExerciseActivity? = null,
    val theme: CourseExerciseTheme? = null,
)

/** Activity descriptor for a [CourseExercise]. */
@Serializable
data class CourseExerciseActivity(
    val id: Int,
    val name: String,
)

/** Theme descriptor for a [CourseExercise]. */
@Serializable
data class CourseExerciseTheme(
    val id: Int,
    val name: String,
)

// ── Course Student Performance ───────────────────────────────────────

/**
 * Per-course student performance with individual task scores.
 */
@Serializable
data class CourseStudentPerformanceResponse(
    val tasks: List<TaskScore> = emptyList(),
)

/**
 * Individual task score within [CourseStudentPerformanceResponse].
 *
 * Known [state] values match [StudentTask] states.
 * Known [scoreSkillLevel] values: `"basic"`, `"medium"`, `"advanced"`.
 */
@Serializable
data class TaskScore(
    val id: Int,
    val state: String = "",
    val score: Double = 0.0,
    val scoreSkillLevel: String? = null,
    val extraScore: Double? = null,
    val exerciseId: Int,
    val maxScore: Int = 10,
    val activity: TaskScoreActivity,
)

/** Activity descriptor for a [TaskScore]. */
@Serializable
data class TaskScoreActivity(
    val id: Int,
    val name: String,
    val weight: Double = 0.0,
    val averageScoreThreshold: Double? = null,
)
