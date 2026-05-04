package io.github.kroune.cumobile.presentation.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.domain.model.ClassDataDomain
import io.github.kroune.cumobile.domain.model.CourseCategory
import io.github.kroune.cumobile.domain.model.CourseDomain
import io.github.kroune.cumobile.domain.model.TaskDomain
import io.github.kroune.cumobile.domain.model.TaskStatus
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.model.ClassDataUi
import io.github.kroune.cumobile.presentation.common.model.mappers.toUi
import io.github.kroune.cumobile.presentation.common.parseDeadlineInstant
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.common.ui.LocalClock
import io.github.kroune.cumobile.presentation.common.ui.previewClock
import io.github.kroune.cumobile.presentation.home.HomeComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

private fun ClassDataDomain.toUi() =
    ClassDataUi(
        startTime = startTime,
        endTime = endTime,
        room = room,
        type = type,
        title = title,
        professor = professor,
        link = link,
        badge = badge,
    )

private val previewMonday = LocalDate(2026, 3, 23)
private val previewToday = LocalDate(2026, 3, 30)

private val previewTasks = persistentListOf(
    TaskDomain(
        id = "1",
        status = TaskStatus.InProgress,
        score = null,
        extraScore = null,
        deadline = null,
        submitAt = null,
        startedAt = null,
        exerciseId = "",
        exerciseName = "ДЗ: Деревья",
        exerciseType = "",
        exerciseMaxScore = 0,
        exerciseDeadline = parseDeadlineInstant("2026-04-01T23:59:00"),
        activityName = null,
        activityWeight = null,
        courseId = "",
        courseName = "Алгоритмы",
        courseIsArchived = false,
        themeId = "",
        themeName = "",
        longreadId = "",
        isLateDaysEnabled = false,
        lateDays = null,
    ).toUi(previewClock.now()),
    TaskDomain(
        id = "2",
        status = TaskStatus.Backlog,
        score = null,
        extraScore = null,
        deadline = null,
        submitAt = null,
        startedAt = null,
        exerciseId = "",
        exerciseName = "Лабораторная 3",
        exerciseType = "",
        exerciseMaxScore = 0,
        exerciseDeadline = parseDeadlineInstant("2026-04-05T23:59:00"),
        activityName = null,
        activityWeight = null,
        courseId = "",
        courseName = "Линейная алгебра",
        courseIsArchived = false,
        themeId = "",
        themeName = "",
        longreadId = "",
        isLateDaysEnabled = false,
        lateDays = null,
    ).toUi(previewClock.now()),
)

private val previewCourses = persistentListOf(
    CourseDomain(
        id = "1",
        name = "Алгоритмы",
        isArchived = false,
        category = CourseCategory.Development,
        categoryCoverUrl = "",
    ).toUi(),
    CourseDomain(
        id = "2",
        name = "Линейная алгебра",
        isArchived = false,
        category = CourseCategory.Mathematics,
        categoryCoverUrl = "",
    ).toUi(),
    CourseDomain(
        id = "3",
        name = "Менеджмент",
        isArchived = false,
        category = CourseCategory.Business,
        categoryCoverUrl = "",
    ).toUi(),
)

private val previewHomeState = HomeComponent.State(
    tasks = ContentState.Success(previewTasks),
    courses = ContentState.Success(previewCourses),
    selectedDate = previewToday,
    weekStart = previewMonday,
    weekRangeLabel = "23 - 29 марта",
    schedule = ContentState.Success(persistentListOf()),
    deadlineTasks = previewTasks,
    activeCourses = previewCourses,
)

private val previewHomeEmptyState = HomeComponent.State(
    tasks = ContentState.Success(persistentListOf()),
    courses = ContentState.Success(persistentListOf()),
    selectedDate = previewToday,
    weekStart = previewMonday,
    weekRangeLabel = "23 - 29 марта",
    schedule = ContentState.Success(persistentListOf()),
)

val previewHomeWithScheduleState = previewHomeState.copy(
    schedule = ContentState.Success(
        persistentListOf(
            ClassDataDomain(
                startTime = "09:00",
                endTime = "10:30",
                title = "Математический анализ",
                room = "А-301",
                type = "Лекция",
            ).toUi(),
            ClassDataDomain(
                startTime = "09:00",
                endTime = "10:30",
                title = "Основы математического анализа и линейной алгебры 2",
                room = "А-301",
                type = "Лекция",
            ).toUi(),
            ClassDataDomain(
                startTime = "11:00",
                endTime = "12:30",
                title = "Программирование",
                room = "Б-204",
                type = "Семинар",
            ).toUi(),
            ClassDataDomain(
                startTime = "14:00",
                endTime = "15:30",
                title = "Физика",
                room = "",
                type = "Лабораторная",
            ).toUi(),
        ),
    ),
)

@Preview
@Composable
private fun PreviewHomeScreenSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        HomeScreenSkeleton()
    }
}

@Preview
@Composable
private fun PreviewHomeScreenSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        HomeScreenSkeleton()
    }
}

@Preview
@Composable
private fun PreviewHomeScreenDark() {
    CuMobileTheme(darkTheme = true) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            HomeContent(state = previewHomeState, onIntent = {}, onTaskClick = {}, onCourseClick = {})
        }
    }
}

@Preview
@Composable
private fun PreviewHomeScreenLight() {
    CuMobileTheme(darkTheme = false) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            HomeContent(state = previewHomeState, onIntent = {}, onTaskClick = {}, onCourseClick = {})
        }
    }
}

@Preview
@Composable
private fun PreviewHomeScreenErrorDark() {
    CuMobileTheme(darkTheme = true) {
        Box(
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
        ) {
            ErrorContent(
                error = "Не удалось загрузить данные",
                onRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewHomeScreenErrorLight() {
    CuMobileTheme(darkTheme = false) {
        Box(
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
        ) {
            ErrorContent(
                error = "Не удалось загрузить данные",
                onRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewHomeScreenLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        HomeScreenSkeleton()
    }
}

@Preview
@Composable
private fun PreviewHomeScreenEmptyDark() {
    CuMobileTheme(darkTheme = true) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            HomeContent(
                state = previewHomeEmptyState,
                onIntent = {},
                onTaskClick = {},
                onCourseClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewHomeScreenEmptyLight() {
    CuMobileTheme(darkTheme = false) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            HomeContent(
                state = previewHomeEmptyState,
                onIntent = {},
                onTaskClick = {},
                onCourseClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewHomeWithScheduleLight() {
    CuMobileTheme(darkTheme = false) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            HomeContent(
                state = previewHomeWithScheduleState,
                onIntent = {},
                onTaskClick = {},
                onCourseClick = {},
            )
        }
    }
}
