package io.github.kroune.cumobile.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.presentation.common.AppColors
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
            .background(AppColors.Surface, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        // Exercise name
        Text(
            text = task.exercise.name,
            color = AppColors.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Course name
        Text(
            text = stripEmojiPrefix(task.course.name),
            color = AppColors.TextSecondary,
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
                color = AppColors.TextSecondary,
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
                color = if (overdue) AppColors.Error else AppColors.TextSecondary,
                fontSize = 12.sp,
            )
        }
    }
}
