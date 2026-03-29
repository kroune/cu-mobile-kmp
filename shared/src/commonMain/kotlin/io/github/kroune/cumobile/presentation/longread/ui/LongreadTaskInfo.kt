package io.github.kroune.cumobile.presentation.longread.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import io.github.kroune.cumobile.presentation.common.ui.AppColorScheme
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.StatusBadge
import io.github.kroune.cumobile.presentation.common.ui.taskStateColor
import io.github.kroune.cumobile.presentation.common.ui.taskStateLabel
import io.github.kroune.cumobile.presentation.longread.LongreadComponent
import io.github.kroune.cumobile.presentation.longread.htmlrender.parseHtmlToBlocks
import io.github.kroune.cumobile.presentation.longread.htmlrender.ui.HtmlContent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Comments tab: displays comment list and input field with file attachments.
 */
@Composable
internal fun CommentsTab(
    comments: ImmutableList<TaskComment>,
    commentText: String,
    isSubmitting: Boolean,
    pendingAttachments: ImmutableList<PendingAttachment>,
    editingCommentId: String?,
    editCommentText: String,
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
                onIntent(LongreadComponent.Intent.Comment.UpdateCommentText(text))
            },
            label = { Text("Комментарий") },
            maxLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = { onIntent(LongreadComponent.Intent.Comment.CreateComment) },
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
                onIntent(LongreadComponent.Intent.Attachment.RemoveCommentAttachment(index))
            },
        )

        Button(
            onClick = { onIntent(LongreadComponent.Intent.Comment.CreateComment) },
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
                CommentCard(
                    comment = comment,
                    isEditing = editingCommentId == comment.id,
                    editText = if (editingCommentId == comment.id) editCommentText else "",
                    isSubmitting = isSubmitting,
                    onIntent = onIntent,
                )
            }
        }
    }
}

private const val ActionIconSize = 18

/** Single comment card with optional edit/delete actions. */
@Composable
private fun CommentCard(
    comment: TaskComment,
    isEditing: Boolean,
    editText: String,
    isSubmitting: Boolean,
    onIntent: (LongreadComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteCommentDialog(
            onConfirm = {
                showDeleteDialog = false
                onIntent(LongreadComponent.Intent.Comment.DeleteComment(comment.id))
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

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
            verticalAlignment = Alignment.CenterVertically,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                comment.createdAt?.let { date ->
                    Text(
                        text = formatDateTime(date),
                        color = AppTheme.colors.textSecondary,
                        fontSize = 11.sp,
                    )
                }
                if (comment.isEditable && !isEditing) {
                    IconButton(
                        onClick = {
                            onIntent(
                                LongreadComponent.Intent.Comment.StartEditComment(
                                    comment.id,
                                    comment.content,
                                ),
                            )
                        },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Редактировать",
                            tint = AppTheme.colors.textSecondary,
                            modifier = Modifier.size(ActionIconSize.dp),
                        )
                    }
                }
                if (comment.isDeletable) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Удалить",
                            tint = AppTheme.colors.taskFailed,
                            modifier = Modifier.size(ActionIconSize.dp),
                        )
                    }
                }
            }
        }

        if (isEditing) {
            EditCommentForm(
                editText = editText,
                isSubmitting = isSubmitting,
                onIntent = onIntent,
            )
        } else {
            CommentContent(comment)
        }
    }
}

/** Read-only comment body: HTML content + attachments. */
@Composable
private fun CommentContent(
    comment: TaskComment,
) {
    val blocks = remember(comment.content) {
        if (comment.content.isBlank()) persistentListOf() else parseHtmlToBlocks(comment.content)
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
    comment.attachments.forEach { attachment ->
        Text(
            text = "\uD83D\uDCCE ${attachment.name}",
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
    }
}

/** Inline edit form shown when editing a comment. */
@Composable
private fun EditCommentForm(
    editText: String,
    isSubmitting: Boolean,
    onIntent: (LongreadComponent.Intent) -> Unit,
) {
    OutlinedTextField(
        value = editText,
        onValueChange = { onIntent(LongreadComponent.Intent.Comment.UpdateEditCommentText(it)) },
        maxLines = 5,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { onIntent(LongreadComponent.Intent.Comment.SaveEditComment) },
        ),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppTheme.colors.textPrimary,
            unfocusedTextColor = AppTheme.colors.textPrimary,
            focusedBorderColor = AppTheme.colors.accent,
            unfocusedBorderColor = AppTheme.colors.textSecondary,
            cursorColor = AppTheme.colors.accent,
        ),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {
        TextButton(onClick = { onIntent(LongreadComponent.Intent.Comment.CancelEditComment) }) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = null,
                modifier = Modifier.size(ActionIconSize.dp),
            )
            Text("Отмена", color = AppTheme.colors.textSecondary)
        }
        Button(
            onClick = { onIntent(LongreadComponent.Intent.Comment.SaveEditComment) },
            enabled = !isSubmitting && editText.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accent),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Сохранить", color = AppTheme.colors.background)
        }
    }
}

/** Confirmation dialog for deleting a comment. */
@Composable
private fun DeleteCommentDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить комментарий?") },
        text = { Text("Это действие нельзя отменить.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Удалить", color = AppTheme.colors.taskFailed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
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

private val eventTypeLabels = mapOf(
    "taskStarted" to "Начато",
    "taskSubmitted" to "Отправлено",
    "taskEvaluated" to "Оценено",
    "taskRejected" to "Отклонено",
    "taskFailed" to "Не сдано",
    "taskReset" to "Сброшено",
    "taskExtraScoreGranted" to "Доп. баллы",
    "maxScoreChanged" to "Макс. балл изменён",
    "exerciseMaxScoreChanged" to "Макс. балл изменён",
    "exerciseEstimated" to "Оценка задания",
    "exerciseDeadlineChanged" to "Дедлайн изменён",
    "assistantAssigned" to "Назначен ассистент",
    "reviewerAssigned" to "Назначен ревьюер",
    "taskProlonged" to "Продлено",
    "solutionAttached" to "Решение прикреплено",
    "taskLateDaysReset" to "Late days сброшены",
    "taskLateDaysCancelled" to "Late days -",
    "taskLateDaysProlong" to "Late days +",
)

private fun eventTypeLabel(type: String): String =
    eventTypeLabels.getOrElse(type) { type }

private val eventTypeColorAccessors: Map<String, (AppColorScheme) -> Color> = mapOf(
    "taskStarted" to { it.taskInProgress },
    "taskSubmitted" to { it.taskHasSolution },
    "solutionAttached" to { it.taskHasSolution },
    "taskEvaluated" to { it.taskEvaluated },
    "taskExtraScoreGranted" to { it.taskEvaluated },
    "taskRejected" to { it.taskRevision },
    "taskFailed" to { it.taskFailed },
    "reviewerAssigned" to { it.accent },
    "assistantAssigned" to { it.accent },
)

@Composable
private fun eventTypeColor(type: String): Color =
    eventTypeColorAccessors[type]?.invoke(AppTheme.colors) ?: AppTheme.colors.textSecondary
