package io.github.kroune.cumobile.presentation.common.ui

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

private val previewTask = TaskDomain(
    id = "",
    status = TaskStatus.InProgress,
    score = null,
    extraScore = null,
    deadline = null,
    submitAt = null,
    startedAt = null,
    exerciseId = "",
    exerciseName = "ДЗ: Линейные отображения",
    exerciseType = "",
    exerciseMaxScore = 0,
    exerciseDeadline = parseDeadlineInstant("2026-04-01T23:59:00"),
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
