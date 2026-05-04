package io.github.kroune.cumobile.presentation.common.model

data class ExerciseWithScoreUi(
    val exerciseId: String,
    val exerciseName: String,
    val exerciseType: String,
    val themeName: String,
    val activityName: String,
    val scoreValue: Double,
    val maxScore: Int,
    /** Pre-formatted "score / maxScore" text for display. */
    val scoreBadgeText: String,
    val statusLabel: String,
    val statusStyle: StatusStyle,
)

data class ActivitySummaryUi(
    val activityId: String,
    val activityName: String,
    val count: Int,
    val averageScore: Double,
    val averageScoreFormatted: String,
    val weight: Double,
    val weightFormatted: String,
    val totalContribution: Double,
    val totalContributionFormatted: String,
)
