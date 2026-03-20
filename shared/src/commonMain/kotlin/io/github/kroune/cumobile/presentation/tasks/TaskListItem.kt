package io.github.kroune.cumobile.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskCourse
import io.github.kroune.cumobile.data.model.TaskExercise
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.StatusBadge
import io.github.kroune.cumobile.presentation.common.formatDeadline
import io.github.kroune.cumobile.presentation.common.isOverdue
import io.github.kroune.cumobile.presentation.common.stripEmojiPrefix
import io.github.kroune.cumobile.presentation.common.taskStateColor
import io.github.kroune.cumobile.presentation.common.taskStateLabel

/**
 * Task list item card matching the Flutter reference.
 *
 * Shows exercise name, course name, status badge, deadline,
 * and optional late-days info. Left border color matches task state.
 */
@Composable
fun TaskListItem(
    task: StudentTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = effectiveTaskState(task)
    val stateColor = taskStateColor(state)
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, stateColor, shape)
            .background(AppTheme.colors.surface, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        // Exercise name
        Text(
            text = task.exercise.name,
            color = AppTheme.colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Course name
        Text(
            text = stripEmojiPrefix(task.course.name),
            color = AppTheme.colors.textSecondary,
            fontSize = 13.sp,
            maxLines = 1,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom row: status badge + deadline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusBadge(
                label = taskStateLabel(state),
                color = stateColor,
            )

            DeadlineText(task = task)
        }

        // Late days info (if enabled and used)
        if (task.isLateDaysEnabled && task.lateDays != null && task.lateDays > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Перенесено на ${task.lateDays} дн.",
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }
    }
}

/**
 * Deadline text with overdue coloring.
 */
@Composable
private fun DeadlineText(
    task: StudentTask,
    modifier: Modifier = Modifier,
) {
    val deadline = task.deadline ?: task.exercise.deadline
    if (deadline != null) {
        val overdue = isOverdue(deadline)
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "\uD83D\uDCC5", // 📅
                fontSize = 12.sp,
            )
            Text(
                text = formatDeadline(deadline),
                color = if (overdue) AppTheme.colors.error else AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }
    }
}

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
        Box(
            Modifier
                .background(AppTheme.colors.background)
                .padding(16.dp),
        ) {
            TaskListItem(task = previewTask, onClick = {})
        }
    }
}

@Preview
@Composable
private fun PreviewTaskListItemLight() {
    CuMobileTheme(darkTheme = false) {
        Box(
            Modifier
                .background(AppTheme.colors.background)
                .padding(16.dp),
        ) {
            TaskListItem(task = previewTask, onClick = {})
        }
    }
}

@Preview
@Composable
private fun PreviewTaskListItemWithOffsetDark() {
    CuMobileTheme(darkTheme = true) {
        Box(
            Modifier
                .background(AppTheme.colors.background)
                .padding(16.dp),
        ) {
            TaskListItem(task = previewTaskWithOffset, onClick = {})
        }
    }
}

@Preview
@Composable
private fun PreviewTaskListItemWithOffsetLight() {
    CuMobileTheme(darkTheme = false) {
        Box(
            Modifier
                .background(AppTheme.colors.background)
                .padding(16.dp),
        ) {
            TaskListItem(task = previewTaskWithOffset, onClick = {})
        }
    }
}
