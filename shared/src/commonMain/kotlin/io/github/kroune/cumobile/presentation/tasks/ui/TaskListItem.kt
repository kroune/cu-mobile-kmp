package io.github.kroune.cumobile.presentation.tasks.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.presentation.common.model.TaskUi
import io.github.kroune.cumobile.presentation.common.model.color
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.StatusBadge

/**
 * Task list item card matching the Flutter reference.
 *
 * Shows exercise name, course name, status badge, deadline,
 * and optional late-days info. Left border color matches task state.
 */
@Composable
fun TaskListItem(
    task: TaskUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColor = task.statusStyle.color()
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, statusColor, shape)
            .background(AppTheme.colors.surface, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Text(
            text = task.exerciseName,
            color = AppTheme.colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = task.courseName,
            color = AppTheme.colors.textSecondary,
            fontSize = 13.sp,
            maxLines = 1,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusBadge(
                label = task.statusLabel,
                color = statusColor,
            )

            DeadlineText(task = task)
        }

        val lateDaysText = task.lateDaysText
        if (lateDaysText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lateDaysText,
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
    task: TaskUi,
    modifier: Modifier = Modifier,
) {
    val deadlineFormatted = task.deadlineFormatted
    if (deadlineFormatted != null) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = deadlineFormatted,
                color = if (task.isOverdue) AppTheme.colors.error else AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }
    }
}
