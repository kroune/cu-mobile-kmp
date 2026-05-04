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
import io.github.kroune.cumobile.presentation.common.model.QuestionResultUi
import io.github.kroune.cumobile.presentation.common.model.QuizAnswerResultUi
import io.github.kroune.cumobile.presentation.common.model.QuizAnswerUi
import io.github.kroune.cumobile.presentation.common.model.QuizQuestionUi
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
    question: QuizQuestionUi,
    answer: QuizAnswerUi?,
    isCompleted: Boolean,
    answerResult: QuizAnswerResultUi?,
    onAnswerChanged: (QuizAnswerUi) -> Unit,
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

private fun answerResultColor(answerResult: QuizAnswerResultUi?): Color? =
    when (answerResult?.result) {
        QuestionResultUi.Success -> ColorSuccess
        QuestionResultUi.PartialSuccess -> ColorPartialSuccess
        QuestionResultUi.Fail -> ColorFail
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
    question: QuizQuestionUi,
    answerResult: QuizAnswerResultUi?,
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "$index.",
            color = AppTheme.colors.textSecondary,
            fontSize = 14.sp,
        )
        Spacer(Modifier.width(6.dp))
        val html = question.description.orEmpty()
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
    question: QuizQuestionUi,
    answer: QuizAnswerUi?,
    isCompleted: Boolean,
    onAnswerChanged: (QuizAnswerUi) -> Unit,
) {
    when (question.typeLabel) {
        LabelSingleChoice ->
            SingleChoiceContent(
                question,
                answer as? QuizAnswerUi.SingleChoice,
                isCompleted,
                onAnswerChanged,
            )
        LabelMultipleChoice ->
            MultipleChoiceContent(
                question,
                answer as? QuizAnswerUi.MultipleChoice,
                isCompleted,
                onAnswerChanged,
            )
        LabelStringMatch ->
            StringMatchContent(
                answer as? QuizAnswerUi.StringMatch,
                isCompleted,
                onAnswerChanged,
            )
        LabelNumberMatch ->
            NumberMatchContent(
                answer as? QuizAnswerUi.NumberMatch,
                isCompleted,
                onAnswerChanged,
            )
        LabelOpenText ->
            OpenTextContent(
                answer as? QuizAnswerUi.OpenText,
                isCompleted,
                onAnswerChanged,
            )
        else ->
            Text(
                text = "Неизвестный тип вопроса",
                color = AppTheme.colors.textSecondary,
                fontSize = 13.sp,
            )
    }
}

@Composable
private fun ResultIcon(result: QuizAnswerResultUi) {
    when (result.result) {
        QuestionResultUi.Success -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = ColorSuccess,
            modifier = Modifier.size(20.dp),
        )
        QuestionResultUi.Fail -> Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = ColorFail,
            modifier = Modifier.size(20.dp),
        )
        QuestionResultUi.PartialSuccess -> Icon(
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
    result: QuizAnswerResultUi,
    question: QuizQuestionUi,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Баллы: ${result.scoreFormatted} / ${question.scoreFormatted}",
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

internal const val LabelSingleChoice = "Один вариант"
internal const val LabelMultipleChoice = "Несколько вариантов"
internal const val LabelStringMatch = "Текстовый ответ"
internal const val LabelNumberMatch = "Числовой ответ"
internal const val LabelOpenText = "Развёрнутый ответ"
