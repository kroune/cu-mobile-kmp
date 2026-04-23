package io.github.kroune.cumobile.presentation.longread.ui.questions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.QuestionResult
import io.github.kroune.cumobile.data.model.QuizAnswer
import io.github.kroune.cumobile.data.model.QuizAnswerResult
import io.github.kroune.cumobile.data.model.QuizQuestion
import io.github.kroune.cumobile.data.model.QuizQuestionType
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.longread.htmlrender.HtmlContent
import io.github.kroune.cumobile.presentation.longread.htmlrender.parseHtmlToBlocks
import kotlinx.collections.immutable.persistentListOf

private val ColorSuccess = Color(0xFF4CAF50)
private val ColorPartialSuccess = Color(0xFFFFC107)
private val ColorFail = Color(0xFFF44336)

@Composable
fun QuestionItem(
    index: Int,
    question: QuizQuestion,
    answer: QuizAnswer?,
    isCompleted: Boolean,
    answerResult: QuizAnswerResult?,
    onAnswerChanged: (QuizAnswer) -> Unit,
) {
    val resultColor = answerResultColor(answerResult)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(questionBorder(resultColor))
            .background(AppTheme.colors.surface)
            .padding(12.dp),
    ) {
        QuestionHeader(index, question, answerResult)
        Spacer(Modifier.height(8.dp))
        QuestionBody(question, answer, isCompleted, onAnswerChanged)

        if (isCompleted && answerResult != null) {
            ResultFooter(answerResult, question)
        }
    }
}

private fun answerResultColor(answerResult: QuizAnswerResult?): Color? =
    when (answerResult?.result) {
        QuestionResult.Success -> ColorSuccess
        QuestionResult.PartialSuccess -> ColorPartialSuccess
        QuestionResult.Fail -> ColorFail
        else -> null
    }

@Composable
private fun questionBorder(resultColor: Color?): Modifier =
    if (resultColor != null) {
        Modifier.border(1.dp, resultColor, RoundedCornerShape(8.dp))
    } else {
        Modifier.border(
            1.dp,
            AppTheme.colors.textSecondary.copy(alpha = 0.2f),
            RoundedCornerShape(8.dp),
        )
    }

@Composable
private fun QuestionHeader(
    index: Int,
    question: QuizQuestion,
    answerResult: QuizAnswerResult?,
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "$index.",
            color = AppTheme.colors.textSecondary,
            fontSize = 14.sp,
        )
        Spacer(Modifier.width(6.dp))
        val html = question.content?.description.orEmpty()
        val blocks = remember(html) {
            if (html.isBlank()) persistentListOf() else parseHtmlToBlocks(html)
        }
        if (blocks.isEmpty()) {
            Text(
                text = html,
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
            )
        } else {
            HtmlContent(
                blocks = blocks,
                modifier = Modifier.weight(1f),
            )
        }
        if (answerResult != null) {
            ResultIcon(answerResult)
        }
    }
}

@Composable
private fun QuestionBody(
    question: QuizQuestion,
    answer: QuizAnswer?,
    isCompleted: Boolean,
    onAnswerChanged: (QuizAnswer) -> Unit,
) {
    when (question.type) {
        QuizQuestionType.SingleChoice ->
            SingleChoiceContent(
                question,
                answer as? QuizAnswer.SingleChoice,
                isCompleted,
                onAnswerChanged,
            )
        QuizQuestionType.MultipleChoice ->
            MultipleChoiceContent(
                question,
                answer as? QuizAnswer.MultipleChoice,
                isCompleted,
                onAnswerChanged,
            )
        QuizQuestionType.StringMatch ->
            StringMatchContent(
                answer as? QuizAnswer.StringMatch,
                isCompleted,
                onAnswerChanged,
            )
        QuizQuestionType.NumberMatch ->
            NumberMatchContent(
                answer as? QuizAnswer.NumberMatch,
                isCompleted,
                onAnswerChanged,
            )
        QuizQuestionType.OpenText ->
            OpenTextContent(
                answer as? QuizAnswer.OpenText,
                isCompleted,
                onAnswerChanged,
            )
        QuizQuestionType.Unknown ->
            Text(
                text = "Неизвестный тип вопроса",
                color = AppTheme.colors.textSecondary,
                fontSize = 13.sp,
            )
    }
}

@Composable
private fun ResultIcon(result: QuizAnswerResult) {
    when (result.result) {
        QuestionResult.Success -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = ColorSuccess,
            modifier = Modifier.size(20.dp),
        )
        QuestionResult.Fail -> Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = ColorFail,
            modifier = Modifier.size(20.dp),
        )
        QuestionResult.PartialSuccess -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = ColorPartialSuccess,
            modifier = Modifier.size(20.dp),
        )
        else -> {}
    }
}

@Composable
private fun ResultFooter(
    result: QuizAnswerResult,
    question: QuizQuestion,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        val score = result.score ?: 0.0
        Text(
            text = "Баллы: ${score.displayScore()} / ${question.score.displayScore()}",
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
        val recommendation = result.recommendation ?: question.recommendation
        if (!recommendation.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = recommendation,
                color = AppTheme.colors.textSecondary,
                fontSize = 12.sp,
            )
        }
    }
}

internal fun Double.displayScore(): String {
    val long = toLong()
    return if (this == long.toDouble()) long.toString() else toString()
}
