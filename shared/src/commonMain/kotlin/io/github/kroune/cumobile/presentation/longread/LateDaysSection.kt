@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.longread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.presentation.common.AppColors

/** Late days balance and management buttons. */
@Composable
internal fun LateDaysInfo(
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
