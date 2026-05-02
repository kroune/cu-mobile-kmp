package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.CourseExerciseApi
import io.github.kroune.cumobile.data.model.PerformanceCourseApi
import io.github.kroune.cumobile.data.model.TaskScoreApi
import io.github.kroune.cumobile.domain.model.CourseExerciseDomain
import io.github.kroune.cumobile.domain.model.CourseGradeDomain
import io.github.kroune.cumobile.domain.model.ExerciseScoreDomain
import io.github.kroune.cumobile.domain.model.TaskStatus

fun PerformanceCourseApi.toDomain(): CourseGradeDomain =
    CourseGradeDomain(
        id = id,
        name = name,
        description = description,
        total = total,
    )

fun CourseExerciseApi.toDomain(): CourseExerciseDomain =
    CourseExerciseDomain(
        id = id,
        name = name,
        type = type,
        activityId = activity?.id,
        activityName = activity?.name,
        themeId = theme?.id,
        themeName = theme?.name,
    )

fun TaskScoreApi.toDomain(): ExerciseScoreDomain =
    ExerciseScoreDomain(
        id = id,
        status = TaskStatus.fromApi(state),
        score = score,
        extraScore = extraScore,
        exerciseId = exerciseId,
        maxScore = maxScore,
        activityId = activity.id,
        activityName = activity.name,
        activityWeight = activity.weight,
        averageScoreThreshold = activity.averageScoreThreshold,
    )
