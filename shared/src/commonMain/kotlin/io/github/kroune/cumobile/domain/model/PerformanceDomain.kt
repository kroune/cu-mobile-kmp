package io.github.kroune.cumobile.domain.model

data class CourseGradeDomain(
    val id: String,
    val name: String,
    val description: String?,
    val total: Int,
)

data class CourseExerciseDomain(
    val id: String,
    val name: String,
    val type: String,
    val activityId: String?,
    val activityName: String?,
    val themeId: String?,
    val themeName: String?,
)

data class ExerciseScoreDomain(
    val id: String,
    val status: TaskStatus,
    val score: Double,
    val extraScore: Double?,
    val exerciseId: String,
    val maxScore: Int,
    val activityId: String,
    val activityName: String,
    val activityWeight: Double,
    val averageScoreThreshold: Double?,
)
