package io.github.kroune.cumobile.presentation.longread

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.StatusBadge
import io.github.kroune.cumobile.presentation.common.formatDeadline
import io.github.kroune.cumobile.presentation.common.rememberFilePicker
import io.github.kroune.cumobile.presentation.common.taskStateColor
import io.github.kroune.cumobile.presentation.common.taskStateLabel

/**
 * Card for a coding material within the longread.
 *
 * Shows task header with status badge. When expanded (active),
 * displays tabs for Solution, Comments, and Info.
 */
@Composable
internal fun CodingMaterialCard(
    material: LongreadMaterial,
    state: LongreadComponent.State,
    onIntent: (LongreadComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val taskId = material.taskId ?: return
    val taskDetails = state.taskDetails[taskId]
    val isActive = state.activeTaskId == taskId

    val solutionPicker = rememberFilePicker { file ->
        onIntent(LongreadComponent.Intent.PickSolutionAttachment(file))
    }
    val commentPicker = rememberFilePicker { file ->
        onIntent(LongreadComponent.Intent.PickCommentAttachment(file))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskHeader(
            material = material,
            taskDetails = taskDetails,
            isActive = isActive,
            onClick = {
                onIntent(LongreadComponent.Intent.SelectTask(taskId))
            },
        )

        if (isActive && taskDetails != null) {
            TaskManagementSection(
                taskDetails = taskDetails,
                state = state,
                onIntent = onIntent,
                onAttachSolution = { solutionPicker.launch() },
                onAttachComment = { commentPicker.launch() },
            )
        }
    }
}

/** Header row with exercise name, status badge, and expand indicator. */
@Composable
private fun TaskHeader(
    material: LongreadMaterial,
    taskDetails: TaskDetails?,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val taskState = taskDetails?.state ?: TaskState.Backlog

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = material.contentName ?: material.name ?: "Задание",
                color = AppTheme.colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            material.estimation?.let { est ->
                Text(
                    text = "Макс. балл: ${est.maxScore ?: "-"}" +
                        (est.activityName?.let { " \u2022 $it" }.orEmpty()),
                    color = AppTheme.colors.textSecondary,
                    fontSize = 12.sp,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            StatusBadge(
                label = taskStateLabel(taskState),
                color = taskStateColor(taskState),
            )
            Text(
                text = if (isActive) "\u25B2" else "\u25BC",
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/**
 * Task management section with tab selector and tab content.
 *
 * Shows Start button for backlog tasks, otherwise shows
 * Solution/Comments/Info tabs.
 */
@Composable
private fun TaskManagementSection(
    taskDetails: TaskDetails,
    state: LongreadComponent.State,
    onIntent: (LongreadComponent.Intent) -> Unit,
    onAttachSolution: () -> Unit,
    onAttachComment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "\u23F0 ${formatDeadline(taskDetails.deadline)}",
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )

        if (taskDetails.state == TaskState.Backlog) {
            StartTaskButton(
                isSubmitting = state.isSubmitting,
                onClick = {
                    onIntent(LongreadComponent.Intent.StartTask)
                },
            )
        } else {
            TabSelector(
                selectedTab = state.selectedTaskTab,
                onTabSelected = { tab ->
                    onIntent(LongreadComponent.Intent.SelectTaskTab(tab))
                },
            )
            when (state.selectedTaskTab) {
                "solution" -> SolutionTab(
                    taskDetails = taskDetails,
                    solutionUrl = state.solutionUrl,
                    isSubmitting = state.isSubmitting,
                    pendingAttachments = state.pendingSolutionAttachments,
                    onIntent = onIntent,
                    onAttach = onAttachSolution,
                )
                "comments" -> CommentsTab(
                    comments = state.taskComments,
                    commentText = state.commentText,
                    isSubmitting = state.isSubmitting,
                    pendingAttachments = state.pendingCommentAttachments,
                    onIntent = onIntent,
                    onAttach = onAttachComment,
                )
                "info" -> InfoTab(
                    taskDetails = taskDetails,
                    events = state.taskEvents,
                )
            }
        }
    }
}

/** Green button to start a backlog task. */
@Composable
private fun StartTaskButton(
    isSubmitting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = !isSubmitting,
        modifier = modifier.fillMaxWidth(),
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
            Text(text = "Начать задание", color = AppTheme.colors.background)
        }
    }
}

/** Tab selector for Solution / Comments / Info. */
@Composable
private fun TabSelector(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        "solution" to "Решение",
        "comments" to "Комментарии",
        "info" to "Инфо",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.background),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        tabs.forEach { (key, label) ->
            val isSelected = selectedTab == key
            Text(
                text = label,
                color = if (isSelected) AppTheme.colors.accent else AppTheme.colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clickable { onTabSelected(key) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}
