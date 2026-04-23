package io.github.kroune.cumobile.presentation.longread.ui.questions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.longread.component.questions.QuestionsMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.questions.QuestionsMaterialComponent.QuizPhase
import io.github.kroune.cumobile.data.model.TaskState as TS

@Composable
fun QuestionsMaterialCard(
    material: LongreadMaterial,
    state: QuestionsMaterialComponent.State,
    onIntent: (QuestionsMaterialComponent.Intent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .clickable { onIntent(QuestionsMaterialComponent.Intent.ToggleExpanded) }
            .padding(16.dp),
    ) {
        CardHeader(material, state)
        AnimatedVisibility(
            visible = state.isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                ExpandedContent(state, onIntent)
            }
        }
    }

    ConfirmDialogs(state, onIntent)
}

@Composable
private fun ConfirmDialogs(
    state: QuestionsMaterialComponent.State,
    onIntent: (QuestionsMaterialComponent.Intent) -> Unit,
) {
    when (val dialog = state.confirmDialog) {
        is QuestionsMaterialComponent.ConfirmDialog.StartQuiz -> AlertDialog(
            onDismissRequest = { onIntent(QuestionsMaterialComponent.Intent.DismissDialog) },
            title = { Text("Начать тест") },
            text = {
                Text("Тест ограничен по времени: ${dialog.timerDuration}. Начать?")
            },
            confirmButton = {
                TextButton(
                    onClick = { onIntent(QuestionsMaterialComponent.Intent.ConfirmDialogAction) },
                ) { Text("Начать") }
            },
            dismissButton = {
                TextButton(
                    onClick = { onIntent(QuestionsMaterialComponent.Intent.DismissDialog) },
                ) { Text("Отмена") }
            },
        )

        is QuestionsMaterialComponent.ConfirmDialog.CompleteWithUnanswered -> AlertDialog(
            onDismissRequest = { onIntent(QuestionsMaterialComponent.Intent.DismissDialog) },
            title = { Text("Завершить тест?") },
            text = {
                Text(
                    "У вас есть неотвеченные вопросы: ${dialog.unansweredCount}",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onIntent(QuestionsMaterialComponent.Intent.ConfirmDialogAction) },
                ) { Text("Завершить") }
            },
            dismissButton = {
                TextButton(
                    onClick = { onIntent(QuestionsMaterialComponent.Intent.DismissDialog) },
                ) { Text("Отмена") }
            },
        )

        null -> {}
    }
}

@Composable
private fun CardHeader(
    material: LongreadMaterial,
    state: QuestionsMaterialComponent.State,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Quiz,
            contentDescription = null,
            tint = AppTheme.colors.accent,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = material.contentName ?: "Тест",
                color = AppTheme.colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            PhaseStatusText(state)
        }
        ScoreBadge(state)
        Icon(
            imageVector = if (state.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = AppTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun PhaseStatusText(state: QuestionsMaterialComponent.State) {
    val (text, color) = when (state.phase) {
        QuizPhase.Loading -> "Загрузка..." to AppTheme.colors.textSecondary
        QuizPhase.NotStarted -> "Не начат" to AppTheme.colors.textSecondary
        QuizPhase.InProgress -> "В процессе" to AppTheme.colors.accent
        QuizPhase.Completing -> "Завершение..." to AppTheme.colors.accent
        QuizPhase.Completed -> {
            when (state.taskState) {
                TS.Review -> "На проверке" to AppTheme.colors.accent
                TS.InProgress -> "Попытка завершена" to AppTheme.colors.accent
                TS.Failed -> "Неудача" to AppTheme.colors.error
                else -> "Завершён" to AppTheme.colors.taskEvaluated
            }
        }
        is QuizPhase.Error -> "Ошибка" to AppTheme.colors.error
    }
    Text(text = text, color = color, fontSize = 12.sp)
}

@Composable
private fun ScoreBadge(state: QuestionsMaterialComponent.State) {
    val details = state.taskDetails.dataOrNull ?: return
    val score = details.score ?: return
    val maxScore = details.maxScore ?: return
    Text(
        text = "${score.displayScore()}/${maxScore.displayScore()}",
        color = AppTheme.colors.textPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(end = 8.dp),
    )
}

@Composable
private fun ExpandedContent(
    state: QuestionsMaterialComponent.State,
    onIntent: (QuestionsMaterialComponent.Intent) -> Unit,
) {
    when (state.phase) {
        QuizPhase.Loading -> LoadingContent()
        QuizPhase.NotStarted -> NotStartedContent(state, onIntent)
        QuizPhase.InProgress -> InProgressContent(state, onIntent)
        QuizPhase.Completing -> CompletingContent()
        QuizPhase.Completed -> CompletedContent(state, onIntent)
        is QuizPhase.Error -> ErrorContent(state.phase.message, onIntent)
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = AppTheme.colors.accent)
    }
}

@Composable
private fun NotStartedContent(
    state: QuestionsMaterialComponent.State,
    onIntent: (QuestionsMaterialComponent.Intent) -> Unit,
) {
    Column {
        val details = state.taskDetails.dataOrNull
        val timer = details?.exercise?.timer
        if (timer != null) {
            Text(
                text = "Ограничение по времени: $timer",
                color = AppTheme.colors.textSecondary,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(8.dp))
        }

        val attemptsLimit = state.attemptsLimit
        if (attemptsLimit != null) {
            Text(
                text = "Количество попыток: $attemptsLimit",
                color = AppTheme.colors.textSecondary,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { onIntent(QuestionsMaterialComponent.Intent.StartTask) },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.accent,
            ),
        ) {
            Box(contentAlignment = Alignment.Center) {
                AnimatedContent(targetState = state.isSubmitting, label = "start_quiz") { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Начать тест")
                    }
                }
            }
        }
    }
}

@Composable
private fun InProgressContent(
    state: QuestionsMaterialComponent.State,
    onIntent: (QuestionsMaterialComponent.Intent) -> Unit,
) {
    Column {
        if (state.timerTotalSeconds > 0) {
            QuizTimerBar(
                remainingSeconds = state.timerRemainingSeconds,
                totalSeconds = state.timerTotalSeconds,
            )
            Spacer(Modifier.height(12.dp))
        }

        state.questions.forEachIndexed { index, question ->
            androidx.compose.runtime.key(question.id) {
                QuestionItem(
                    index = index + 1,
                    question = question,
                    answer = state.answers[question.id],
                    isCompleted = false,
                    answerResult = null,
                    onAnswerChanged = { answer ->
                        onIntent(
                            QuestionsMaterialComponent.Intent.UpdateAnswer(question.id, answer),
                        )
                    },
                )
            }
            if (index < state.questions.lastIndex) {
                Spacer(Modifier.height(16.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onIntent(QuestionsMaterialComponent.Intent.CompleteAttempt) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.accent,
            ),
        ) {
            Text("Завершить тест")
        }
    }
}

@Composable
private fun CompletingContent() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppTheme.colors.accent)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Завершение теста...",
                color = AppTheme.colors.textSecondary,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onIntent: (QuestionsMaterialComponent.Intent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            color = AppTheme.colors.error,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onIntent(QuestionsMaterialComponent.Intent.RetryLoad) },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.accent,
            ),
        ) {
            Text("Повторить")
        }
    }
}
