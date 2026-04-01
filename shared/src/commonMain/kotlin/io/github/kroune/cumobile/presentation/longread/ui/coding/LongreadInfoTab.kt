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
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent
import io.github.kroune.cumobile.presentation.common.formatDateTime
import io.github.kroune.cumobile.presentation.common.formatDeadline
import io.github.kroune.cumobile.presentation.common.ui.AppColorScheme
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.StatusBadge
import io.github.kroune.cumobile.presentation.common.ui.taskStateColor
import io.github.kroune.cumobile.presentation.common.ui.taskStateLabel

/**
 * Info tab: task summary and events timeline.
 */
@Composable
internal fun InfoTab(
    taskDetails: TaskDetails,
    events: List<TaskEvent>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskInfoSummary(taskDetails)

        if (events.isNotEmpty()) {
            Text(
                text = "История",
                color = AppTheme.colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
            events.forEach { event ->
                EventCard(event)
                HorizontalDivider(
                    color = AppTheme.colors.textSecondary.copy(alpha = 0.2f),
                )
            }
        }
    }
}

/** Task info summary: status, score, deadline, late days. */
@Composable
private fun TaskInfoSummary(
    taskDetails: TaskDetails,
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
        InfoRow(
            label = "Статус",
            value = taskStateLabel(taskDetails.state.orEmpty()),
            valueColor = taskStateColor(taskDetails.state.orEmpty()),
        )
        InfoRow(
            label = "Оценка",
            value = "${taskDetails.score?.toInt() ?: "-"} / ${taskDetails.maxScore ?: "-"}",
        )
        InfoRow(
            label = "Дедлайн",
            value = formatDeadline(taskDetails.deadline),
        )
        if (taskDetails.isLateDaysEnabled) {
            InfoRow(
                label = "Late days",
                value = "Исп.: ${taskDetails.lateDays ?: 0}" +
                    " | Баланс: ${taskDetails.lateDaysBalance ?: 0}",
            )
        }
        taskDetails.solutionUrl?.let { url ->
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
    event: TaskEvent,
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
                label = eventTypeLabel(event.type),
                color = eventTypeColor(event.type),
            )
            event.occurredOn?.let { date ->
                Text(
                    text = formatDateTime(date),
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

        event.content.state?.let { state ->
            Text(
                text = "Статус: ${taskStateLabel(state)}",
                color = taskStateColor(state),
                fontSize = 12.sp,
            )
        }

        event.content.score?.let { score ->
            score.value?.let { value ->
                Text(
                    text = "Оценка: ${value.toInt()}",
                    color = AppTheme.colors.taskEvaluated,
                    fontSize = 12.sp,
                )
            }
        }

        event.content.lateDaysValue?.let { days ->
            Text(
                text = "Late days: $days",
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }
    }
}

private val eventTypeLabels = mapOf(
    "taskStarted" to "Начато",
    "taskSubmitted" to "Отправлено",
    "taskEvaluated" to "Принято",
    "taskRejected" to "Доработка",
    "taskFailed" to "Не сдано",
    "taskReset" to "Статус изменён",
    "taskExtraScoreGranted" to "Доп. баллы",
    "maxScoreChanged" to "Макс. балл изменён",
    "exerciseMaxScoreChanged" to "Макс. балл изменён",
    "exerciseEstimated" to "Задание выдано",
    "exerciseDeadlineChanged" to "Дедлайн изменён",
    "assistantAssigned" to "Назначен проверяющий",
    "reviewerAssigned" to "Назначен проверяющий",
    "taskProlonged" to "Дедлайн изменён",
    "solutionAttached" to "Файлы прикреплены",
    "taskLateDaysReset" to "Late days сброшены",
    "taskLateDaysCancelled" to "Late days возвращены",
    "taskLateDaysProlong" to "Late days списаны",
)

private fun eventTypeLabel(type: String): String =
    eventTypeLabels.getOrElse(type) { type }

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
