package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.CourseExerciseDomain
import io.github.kroune.cumobile.domain.model.ExerciseScoreDomain
import io.github.kroune.cumobile.presentation.common.formatScore
import io.github.kroune.cumobile.presentation.common.model.ActivitySummaryUi
import io.github.kroune.cumobile.presentation.common.model.ExerciseWithScoreUi
import io.github.kroune.cumobile.presentation.common.model.label
import io.github.kroune.cumobile.presentation.common.model.toStatusStyle

private const val DefaultMaxScore = 10

fun toExerciseWithScoreUi(
    exercise: CourseExerciseDomain,
    score: ExerciseScoreDomain?,
): ExerciseWithScoreUi {
    val statusStyle = score?.status?.toStatusStyle()
    val scoreValue = score?.score ?: 0.0
    val maxScore = score?.maxScore ?: DefaultMaxScore
    return ExerciseWithScoreUi(
        exerciseId = exercise.id,
        exerciseName = exercise.name,
        exerciseType = exercise.type,
        themeName = exercise.themeName ?: "Без темы",
        activityName = exercise.activityName ?: "Без активности",
        scoreValue = scoreValue,
        maxScore = maxScore,
        scoreBadgeText = "${formatScore(scoreValue)} / $maxScore",
        statusLabel = statusStyle?.label() ?: "none",
        statusStyle = statusStyle
            ?: io.github.kroune.cumobile.presentation.common.model.StatusStyle.Backlog,
    )
}

fun toActivitySummaryUi(
    activityId: String,
    activityName: String,
    count: Int,
    averageScore: Double,
    weight: Double,
): ActivitySummaryUi {
    val totalContribution = averageScore * weight
    return ActivitySummaryUi(
        activityId = activityId,
        activityName = activityName,
        count = count,
        averageScore = averageScore,
        averageScoreFormatted = formatScore(averageScore),
        weight = weight,
        weightFormatted = formatScore(weight),
        totalContribution = totalContribution,
        totalContributionFormatted = formatScore(totalContribution),
    )
}
