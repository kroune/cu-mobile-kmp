package io.github.kroune.cumobile.presentation.longread.ui.coding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.model.TaskDetailsUi
import io.github.kroune.cumobile.presentation.common.model.TaskEventUi
import io.github.kroune.cumobile.presentation.common.model.color
import io.github.kroune.cumobile.presentation.common.ui.AppColorScheme
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.ShimmerBox
import io.github.kroune.cumobile.presentation.common.ui.StatusBadge
import kotlinx.collections.immutable.ImmutableList

/**
 * Info tab: task summary and events timeline.
 */
@Composable
internal fun InfoTab(
    taskDetails: TaskDetailsUi,
    events: ContentState<ImmutableList<TaskEventUi>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskInfoSummary(taskDetails)

        EventsTimeline(events)
    }
}

@Composable
private fun EventsTimeline(events: ContentState<ImmutableList<TaskEventUi>>) {
    when (events) {
        is ContentState.Loading -> EventsTimelineSkeleton()
        is ContentState.Error -> TimelineErrorText(events.message)
        is ContentState.Success -> {
            val list = events.data
            if (list.isEmpty()) return
            Text(
                text = "История",
                color = AppTheme.colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
            list.forEach { event ->
                EventCard(event)
                HorizontalDivider(
                    color = AppTheme.colors.textSecondary.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
private fun EventsTimelineSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShimmerBox(Modifier.fillMaxWidth(), height = 12.dp)
        ShimmerBox(Modifier.fillMaxWidth(), height = 12.dp)
        ShimmerBox(Modifier.fillMaxWidth(), height = 12.dp)
    }
}

@Composable
private fun TimelineErrorText(message: String) {
    Text(
        text = message,
        color = AppTheme.colors.textSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

/** Task info summary: status, score, deadline, late days. */
@Composable
private fun TaskInfoSummary(
    taskDetails: TaskDetailsUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val style = taskDetails.statusStyle
        InfoRow(
            label = "Статус",
            value = taskDetails.statusLabel ?: "—",
            valueColor = style?.color() ?: AppTheme.colors.textPrimary,
        )
        InfoRow(
            label = "Оценка",
            value = "${taskDetails.scoreText ?: "-"} / ${taskDetails.exercise?.maxScoreFormatted ?: "-"}",
        )
        InfoRow(
            label = "Дедлайн",
            value = taskDetails.deadlineFormatted ?: "—",
        )
        if (taskDetails.isLateDaysEnabled) {
            InfoRow(
                label = "Late days",
                value = "Исп.: ${taskDetails.lateDays ?: 0}" +
                    " | Баланс: ${taskDetails.studentLateDaysBalance ?: 0}",
            )
        }
        taskDetails.solution?.solutionUrl?.let { url ->
            InfoRow(label = "Решение", value = url)
        }
    }
}

/** Label-value row for the task info summary. */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = AppTheme.colors.textPrimary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = AppTheme.colors.textSecondary,
            fontSize = 13.sp,
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/** Single event card in the timeline. */
@Composable
private fun EventCard(
    event: TaskEventUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatusBadge(
                label = event.typeLabel,
                color = eventTypeColor(event.type),
            )
            event.occurredOnFormatted?.let { date ->
                Text(
                    text = date,
                    color = AppTheme.colors.textSecondary,
                    fontSize = 11.sp,
                )
            }
        }

        event.actorName?.let { name ->
            Text(
                text = name,
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }

        val statusLabel = event.content.statusLabel
        val statusStyle = event.content.statusStyle
        if (statusLabel != null && statusStyle != null) {
            Text(
                text = "Статус: $statusLabel",
                color = statusStyle.color(),
                fontSize = 12.sp,
            )
        }

        event.content.scoreFormatted?.let { formatted ->
            Text(
                text = "Оценка: $formatted",
                color = AppTheme.colors.taskEvaluated,
                fontSize = 12.sp,
            )
        }

        event.content.lateDaysFormatted?.let { days ->
            Text(
                text = "Late days: $days",
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }
    }
}

private val eventTypeColorAccessors: Map<String, (AppColorScheme) -> Color> = mapOf(
    "taskStarted" to { it.taskInProgress },
    "taskSubmitted" to { it.taskReview },
    "solutionAttached" to { it.taskHasSolution },
    "taskEvaluated" to { it.taskEvaluated },
    "taskExtraScoreGranted" to { it.taskEvaluated },
    "taskRejected" to { it.taskFailed },
    "taskFailed" to { it.taskFailed },
    "taskReset" to { it.taskBacklog },
    "exerciseEstimated" to { it.taskBacklog },
    "reviewerAssigned" to { it.accent },
    "assistantAssigned" to { it.accent },
)

@Composable
private fun eventTypeColor(type: String): Color =
    eventTypeColorAccessors[type]?.invoke(AppTheme.colors) ?: AppTheme.colors.textSecondary
