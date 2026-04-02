package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskCourse
import io.github.kroune.cumobile.data.model.TaskExercise
import io.github.kroune.cumobile.data.model.TaskState

private val previewTask = StudentTask(
    state = TaskState.InProgress,
    exercise = TaskExercise(name = "ДЗ: Линейные отображения", deadline = "2026-04-01T23:59:00"),
    course = TaskCourse(name = "Линейная алгебра"),
)

private val previewTaskWithOffset = StudentTask(
    state = TaskState.Backlog,
    exercise = TaskExercise(
        name = "Аудиторная работа",
        deadline = "2026-03-16T12:20:00+00:00",
    ),
    course = TaskCourse(name = "Введение в искусственный интеллект"),
)

@Preview
@Composable
private fun PreviewDeadlineTaskCardDark() {
    CuMobileTheme(darkTheme = true) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
                DeadlineTaskCard(task = previewTask, onClick = {})
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDeadlineTaskCardLight() {
    CuMobileTheme(darkTheme = false) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
                DeadlineTaskCard(task = previewTask, onClick = {})
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDeadlineTaskCardWithOffsetDark() {
    CuMobileTheme(darkTheme = true) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
                DeadlineTaskCard(task = previewTaskWithOffset, onClick = {})
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDeadlineTaskCardWithOffsetLight() {
    CuMobileTheme(darkTheme = false) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
                DeadlineTaskCard(task = previewTaskWithOffset, onClick = {})
            }
        }
    }
}

@Preview
@Composable
private fun PreviewStatusBadgeDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
            StatusBadge(label = "В работе", color = AppTheme.colors.taskInProgress)
        }
    }
}

@Preview
@Composable
private fun PreviewStatusBadgeLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
            StatusBadge(label = "В работе", color = AppTheme.colors.taskInProgress)
        }
    }
}
