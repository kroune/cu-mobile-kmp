package io.github.kroune.cumobile.presentation.longread

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.common.StatusBadge
import io.github.kroune.cumobile.presentation.common.formatDeadline
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Surface)
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
    val taskState = taskDetails?.state ?: "backlog"

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
                color = AppColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            material.estimation?.let { est ->
                Text(
                    text = "Макс. балл: ${est.maxScore}" +
                        (est.activityName?.let { " \u2022 $it" }.orEmpty()),
                    color = AppColors.TextSecondary,
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
                color = AppColors.TextSecondary,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Deadline row
        Text(
            text = "\u23F0 ${formatDeadline(taskDetails.deadline)}",
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
        )

        if (taskDetails.state == "backlog") {
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
                    onIntent = onIntent,
                )
                "comments" -> CommentsTab(
                    comments = state.taskComments,
                    commentText = state.commentText,
                    isSubmitting = state.isSubmitting,
                    onIntent = onIntent,
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
            containerColor = AppColors.Accent,
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                color = AppColors.Background,
                modifier = Modifier.padding(4.dp),
            )
        } else {
            Text(text = "Начать задание", color = AppColors.Background)
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
            .background(AppColors.Background),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        tabs.forEach { (key, label) ->
            val isSelected = selectedTab == key
            Text(
                text = label,
                color = if (isSelected) AppColors.Accent else AppColors.TextSecondary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clickable { onTabSelected(key) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

/** Solution tab: URL input, submit button, existing solution display. */
@Composable
private fun SolutionTab(
    taskDetails: TaskDetails,
    solutionUrl: String,
    isSubmitting: Boolean,
    onIntent: (LongreadComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Existing solution display
        taskDetails.solutionUrl?.let { url ->
            ExistingSolutionDisplay(url = url)
        }

        // Score display for evaluated tasks
        if (taskDetails.score != null) {
            ScoreDisplay(taskDetails)
        }

        // Late days info
        LateDaysInfo(taskDetails, onIntent)

        // Solution URL input (only for submittable states)
        if (canSubmitSolution(taskDetails.state)) {
            SolutionUrlInput(
                solutionUrl = solutionUrl,
                isSubmitting = isSubmitting,
                onIntent = onIntent,
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
            .background(AppColors.Accent.copy(alpha = 0.1f))
            .padding(12.dp),
    ) {
        Text(
            text = "Текущее решение:",
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
        )
        Text(
            text = url,
            color = AppColors.Accent,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Solution URL text field and submit button. */
@Composable
private fun SolutionUrlInput(
    solutionUrl: String,
    isSubmitting: Boolean,
    onIntent: (LongreadComponent.Intent) -> Unit,
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
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                focusedBorderColor = AppColors.Accent,
                unfocusedBorderColor = AppColors.TextSecondary,
                focusedLabelColor = AppColors.Accent,
                unfocusedLabelColor = AppColors.TextSecondary,
                cursorColor = AppColors.Accent,
            ),
        )

        Button(
            onClick = {
                onIntent(LongreadComponent.Intent.SubmitSolution)
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Accent,
            ),
            shape = RoundedCornerShape(8.dp),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = AppColors.Background,
                    modifier = Modifier.padding(4.dp),
                )
            } else {
                Text(text = "Отправить решение", color = AppColors.Background)
            }
        }
    }
}

/** Score display row for evaluated tasks. */
@Composable
private fun ScoreDisplay(
    taskDetails: TaskDetails,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.TaskEvaluated.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Оценка",
            color = AppColors.TextPrimary,
            fontSize = 14.sp,
        )
        Text(
            text = "${taskDetails.score?.toInt() ?: 0} / ${taskDetails.maxScore ?: 0}",
            color = AppColors.TaskEvaluated,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Late days balance and management buttons. */
@Composable
private fun LateDaysInfo(
    taskDetails: TaskDetails,
    onIntent: (LongreadComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!taskDetails.isLateDaysEnabled) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.Background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Late days",
                color = AppColors.TextPrimary,
                fontSize = 13.sp,
            )
            Text(
                text = "Использовано: ${taskDetails.lateDays ?: 0}" +
                    " | Баланс: ${taskDetails.lateDaysBalance ?: 0}",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val canProlong = (taskDetails.lateDaysBalance ?: 0) > 0
            if (canProlong) {
                Button(
                    onClick = {
                        onIntent(LongreadComponent.Intent.ProlongLateDays)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Accent,
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(text = "+1 день", color = AppColors.Background, fontSize = 12.sp)
                }
            }
            if ((taskDetails.lateDays ?: 0) > 0) {
                Button(
                    onClick = {
                        onIntent(LongreadComponent.Intent.CancelLateDays)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Error,
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(text = "Отменить", color = AppColors.TextPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}

/** Returns true if the task state allows submitting a solution. */
private fun canSubmitSolution(state: String?): Boolean = state in listOf("inProgress", "revision", "rework")
