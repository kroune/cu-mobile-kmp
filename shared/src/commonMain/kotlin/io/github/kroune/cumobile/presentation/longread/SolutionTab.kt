@file:Suppress("MaxLineLength")

package io.github.kroune.cumobile.presentation.longread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.AppColors

/** Solution tab: URL input, submit button, existing solution display. */
@Composable
internal fun SolutionTab(
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

/** Returns true if the task state allows submitting a solution. */
internal fun canSubmitSolution(state: String?): Boolean = state in listOf(TaskState.InProgress, TaskState.Revision, TaskState.Rework)
