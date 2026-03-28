package io.github.kroune.cumobile.presentation.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.model.ClassData
import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskCourse
import io.github.kroune.cumobile.data.model.TaskExercise
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.home.HomeComponent

private val previewHomeState = HomeComponent.State(
    tasks = ContentState.Success(
        listOf(
            StudentTask(
                id = "1",
                state = TaskState.InProgress,
                exercise = TaskExercise(name = "ДЗ: Деревья", deadline = "2026-04-01T23:59:00"),
                course = TaskCourse(name = "Алгоритмы"),
            ),
            StudentTask(
                id = "2",
                state = TaskState.Backlog,
                exercise = TaskExercise(name = "Лабораторная 3", deadline = "2026-04-05T23:59:00"),
                course = TaskCourse(name = "Линейная алгебра"),
            ),
        ),
    ),
    courses = ContentState.Success(
        listOf(
            Course(id = "1", name = "Алгоритмы", category = "development"),
            Course(id = "2", name = "Линейная алгебра", category = "mathematics"),
            Course(id = "3", name = "Менеджмент", category = "business"),
        ),
    ),
    profileInitials = ContentState.Success("ИП"),
    lateDaysBalance = ContentState.Success(5),
    avatarBitmap = ContentState.Success(null),
    selectedDateMillis = 1774051200000L,
    schedule = ContentState.Success(emptyList()),
)

private val previewHomeEmptyState = HomeComponent.State(
    tasks = ContentState.Success(emptyList()),
    courses = ContentState.Success(emptyList()),
    profileInitials = ContentState.Success("ИП"),
    avatarBitmap = ContentState.Success(null),
    lateDaysBalance = ContentState.Success(null),
    selectedDateMillis = 1774051200000L,
    schedule = ContentState.Success(emptyList()),
)

private val previewHomeWithScheduleState = previewHomeState.copy(
    schedule = ContentState.Success(
        listOf(
            ClassData(
                startTime = "09:00",
                endTime = "10:30",
                title = "Математический анализ",
                room = "А-301",
                type = "Лекция",
            ),
            ClassData(
                startTime = "11:00",
                endTime = "12:30",
                title = "Программирование",
                room = "Б-204",
                type = "Семинар",
            ),
            ClassData(
                startTime = "14:00",
                endTime = "15:30",
                title = "Физика",
                room = "",
                type = "Лабораторная",
            ),
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
        HomeContent(state = previewHomeState, onIntent = {}, onTaskClick = {}, onCourseClick = {})
    }
}

@Preview
@Composable
private fun PreviewHomeScreenLight() {
    CuMobileTheme(darkTheme = false) {
        HomeContent(state = previewHomeState, onIntent = {}, onTaskClick = {}, onCourseClick = {})
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
        HomeContent(
            state = previewHomeEmptyState,
            onIntent = {},
            onTaskClick = {},
            onCourseClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewHomeScreenEmptyLight() {
    CuMobileTheme(darkTheme = false) {
        HomeContent(
            state = previewHomeEmptyState,
            onIntent = {},
            onTaskClick = {},
            onCourseClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewHomeWithScheduleDark() {
    CuMobileTheme(darkTheme = true) {
        HomeContent(
            state = previewHomeWithScheduleState,
            onIntent = {},
            onTaskClick = {},
            onCourseClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewHomeWithScheduleLight() {
    CuMobileTheme(darkTheme = false) {
        HomeContent(
            state = previewHomeWithScheduleState,
            onIntent = {},
            onTaskClick = {},
            onCourseClick = {},
        )
    }
}
