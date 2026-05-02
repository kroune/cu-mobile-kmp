package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.CourseOverviewApi
import io.github.kroune.cumobile.data.model.CourseThemeApi
import io.github.kroune.cumobile.data.model.LongreadApi
import io.github.kroune.cumobile.data.model.ThemeExerciseApi
import io.github.kroune.cumobile.domain.model.CourseOverviewDomain
import io.github.kroune.cumobile.domain.model.CourseThemeDomain
import io.github.kroune.cumobile.domain.model.LongreadDomain
import io.github.kroune.cumobile.domain.model.ThemeExerciseDomain

fun CourseOverviewApi.toDomain(): CourseOverviewDomain =
    CourseOverviewDomain(
        id = id,
        name = name,
        isArchived = isArchived,
        themes = themes.map { it.toDomain() },
    )

fun CourseThemeApi.toDomain(): CourseThemeDomain =
    CourseThemeDomain(
        id = id,
        name = name,
        order = order,
        state = state,
        longreads = longreads.map { it.toDomain() },
    )

fun LongreadApi.toDomain(): LongreadDomain =
    LongreadDomain(
        id = id,
        type = type,
        name = name,
        state = state,
        exercises = exercises.map { it.toDomain() },
    )

fun ThemeExerciseApi.toDomain(): ThemeExerciseDomain =
    ThemeExerciseDomain(
        id = id,
        name = name,
        maxScore = maxScore,
        deadline = parseDeadlineInstant(deadline),
        activityName = activity?.name,
        activityWeight = activity?.weight,
    )
