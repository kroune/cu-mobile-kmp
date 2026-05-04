package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.CourseOverviewDomain
import io.github.kroune.cumobile.domain.model.CourseThemeDomain
import io.github.kroune.cumobile.domain.model.LongreadDomain
import io.github.kroune.cumobile.domain.model.ThemeExerciseDomain
import io.github.kroune.cumobile.presentation.common.formatDeadlineShort
import io.github.kroune.cumobile.presentation.common.model.CourseOverviewUi
import io.github.kroune.cumobile.presentation.common.model.CourseThemeUi
import io.github.kroune.cumobile.presentation.common.model.LongreadSummaryUi
import io.github.kroune.cumobile.presentation.common.model.ThemeExerciseUi
import kotlinx.collections.immutable.toImmutableList

fun CourseOverviewDomain.toUi(): CourseOverviewUi =
    CourseOverviewUi(
        id = id,
        name = name,
        isArchived = isArchived,
        themes = themes.map { it.toUi() }.toImmutableList(),
    )

fun CourseThemeDomain.toUi(): CourseThemeUi =
    CourseThemeUi(
        id = id,
        name = name,
        order = order,
        state = state,
        longreads = longreads.map { it.toUi() }.toImmutableList(),
    )

private fun LongreadDomain.toUi(): LongreadSummaryUi =
    LongreadSummaryUi(
        id = id,
        type = type,
        name = name,
        state = state,
        exercises = exercises.map { it.toUi() }.toImmutableList(),
    )

private fun ThemeExerciseDomain.toUi(): ThemeExerciseUi =
    ThemeExerciseUi(
        id = id,
        name = name,
        maxScore = maxScore,
        deadlineFormatted = formatDeadlineShort(deadline),
        activityName = activityName,
    )
