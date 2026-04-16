package io.github.kroune.cumobile.presentation.tasks.ui

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
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.ui.LocalClock
import io.github.kroune.cumobile.presentation.common.ui.previewClock

private val previewTask = StudentTask(
    state = TaskState.InProgress,
    exercise = TaskExercise(name = "ДЗ: Деревья и графы", deadline = "2026-04-01T23:59:00"),
    course = TaskCourse(name = "Алгоритмы и структуры данных"),
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
private fun PreviewTaskListItemDark() {
    CuMobileTheme(darkTheme = true) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            Box(
                Modifier
                    .background(AppTheme.colors.background)
                    .padding(16.dp),
            ) {
                TaskListItem(task = previewTask, onClick = {})
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTaskListItemLight() {
    CuMobileTheme(darkTheme = false) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            Box(
                Modifier
                    .background(AppTheme.colors.background)
                    .padding(16.dp),
            ) {
                TaskListItem(task = previewTask, onClick = {})
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTaskListItemWithOffsetDark() {
    CuMobileTheme(darkTheme = true) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            Box(
                Modifier
                    .background(AppTheme.colors.background)
                    .padding(16.dp),
            ) {
                TaskListItem(task = previewTaskWithOffset, onClick = {})
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTaskListItemWithOffsetLight() {
    CuMobileTheme(darkTheme = false) {
        CompositionLocalProvider(LocalClock provides previewClock) {
            Box(
                Modifier
                    .background(AppTheme.colors.background)
                    .padding(16.dp),
            ) {
                TaskListItem(task = previewTaskWithOffset, onClick = {})
            }
        }
    }
}
