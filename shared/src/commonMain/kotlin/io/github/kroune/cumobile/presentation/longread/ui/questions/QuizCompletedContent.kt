package io.github.kroune.cumobile.presentation.longread.ui.questions

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.common.model.AnswerValueUi
import io.github.kroune.cumobile.presentation.common.model.QuizAnswerUi
import io.github.kroune.cumobile.presentation.common.model.QuizAttemptUi
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.longread.component.questions.QuestionsMaterialComponent
import kotlinx.collections.immutable.toImmutableSet

@Composable
internal fun CompletedContent(
    state: QuestionsMaterialComponent.State,
    onIntent: (QuestionsMaterialComponent.Intent) -> Unit,
) {
    Column {
        val attempt = state.attemptResults
        if (attempt != null) {
            AttemptScoreHeader(attempt, state)
            EvaluationStrategyText(state.evaluationStrategy)
        }

        CompletedQuestionsList(state)
        AttemptsInfo(state)
        RetryButton(state, onIntent)
    }
}

@Composable
private fun AttemptScoreHeader(
    attempt: QuizAttemptUi,
    state: QuestionsMaterialComponent.State,
) {
    val details = state.taskDetails.dataOrNull
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Результат",
            color = AppTheme.colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        val extraScoreText = details?.extraScoreText
        val scoreText = buildString {
            append("${attempt.scoreFormatted} / ${attempt.maxScoreFormatted}")
            if (extraScoreText != null) {
                append(" +$extraScoreText")
            }
        }
        Text(
            text = scoreText,
            color = AppTheme.colors.accent,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
    val skillLevel = details?.scoreSkillLevel
    if (skillLevel != null) {
        Text(
            text = skillLevel,
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun EvaluationStrategyText(strategy: String?) {
    if (strategy == null) return
    val strategyText = when (strategy) {
        "Best" -> "Оценивается по лучшей попытке"
        "Last" -> "Оценивается по последней попытке"
        else -> return
    }
    Text(
        text = strategyText,
        color = AppTheme.colors.textSecondary,
        fontSize = 12.sp,
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun CompletedQuestionsList(state: QuestionsMaterialComponent.State) {
    val answersMap = state.attemptResults
        ?.answers
        ?.associateBy { it.questionId }
        .orEmpty()
    state.questions.forEachIndexed { index, question ->
        key(question.id) {
            val resultForQuestion = answersMap[question.id]
            val answerToShow = resultForQuestion?.answerValue?.let {
                answerValueToQuizAnswer(question.typeLabel, it)
            } ?: state.answers[question.id]
            QuestionItem(
                index = index + 1,
                question = question,
                answer = answerToShow,
                isCompleted = true,
                answerResult = resultForQuestion,
                onAnswerChanged = {},
            )
        }
        if (index < state.questions.lastIndex) {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AttemptsInfo(state: QuestionsMaterialComponent.State) {
    if (state.pastAttempts.size > 1) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Попытки: ${state.pastAttempts.size}" +
                if (state.attemptsLimit != null) " / ${state.attemptsLimit}" else "",
            color = AppTheme.colors.textSecondary,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun RetryButton(
    state: QuestionsMaterialComponent.State,
    onIntent: (QuestionsMaterialComponent.Intent) -> Unit,
) {
    if (!state.canStartNewAttempt) return

    Spacer(Modifier.height(12.dp))
    Button(
        onClick = { onIntent(QuestionsMaterialComponent.Intent.StartAttempt) },
        enabled = !state.isSubmitting,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.accent,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            AnimatedContent(targetState = state.isSubmitting, label = "retry_quiz") { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Повторить попытку")
                }
            }
        }
    }
}

private fun answerValueToQuizAnswer(
    typeLabel: String,
    value: AnswerValueUi,
): QuizAnswerUi? =
    when (typeLabel) {
        LabelSingleChoice ->
            (value as? AnswerValueUi.Text)?.let { QuizAnswerUi.SingleChoice(it.content) }
        LabelMultipleChoice ->
            (value as? AnswerValueUi.Choices)?.let { QuizAnswerUi.MultipleChoice(it.optionIds.toImmutableSet()) }
        LabelStringMatch ->
            (value as? AnswerValueUi.Text)?.let { QuizAnswerUi.StringMatch(it.content) }
        LabelNumberMatch ->
            (value as? AnswerValueUi.Text)?.let { QuizAnswerUi.NumberMatch(it.content) }
        LabelOpenText ->
            (value as? AnswerValueUi.Text)?.let { QuizAnswerUi.OpenText(it.content) }
        else -> null
    }
