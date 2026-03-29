package io.github.kroune.cumobile.presentation.longread.ui.coding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.PendingAttachment
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.presentation.common.formatDateTime
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.longread.component.coding.CodingMaterialComponent
import io.github.kroune.cumobile.presentation.longread.htmlrender.HtmlContent
import io.github.kroune.cumobile.presentation.longread.htmlrender.parseHtmlToBlocks
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Comments tab: displays comment list and input field with file attachments.
 */
@Composable
internal fun CommentsTab(
    state: CodingMaterialComponent.State,
    onIntent: (CodingMaterialComponent.Intent) -> Unit,
    onAttach: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CommentInputSection(
            commentText = state.commentText,
            isSubmitting = state.isSubmitting,
            pendingAttachments = state.pendingCommentAttachments,
            onIntent = onIntent,
            onAttach = onAttach,
        )

        if (state.taskComments.isEmpty()) {
            Text(
                text = "Нет комментариев",
                color = AppTheme.colors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            state.taskComments.forEach { comment ->
                key(comment.id) {
                    CommentCard(
                        comment = comment,
                        isEditing = state.editingCommentId == comment.id,
                        editText = if (state.editingCommentId == comment.id) {
                            state.editCommentText
                        } else {
                            ""
                        },
                        isSubmitting = state.isSubmitting,
                        downloadingAttachment = state.downloadingAttachment,
                        onIntent = onIntent,
                    )
                }
            }
        }
    }
}

/** Comment text field, attach button, pending files, and send button. */
@Composable
private fun CommentInputSection(
    commentText: String,
    isSubmitting: Boolean,
    pendingAttachments: ImmutableList<PendingAttachment>,
    onIntent: (CodingMaterialComponent.Intent) -> Unit,
    onAttach: () -> Unit,
) {
    OutlinedTextField(
        value = commentText,
        onValueChange = { text ->
            onIntent(CodingMaterialComponent.Intent.Comment.UpdateCommentText(text))
        },
        label = { Text("Комментарий") },
        maxLines = 3,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
            onSend = {
                if (!isSubmitting && commentText.isNotBlank() && !hasUploading(pendingAttachments)) {
                    onIntent(CodingMaterialComponent.Intent.Comment.CreateComment)
                }
            },
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
            onIntent(CodingMaterialComponent.Intent.Attachment.RemoveCommentAttachment(index))
        },
    )

    Button(
        onClick = { onIntent(CodingMaterialComponent.Intent.Comment.CreateComment) },
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
}

private const val ActionIconSize = 18

/** Single comment card with optional edit/delete actions. */
@Composable
private fun CommentCard(
    comment: TaskComment,
    isEditing: Boolean,
    editText: String,
    isSubmitting: Boolean,
    downloadingAttachment: String?,
    onIntent: (CodingMaterialComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteCommentDialog(
            onConfirm = {
                showDeleteDialog = false
                onIntent(CodingMaterialComponent.Intent.Comment.DeleteComment(comment.id))
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
        CommentHeader(
            comment = comment,
            isEditing = isEditing,
            onEdit = {
                onIntent(
                    CodingMaterialComponent.Intent.Comment.StartEditComment(
                        comment.id,
                        comment.content,
                    ),
                )
            },
            onDeleteRequest = { showDeleteDialog = true },
        )

        if (isEditing) {
            EditCommentForm(
                editText = editText,
                isSubmitting = isSubmitting,
                onIntent = onIntent,
            )
        } else {
            CommentContent(
                comment = comment,
                downloadingAttachment = downloadingAttachment,
                onIntent = onIntent,
            )
        }
    }
}

/** Comment header: sender name, date, edit/delete buttons. */
@Composable
private fun CommentHeader(
    comment: TaskComment,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
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
                    onClick = onEdit,
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
                    onClick = onDeleteRequest,
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
}

/** Read-only comment body: HTML content + attachments. */
@Composable
private fun CommentContent(
    comment: TaskComment,
    downloadingAttachment: String?,
    onIntent: (CodingMaterialComponent.Intent) -> Unit,
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
        val isDownloading = downloadingAttachment == attachment.filename
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(enabled = !isDownloading) {
                    onIntent(
                        CodingMaterialComponent.Intent.Attachment.DownloadCommentAttachment(
                            attachment,
                        ),
                    )
                }
                .padding(vertical = 2.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = AppTheme.colors.accent,
                    strokeWidth = 1.5.dp,
                )
            }
            Text(
                text = "\uD83D\uDCCE ${attachment.name}",
                color = AppTheme.colors.accent,
                fontSize = 12.sp,
            )
        }
    }
}

/** Inline edit form shown when editing a comment. */
@Composable
private fun EditCommentForm(
    editText: String,
    isSubmitting: Boolean,
    onIntent: (CodingMaterialComponent.Intent) -> Unit,
) {
    OutlinedTextField(
        value = editText,
        onValueChange = {
            onIntent(CodingMaterialComponent.Intent.Comment.UpdateEditCommentText(it))
        },
        maxLines = 5,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                if (!isSubmitting && editText.isNotBlank()) {
                    onIntent(CodingMaterialComponent.Intent.Comment.SaveEditComment)
                }
            },
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
        TextButton(
            onClick = { onIntent(CodingMaterialComponent.Intent.Comment.CancelEditComment) },
        ) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = null,
                modifier = Modifier.size(ActionIconSize.dp),
            )
            Text("Отмена", color = AppTheme.colors.textSecondary)
        }
        Button(
            onClick = { onIntent(CodingMaterialComponent.Intent.Comment.SaveEditComment) },
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
