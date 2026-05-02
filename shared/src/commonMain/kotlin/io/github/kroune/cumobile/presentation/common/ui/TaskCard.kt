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
import io.github.kroune.cumobile.presentation.common.model.TaskUi
import io.github.kroune.cumobile.presentation.common.model.UrgencyLevel
import io.github.kroune.cumobile.presentation.common.model.color

/**
 * Compact task card for the Дедлайны row on the Home screen.
 *
 * Layout favours temporal scanning:
 *  - Right column: large date primary and urgency-colored date beneath it.
 *  - Left column: state chip, accent-colored course name, secondary task title.
 */
@Composable
fun DeadlineTaskCard(
    task: TaskUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColor = task.statusStyle.color()
    val urgency = when (task.urgencyLevel) {
        UrgencyLevel.Red -> AppTheme.colors.error
        UrgencyLevel.Orange -> AppTheme.colors.taskReview
        UrgencyLevel.Normal -> statusColor
    }

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
            DeadlineStateChip(task = task, statusColor = statusColor)
            Text(
                text = task.courseName,
                color = AppTheme.colors.accent,
                fontSize = 13.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = task.exerciseName,
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
                text = task.deadlineTimeFormatted,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = task.deadlineDayMonthFormatted,
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
    task: TaskUi,
    statusColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(statusColor.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = task.deadlineBadgeLabel,
            color = statusColor,
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
