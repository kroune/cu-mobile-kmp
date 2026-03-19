@file:Suppress("MagicNumber")

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
import io.github.kroune.cumobile.data.model.TaskState
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

private val logger = KotlinLogging.logger {}

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
private fun taskStateBadgeLabel(
    state: String,
    score: Double?,
): String =
    if (state == TaskState.Evaluated && score != null) {
        "${score.toInt()}"
    } else {
        taskStateLabel(state)
    }

/**
 * Checks if an ISO 8601 deadline string is in the past.
 *
 * Parses the deadline and compares it with the current time
 * using kotlinx-datetime for cross-platform compatibility.
 */
internal fun isOverdue(deadline: String?): Boolean {
    if (deadline == null) return false
    return try {
        val normalized = if (deadline.endsWith("Z")) {
            deadline.removeSuffix("Z")
        } else {
            deadline
        }
        val isoString = if (!normalized.contains("T")) {
            "${normalized}T23:59:59"
        } else {
            normalized
        }
        val deadlineDateTime = LocalDateTime.parse(isoString)
        val deadlineInstant = deadlineDateTime.toInstant(TimeZone.currentSystemDefault())
        val now = kotlin.time.Clock.System.now()
        deadlineInstant < now
    } catch (e: Exception) {
        logger.error(e) { "Failed to parse deadline for overdue check: $deadline" }
        false
    }
}
