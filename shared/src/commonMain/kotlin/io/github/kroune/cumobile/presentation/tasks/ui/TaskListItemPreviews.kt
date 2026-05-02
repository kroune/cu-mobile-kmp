package io.github.kroune.cumobile.presentation.tasks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kroune.cumobile.domain.model.TaskDomain
import io.github.kroune.cumobile.domain.model.TaskStatus
import io.github.kroune.cumobile.presentation.common.model.mappers.toUi
import io.github.kroune.cumobile.presentation.common.parseDeadlineInstant
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.ui.LocalClock
import io.github.kroune.cumobile.presentation.common.ui.previewClock

private val previewTask = TaskDomain(
    id = "",
    status = TaskStatus.InProgress,
    score = null,
    extraScore = null,
    deadline = null,
    submitAt = null,
    startedAt = null,
    exerciseId = "",
    exerciseName = "ДЗ: Деревья и графы",
    exerciseType = "",
    exerciseMaxScore = 0,
    exerciseDeadline = parseDeadlineInstant("2026-04-01T23:59:00"),
    activityName = null,
    activityWeight = null,
    courseId = "",
    courseName = "Алгоритмы и структуры данных",
    courseIsArchived = false,
    themeId = "",
    themeName = "",
    longreadId = "",
    isLateDaysEnabled = false,
    lateDays = null,
).toUi(previewClock.now())

private val previewTaskWithOffset = TaskDomain(
    id = "",
    status = TaskStatus.Backlog,
    score = null,
    extraScore = null,
    deadline = null,
    submitAt = null,
    startedAt = null,
    exerciseId = "",
    exerciseName = "Аудиторная работа",
    exerciseType = "",
    exerciseMaxScore = 0,
    exerciseDeadline = parseDeadlineInstant("2026-03-16T12:20:00+00:00"),
    activityName = null,
    activityWeight = null,
    courseId = "",
    courseName = "Введение в искусственный интеллект",
    courseIsArchived = false,
    themeId = "",
    themeName = "",
    longreadId = "",
    isLateDaysEnabled = false,
    lateDays = null,
).toUi(previewClock.now())

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
