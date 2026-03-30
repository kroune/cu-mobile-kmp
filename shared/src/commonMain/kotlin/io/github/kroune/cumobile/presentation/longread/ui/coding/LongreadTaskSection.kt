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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import io.github.kroune.cumobile.presentation.common.formatDeadline
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.StatusBadge
import io.github.kroune.cumobile.presentation.common.ui.rememberFilePicker
import io.github.kroune.cumobile.presentation.common.ui.taskStateColor
import io.github.kroune.cumobile.presentation.common.ui.taskStateLabel
import io.github.kroune.cumobile.presentation.longread.component.coding.CodingMaterialComponent

/**
 * Card content for a coding material within the longread.
 *
 * Reads state from [CodingMaterialComponent] and dispatches its intents.
 * Called by [DefaultCodingMaterialComponent.Render].
 */
@Composable
internal fun CodingMaterialCardContent(
    material: LongreadMaterial,
    state: CodingMaterialComponent.State,
    onIntent: (CodingMaterialComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val taskDetails = state.taskDetails

    val solutionPicker = rememberFilePicker { file ->
        onIntent(CodingMaterialComponent.Intent.Attachment.PickSolutionAttachment(file))
    }
    val commentPicker = rememberFilePicker { file ->
        onIntent(CodingMaterialComponent.Intent.Attachment.PickCommentAttachment(file))
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
            isActive = state.isExpanded,
            onClick = {
                onIntent(CodingMaterialComponent.Intent.ToggleExpanded)
            },
        )

        if (state.isExpanded && taskDetails != null) {
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
            Icon(
                imageVector = if (isActive) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (isActive) "Свернуть" else "Развернуть",
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(18.dp).padding(top = 4.dp),
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
    state: CodingMaterialComponent.State,
    onIntent: (CodingMaterialComponent.Intent) -> Unit,
    onAttachSolution: () -> Unit,
    onAttachComment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = formatDeadline(taskDetails.deadline),
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }

        if (taskDetails.state == TaskState.Backlog) {
            StartTaskButton(
                isSubmitting = state.isSubmitting,
                onClick = {
                    onIntent(CodingMaterialComponent.Intent.Task.StartTask)
                },
            )
        } else {
            TabSelector(
                selectedTab = state.selectedTab,
                onTabSelected = { tab ->
                    onIntent(CodingMaterialComponent.Intent.SelectTab(tab))
                },
            )
            when (state.selectedTab) {
                "solution" -> SolutionTab(
                    taskDetails = taskDetails,
                    solutionUrl = state.solutionUrl,
                    isSubmitting = state.isSubmitting,
                    pendingAttachments = state.pendingSolutionAttachments,
                    onIntent = onIntent,
                    onAttach = onAttachSolution,
                )
                "comments" -> CommentsTab(
                    state = state,
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
