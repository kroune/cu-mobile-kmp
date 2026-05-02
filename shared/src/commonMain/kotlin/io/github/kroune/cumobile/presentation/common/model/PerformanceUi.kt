package io.github.kroune.cumobile.presentation.common.model

data class ExerciseWithScoreUi(
    val exerciseId: String,
    val exerciseName: String,
    val exerciseType: String,
    val themeName: String,
    val activityName: String,
    val scoreValue: Double,
    val maxScore: Int,
    val statusLabel: String,
    val statusStyle: StatusStyle,
)

data class ActivitySummaryUi(
    val activityId: String,
    val activityName: String,
    val count: Int,
    val averageScore: Double,
    val weight: Double,
    val totalContribution: Double,
)
