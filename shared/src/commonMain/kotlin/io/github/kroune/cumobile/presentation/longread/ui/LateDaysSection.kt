@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.longread.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.presentation.common.formatDeadline
import io.github.kroune.cumobile.presentation.common.formatDeadlinePlusDays
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.longread.LongreadComponent
import kotlin.math.min

private const val MaxLateDaysPerTask = 7

/** Late days balance and management buttons with stepper dialog. */
@Composable
internal fun LateDaysInfo(
    taskDetails: TaskDetails,
    onIntent: (LongreadComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!taskDetails.isLateDaysEnabled) return

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LateDaysHeader(taskDetails)
        LateDaysActions(
            taskDetails = taskDetails,
            onProlongClick = { showDialog = true },
            onCancelClick = { onIntent(LongreadComponent.Intent.CancelLateDays) },
        )
    }

    if (showDialog) {
        LateDaysDialog(
            taskDetails = taskDetails,
            onConfirm = { days ->
                showDialog = false
                onIntent(LongreadComponent.Intent.ProlongLateDays(days))
            },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun LateDaysHeader(taskDetails: TaskDetails) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Late days",
            color = AppTheme.colors.textPrimary,
            fontSize = 13.sp,
        )
        Text(
            text = "Использовано: ${taskDetails.lateDays ?: 0}" +
                " | Баланс: ${taskDetails.lateDaysBalance ?: 0}",
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun LateDaysActions(
    taskDetails: TaskDetails,
    onProlongClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val used = taskDetails.lateDays ?: 0
    val balance = taskDetails.lateDaysBalance ?: 0
    val maxAdditional = min(MaxLateDaysPerTask - used, balance)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (maxAdditional > 0) {
            Button(
                onClick = onProlongClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.accent,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(text = "Продлить", color = AppTheme.colors.background, fontSize = 12.sp)
            }
        }
        if (used > 0) {
            Button(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.error,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(text = "Отменить", color = AppTheme.colors.textPrimary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun LateDaysDialog(
    taskDetails: TaskDetails,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val used = taskDetails.lateDays ?: 0
    val balance = taskDetails.lateDaysBalance ?: 0
    val maxAdditional = min(MaxLateDaysPerTask - used, balance)
    var selectedDays by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surface,
        title = {
            Text(
                text = "Продлить дедлайн",
                color = AppTheme.colors.textPrimary,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Текущий дедлайн: ${formatDeadline(taskDetails.deadline)}",
                    color = AppTheme.colors.textSecondary,
                    fontSize = 13.sp,
                )

                DayStepper(
                    value = selectedDays,
                    min = 1,
                    max = maxAdditional,
                    onValueChange = { selectedDays = it },
                )

                val newDeadline = formatDeadlinePlusDays(
                    taskDetails.deadline,
                    used + selectedDays,
                )
                if (newDeadline != null) {
                    Text(
                        text = "Новый дедлайн: $newDeadline",
                        color = AppTheme.colors.accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Text(
                    text = "Останется late days: ${balance - selectedDays}",
                    color = AppTheme.colors.textSecondary,
                    fontSize = 12.sp,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedDays) }) {
                Text(text = "Продлить", color = AppTheme.colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Отмена", color = AppTheme.colors.textSecondary)
            }
        },
    )
}

@Composable
private fun DayStepper(
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconButton(
            onClick = { if (value > min) onValueChange(value - 1) },
            enabled = value > min,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = AppTheme.colors.background,
            ),
        ) {
            Text(
                text = "−",
                color = if (value > min) AppTheme.colors.textPrimary else AppTheme.colors.textSecondary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = "$value",
            color = AppTheme.colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        IconButton(
            onClick = { if (value < max) onValueChange(value + 1) },
            enabled = value < max,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = AppTheme.colors.background,
            ),
        ) {
            Text(
                text = "+",
                color = if (value < max) AppTheme.colors.textPrimary else AppTheme.colors.textSecondary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Score display row for evaluated tasks. */
@Composable
internal fun ScoreDisplay(
    taskDetails: TaskDetails,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.taskEvaluated.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Оценка",
            color = AppTheme.colors.textPrimary,
            fontSize = 14.sp,
        )
        Text(
            text = "${taskDetails.score?.toInt() ?: 0} / ${taskDetails.maxScore ?: 0}",
            color = AppTheme.colors.taskEvaluated,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
