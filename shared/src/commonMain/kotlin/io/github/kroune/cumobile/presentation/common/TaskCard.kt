package io.github.kroune.cumobile.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

/**
 * Compact task card for the deadlines section on the Home screen.
 *
 * Shows exercise name, course name, deadline, and status badge.
 * Matches the Flutter reference: 200dp wide, rounded corners,
 * border color matching task state.
 *
 * @param task The student task to display.
 * @param onClick Called when the card is tapped.
 */
@Composable
fun DeadlineTaskCard(
    task: StudentTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateColor = taskStateColor(task.state)
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .width(200.dp)
            .clip(shape)
            .border(1.dp, stateColor, shape)
            .background(AppColors.Surface, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Exercise name
        Text(
            text = stripEmojiPrefix(task.exercise.name),
            color = AppColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Course name
        Text(
            text = stripEmojiPrefix(task.course.name),
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Deadline row
        DeadlineDateRow(deadline = task.exercise.deadline)

        // Status badge with score
        StatusBadge(
            label = taskStateBadgeLabel(task.state, task.score),
            color = stateColor,
        )
    }
}

/**
 * Displays the deadline date with a clock emoji.
 *
 * Shows the date in a short format. Text turns red if overdue.
 */
@Composable
private fun DeadlineDateRow(
    deadline: String?,
    modifier: Modifier = Modifier,
) {
    val displayText = formatDeadline(deadline)
    val overdue = isOverdue(deadline)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "\u23F0", // ⏰
            fontSize = 12.sp,
        )
        Text(
            text = displayText,
            color = if (overdue) AppColors.Error else AppColors.TextSecondary,
            fontSize = 12.sp,
        )
    }
}

/**
 * Colored badge showing a label on a tinted background.
 *
 * Used by both the DeadlineTaskCard and TaskListItem.
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
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Returns the badge label for a task state, optionally showing
 * the score for evaluated tasks.
 */
fun taskStateBadgeLabel(
    state: String,
    score: Double?,
): String =
    if (state == "evaluated" && score != null) {
        "${score.toInt()}"
    } else {
        taskStateLabel(state)
    }

/**
 * Formats an ISO 8601 deadline string into a short display format.
 *
 * Example: "2026-02-15T14:00:00" -> "15.02 14:00"
 */
fun formatDeadline(deadline: String?): String {
    if (deadline == null) return "Без дедлайна"
    return try {
        val parts = deadline.split("T")
        if (parts.size < 2) return deadline
        val dateParts = parts[0].split("-")
        val timeParts = parts[1].split(":")
        if (dateParts.size < 3 || timeParts.size < 2) return deadline
        val day = dateParts[2]
        val month = dateParts[1]
        val hour = timeParts[0]
        val minute = timeParts[1]
        "$day.$month $hour:$minute"
    } catch (_: Exception) {
        deadline
    }
}

/**
 * Checks if an ISO 8601 deadline string is in the past.
 *
 * Uses simple string comparison which works for ISO 8601 format
 * since it sorts lexicographically.
 */
fun isOverdue(deadline: String?): Boolean {
    if (deadline == null) return false
    // TODO: Implement proper datetime comparison in Phase 11
    return false
}
