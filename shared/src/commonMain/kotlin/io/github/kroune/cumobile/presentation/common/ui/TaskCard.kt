package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.formatDeadlineDayShortMonth
import io.github.kroune.cumobile.presentation.common.formatDeadlineTime
import io.github.kroune.cumobile.presentation.common.parseDeadlineInstant

private const val MillisPerHour = 3_600_000L
private const val MillisPerDay = 86_400_000L
private const val HoursPerDay = 24
private const val UrgencyRedHours = 24L
private const val UrgencyOrangeHours = 72L

/**
 * Compact task card for the Дедлайны row on the Home screen.
 *
 * Layout favours temporal scanning:
 *  - Right column: large time (primary) and urgency-colored date beneath it.
 *  - Left column: state chip, accent-colored course name, secondary task title.
 */
@Composable
fun DeadlineTaskCard(
    task: StudentTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateColor = taskStateColor(task.state)
    val urgency = deadlineUrgencyColor(task.exercise.deadline, stateColor)

    Row(
        modifier = modifier
            .width(240.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DeadlineStateChip(task = task, stateColor = stateColor)
            Text(
                text = stripEmojiPrefix(task.course.name),
                color = AppTheme.colors.accent,
                fontSize = 13.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stripEmojiPrefix(task.exercise.name),
                color = AppTheme.colors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatDeadlineTime(task.exercise.deadline),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatDeadlineDayShortMonth(task.exercise.deadline),
                fontSize = 15.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Bold,
                color = urgency,
            )
        }
    }
}

@Composable
private fun DeadlineStateChip(
    task: StudentTask,
    stateColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(stateColor.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = deadlineBadgeLabel(task.state, task.score),
            color = stateColor,
            fontSize = 10.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Colored pill used for state badges in list items and detail screens.
 */
@Composable
fun StatusBadge(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun deadlineBadgeLabel(
    state: String,
    score: Double?,
): String =
    if (state == TaskState.Evaluated && score != null) {
        "${score.toInt()}"
    } else {
        taskStateLabel(state)
    }

@Composable
private fun deadlineUrgencyColor(
    deadline: String?,
    stateColor: Color,
): Color {
    val instant = parseDeadlineInstant(deadline) ?: return stateColor
    val diffMs = instant.toEpochMilliseconds() - LocalClock.current.now().toEpochMilliseconds()
    if (diffMs <= 0) return AppTheme.colors.error
    val totalHours = diffMs / MillisPerHour
    val totalDaysCeil = (diffMs + MillisPerDay - 1) / MillisPerDay
    return when {
        totalHours < UrgencyRedHours -> AppTheme.colors.error
        totalDaysCeil <= UrgencyOrangeHours / HoursPerDay -> AppTheme.colors.taskReview
        else -> stateColor
    }
}
