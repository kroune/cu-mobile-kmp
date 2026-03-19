package io.github.kroune.cumobile.presentation.longread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.PendingAttachment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.data.model.UploadStatus
import io.github.kroune.cumobile.presentation.common.AppTheme

/** Solution tab: URL input, file attachments, submit button, existing solution display. */
@Composable
internal fun SolutionTab(
    taskDetails: TaskDetails,
    solutionUrl: String,
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
        taskDetails.solutionUrl?.let { url ->
            ExistingSolutionDisplay(url = url)
        }

        if (taskDetails.score != null) {
            ScoreDisplay(taskDetails)
        }

        LateDaysInfo(taskDetails, onIntent)

        if (canSubmitSolution(taskDetails.state)) {
            SolutionUrlInput(
                solutionUrl = solutionUrl,
                isSubmitting = isSubmitting,
                pendingAttachments = pendingAttachments,
                onIntent = onIntent,
                onAttach = onAttach,
            )
        }
    }
}

/** Displays the current solution URL. */
@Composable
private fun ExistingSolutionDisplay(
    url: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.accent.copy(alpha = 0.1f))
            .padding(12.dp),
    ) {
        Text(
            text = "Текущее решение:",
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
        Text(
            text = url,
            color = AppTheme.colors.accent,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Solution URL text field, attach button, pending files, and submit button. */
@Composable
private fun SolutionUrlInput(
    solutionUrl: String,
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
        OutlinedTextField(
            value = solutionUrl,
            onValueChange = { url ->
                onIntent(LongreadComponent.Intent.UpdateSolutionUrl(url))
            },
            label = { Text("URL решения") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    onIntent(LongreadComponent.Intent.SubmitSolution)
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
                onIntent(LongreadComponent.Intent.RemoveSolutionAttachment(index))
            },
        )

        Button(
            onClick = {
                onIntent(LongreadComponent.Intent.SubmitSolution)
            },
            enabled = !isSubmitting && !hasUploading(pendingAttachments),
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
                Text(text = "Отправить решение", color = AppTheme.colors.background)
            }
        }
    }
}

/** Returns true if the task state allows submitting a solution. */
internal fun canSubmitSolution(state: String?): Boolean = state in listOf(TaskState.InProgress, TaskState.Revision, TaskState.Rework)

/** "Attach file" button. */
@Composable
internal fun AttachButton(
    onAttach: () -> Unit,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onAttach,
        enabled = !isSubmitting,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppTheme.colors.accent,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.AttachFile,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = "Прикрепить файл",
            modifier = Modifier.padding(start = 4.dp),
            fontSize = 13.sp,
        )
    }
}

/** Displays a list of pending attachments with status indicators and remove buttons. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PendingAttachmentsList(
    attachments: List<PendingAttachment>,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (attachments.isEmpty()) return
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        attachments.forEachIndexed { index, attachment ->
            PendingAttachmentChip(
                attachment = attachment,
                onRemove = { onRemove(index) },
            )
        }
    }
}

/** Single pending attachment chip with status icon and remove button. */
@Composable
private fun PendingAttachmentChip(
    attachment: PendingAttachment,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                when (attachment.status) {
                    UploadStatus.Uploading -> AppTheme.colors.accent.copy(alpha = 0.1f)
                    UploadStatus.Uploaded -> AppTheme.colors.accent.copy(alpha = 0.2f)
                    UploadStatus.Failed -> AppTheme.colors.taskFailed.copy(alpha = 0.2f)
                },
            ).padding(start = 10.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (attachment.status == UploadStatus.Uploading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = AppTheme.colors.accent,
                strokeWidth = 2.dp,
            )
        }
        Text(
            text = attachment.name,
            color = when (attachment.status) {
                UploadStatus.Failed -> AppTheme.colors.taskFailed
                else -> AppTheme.colors.textPrimary
            },
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(20.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Удалить",
                modifier = Modifier.size(14.dp),
                tint = AppTheme.colors.textSecondary,
            )
        }
    }
}

/** Returns true if any attachment is still uploading. */
internal fun hasUploading(attachments: List<PendingAttachment>): Boolean = attachments.any { it.status == UploadStatus.Uploading }
