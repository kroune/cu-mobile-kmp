@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.longread.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.PendingAttachment
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent
import io.github.kroune.cumobile.presentation.common.formatDateTime
import io.github.kroune.cumobile.presentation.common.formatDeadline
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.StatusBadge
import io.github.kroune.cumobile.presentation.common.ui.taskStateColor
import io.github.kroune.cumobile.presentation.common.ui.taskStateLabel
import io.github.kroune.cumobile.presentation.longread.LongreadComponent
import io.github.kroune.cumobile.presentation.longread.htmlrender.parseHtmlToBlocks
import io.github.kroune.cumobile.presentation.longread.htmlrender.ui.HtmlContent

/**
 * Comments tab: displays comment list and input field with file attachments.
 */
@Composable
internal fun CommentsTab(
    comments: List<TaskComment>,
    commentText: String,
    isSubmitting: Boolean,
    pendingAttachments: List<PendingAttachment>,
    onIntent: (LongreadComponent.Intent) -> Unit,
    onAttach: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Comment input
        OutlinedTextField(
            value = commentText,
            onValueChange = { text ->
                onIntent(LongreadComponent.Intent.UpdateCommentText(text))
            },
            label = { Text("Комментарий") },
            maxLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = { onIntent(LongreadComponent.Intent.CreateComment) },
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppTheme.colors.textPrimary,
                unfocusedTextColor = AppTheme.colors.textPrimary,
                focusedBorderColor = AppTheme.colors.accent,
                unfocusedBorderColor = AppTheme.colors.textSecondary,
                focusedLabelColor = AppTheme.colors.accent,
                unfocusedLabelColor = AppTheme.colors.textSecondary,
                cursorColor = AppTheme.colors.accent,
            ),
        )

        AttachButton(onAttach = onAttach, isSubmitting = isSubmitting)

        PendingAttachmentsList(
            attachments = pendingAttachments,
            onRemove = { index ->
                onIntent(LongreadComponent.Intent.RemoveCommentAttachment(index))
            },
        )

        Button(
            onClick = { onIntent(LongreadComponent.Intent.CreateComment) },
            enabled = !isSubmitting &&
                commentText.isNotBlank() &&
                !hasUploading(pendingAttachments),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.accent,
            ),
            shape = RoundedCornerShape(8.dp),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = AppTheme.colors.background,
                    modifier = Modifier.padding(4.dp),
                )
            } else {
                Text(text = "Отправить", color = AppTheme.colors.background)
            }
        }

        // Comment list
        if (comments.isEmpty()) {
            Text(
                text = "Нет комментариев",
                color = AppTheme.colors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            comments.forEach { comment ->
                CommentCard(comment)
            }
        }
    }
}

/** Single comment card. */
@Composable
private fun CommentCard(
    comment: TaskComment,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = comment.sender.name.ifBlank { comment.sender.email },
                color = AppTheme.colors.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            comment.createdAt?.let { date ->
                Text(
                    text = formatDateTime(date),
                    color = AppTheme.colors.textSecondary,
                    fontSize = 11.sp,
                )
            }
        }
        val blocks = remember(comment.content) {
            if (comment.content.isBlank()) emptyList() else parseHtmlToBlocks(comment.content)
        }
        if (blocks.isNotEmpty()) {
            HtmlContent(blocks = blocks)
        } else {
            Text(
                text = comment.content,
                color = AppTheme.colors.textPrimary,
                fontSize = 13.sp,
            )
        }
        // Attachments
        comment.attachments.forEach { attachment ->
            Text(
                text = "\uD83D\uDCCE ${attachment.name}",
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }
    }
}

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
            value = formatDeadline(
                taskDetails.deadline,
            ),
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
    valueColor: androidx.compose.ui.graphics.Color = AppTheme.colors.textPrimary,
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

        // Actor
        event.actorName?.let { name ->
            Text(
                text = name,
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }

        // State change
        event.content.state?.let { state ->
            Text(
                text = "Статус: ${taskStateLabel(state)}",
                color = taskStateColor(state),
                fontSize = 12.sp,
            )
        }

        // Score change
        event.content.score?.let { score ->
            score.value?.let { value ->
                Text(
                    text = "Оценка: ${value.toInt()}",
                    color = AppTheme.colors.taskEvaluated,
                    fontSize = 12.sp,
                )
            }
        }

        // Late days
        event.content.lateDaysValue?.let { days ->
            Text(
                text = "Late days: $days",
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }
    }
}

/** Returns a display label for an event type (API sends camelCase). */
@Suppress("CyclomaticComplexMethod")
private fun eventTypeLabel(type: String): String =
    when (type) {
        "taskStarted" -> "Начато"
        "taskSubmitted" -> "Отправлено"
        "taskEvaluated" -> "Оценено"
        "taskRejected" -> "Отклонено"
        "taskFailed" -> "Не сдано"
        "taskReset" -> "Сброшено"
        "taskExtraScoreGranted" -> "Доп. баллы"
        "maxScoreChanged", "exerciseMaxScoreChanged" -> "Макс. балл изменён"
        "exerciseEstimated" -> "Оценка задания"
        "exerciseDeadlineChanged" -> "Дедлайн изменён"
        "assistantAssigned" -> "Назначен ассистент"
        "reviewerAssigned" -> "Назначен ревьюер"
        "taskProlonged" -> "Продлено"
        "solutionAttached" -> "Решение прикреплено"
        "taskLateDaysReset" -> "Late days сброшены"
        "taskLateDaysCancelled" -> "Late days -"
        "taskLateDaysProlong" -> "Late days +"
        else -> type
    }

/** Returns a color for an event type (API sends camelCase). */
@Composable
private fun eventTypeColor(type: String): androidx.compose.ui.graphics.Color =
    when (type) {
        "taskStarted" -> AppTheme.colors.taskInProgress
        "taskSubmitted", "solutionAttached" -> AppTheme.colors.taskHasSolution
        "taskEvaluated", "taskExtraScoreGranted" -> AppTheme.colors.taskEvaluated
        "taskRejected" -> AppTheme.colors.taskRevision
        "taskFailed" -> AppTheme.colors.taskFailed
        "reviewerAssigned", "assistantAssigned" -> AppTheme.colors.accent
        else -> AppTheme.colors.textSecondary
    }
