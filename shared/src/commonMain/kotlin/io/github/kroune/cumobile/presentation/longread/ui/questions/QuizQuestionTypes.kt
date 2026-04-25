package io.github.kroune.cumobile.presentation.longread.ui.questions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.QuizAnswer
import io.github.kroune.cumobile.data.model.QuizQuestion
import io.github.kroune.cumobile.presentation.common.ui.AppTheme

@Composable
internal fun SingleChoiceContent(
    question: QuizQuestion,
    answer: QuizAnswer.SingleChoice?,
    isCompleted: Boolean,
    onAnswerChanged: (QuizAnswer) -> Unit,
) {
    Column {
        question.options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = !isCompleted) {
                        onAnswerChanged(QuizAnswer.SingleChoice(option.id))
                    }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = answer?.optionId == option.id,
                    onClick = if (isCompleted) {
                        null
                    } else {
                        { onAnswerChanged(QuizAnswer.SingleChoice(option.id)) }
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = AppTheme.colors.accent),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = option.text,
                    color = AppTheme.colors.textPrimary,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
internal fun MultipleChoiceContent(
    question: QuizQuestion,
    answer: QuizAnswer.MultipleChoice?,
    isCompleted: Boolean,
    onAnswerChanged: (QuizAnswer) -> Unit,
) {
    val selectedIds = answer?.optionIds.orEmpty()

    Column {
        question.options.forEach { option ->
            val isChecked = option.id in selectedIds
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = !isCompleted) {
                        val newSet =
                            if (isChecked) selectedIds - option.id else selectedIds + option.id
                        onAnswerChanged(QuizAnswer.MultipleChoice(newSet))
                    }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = if (isCompleted) {
                        null
                    } else {
                        { checked ->
                            val newSet =
                                if (checked) selectedIds + option.id else selectedIds - option.id
                            onAnswerChanged(QuizAnswer.MultipleChoice(newSet))
                        }
                    },
                    colors = CheckboxDefaults.colors(checkedColor = AppTheme.colors.accent),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = option.text,
                    color = AppTheme.colors.textPrimary,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
internal fun StringMatchContent(
    answer: QuizAnswer.StringMatch?,
    isCompleted: Boolean,
    onAnswerChanged: (QuizAnswer) -> Unit,
) {
    OutlinedTextField(
        value = answer?.text.orEmpty(),
        onValueChange = { onAnswerChanged(QuizAnswer.StringMatch(it)) },
        label = { Text("Ответ") },
        readOnly = isCompleted,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
internal fun NumberMatchContent(
    answer: QuizAnswer.NumberMatch?,
    isCompleted: Boolean,
    onAnswerChanged: (QuizAnswer) -> Unit,
) {
    var text by remember(answer?.text) { mutableStateOf(answer?.text.orEmpty()) }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            val filtered = newText.filter { c -> c.isDigit() || c == '.' || c == ',' || c == '-' }
            text = filtered
            if (filtered.isNotEmpty()) {
                onAnswerChanged(QuizAnswer.NumberMatch(filtered))
            }
        },
        label = { Text("Числовой ответ") },
        readOnly = isCompleted,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

@Composable
internal fun OpenTextContent(
    answer: QuizAnswer.OpenText?,
    isCompleted: Boolean,
    onAnswerChanged: (QuizAnswer) -> Unit,
) {
    OutlinedTextField(
        value = answer?.text.orEmpty(),
        onValueChange = { onAnswerChanged(QuizAnswer.OpenText(it)) },
        label = { Text("Развёрнутый ответ") },
        readOnly = isCompleted,
        modifier = Modifier.fillMaxWidth().height(120.dp),
        maxLines = 6,
    )
}
