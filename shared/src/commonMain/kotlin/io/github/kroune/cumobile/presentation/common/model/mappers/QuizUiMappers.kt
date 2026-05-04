package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.AnswerValue
import io.github.kroune.cumobile.domain.model.PickedFileDomain
import io.github.kroune.cumobile.domain.model.QuestionResult
import io.github.kroune.cumobile.domain.model.QuizAnswer
import io.github.kroune.cumobile.domain.model.QuizAnswerResultDomain
import io.github.kroune.cumobile.domain.model.QuizAttemptDomain
import io.github.kroune.cumobile.domain.model.QuizQuestionDomain
import io.github.kroune.cumobile.domain.model.QuizQuestionType
import io.github.kroune.cumobile.presentation.common.displayScore
import io.github.kroune.cumobile.presentation.common.model.AnswerValueUi
import io.github.kroune.cumobile.presentation.common.model.PickedFileUi
import io.github.kroune.cumobile.presentation.common.model.QuestionResultUi
import io.github.kroune.cumobile.presentation.common.model.QuizAnswerResultUi
import io.github.kroune.cumobile.presentation.common.model.QuizAnswerUi
import io.github.kroune.cumobile.presentation.common.model.QuizAttemptUi
import io.github.kroune.cumobile.presentation.common.model.QuizOptionUi
import io.github.kroune.cumobile.presentation.common.model.QuizQuestionUi
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

fun QuizQuestionDomain.toUi(): QuizQuestionUi =
    QuizQuestionUi(
        id = id,
        typeLabel = type.label(),
        score = score,
        scoreFormatted = score.displayScore(),
        description = description,
        recommendation = recommendation,
        options = options.map { QuizOptionUi(id = it.id, text = it.text) }.toImmutableList(),
    )

fun QuizAttemptDomain.toUi(): QuizAttemptUi =
    QuizAttemptUi(
        id = id,
        answers = answers.map { it.toUi() }.toImmutableList(),
        score = score,
        scoreFormatted = (score ?: 0.0).displayScore(),
        maxScore = maxScore,
        maxScoreFormatted = (maxScore ?: 0.0).displayScore(),
    )

fun QuizAnswerResultDomain.toUi(): QuizAnswerResultUi =
    QuizAnswerResultUi(
        questionId = questionId,
        result = result.toUi(),
        score = score,
        scoreFormatted = (score ?: 0.0).displayScore(),
        recommendation = recommendation,
        answerValue = answerValue?.toUi(),
    )

fun QuestionResult.toUi(): QuestionResultUi =
    when (this) {
        QuestionResult.Unknown -> QuestionResultUi.Unknown
        QuestionResult.Unanswered -> QuestionResultUi.Unanswered
        QuestionResult.Review -> QuestionResultUi.Review
        QuestionResult.Fail -> QuestionResultUi.Fail
        QuestionResult.Success -> QuestionResultUi.Success
        QuestionResult.PartialSuccess -> QuestionResultUi.PartialSuccess
    }

fun AnswerValue.toUi(): AnswerValueUi =
    when (this) {
        is AnswerValue.Text -> AnswerValueUi.Text(content)
        is AnswerValue.Choices -> AnswerValueUi.Choices(optionIds.toImmutableList())
    }

fun QuizAnswer.toUi(): QuizAnswerUi =
    when (this) {
        is QuizAnswer.SingleChoice -> QuizAnswerUi.SingleChoice(optionId)
        is QuizAnswer.MultipleChoice -> QuizAnswerUi.MultipleChoice(optionIds.toImmutableSet())
        is QuizAnswer.StringMatch -> QuizAnswerUi.StringMatch(text)
        is QuizAnswer.NumberMatch -> QuizAnswerUi.NumberMatch(text)
        is QuizAnswer.OpenText -> QuizAnswerUi.OpenText(text)
    }

fun QuizAnswerUi.toDomain(): QuizAnswer =
    when (this) {
        is QuizAnswerUi.SingleChoice -> QuizAnswer.SingleChoice(optionId)
        is QuizAnswerUi.MultipleChoice -> QuizAnswer.MultipleChoice(optionIds)
        is QuizAnswerUi.StringMatch -> QuizAnswer.StringMatch(text)
        is QuizAnswerUi.NumberMatch -> QuizAnswer.NumberMatch(text)
        is QuizAnswerUi.OpenText -> QuizAnswer.OpenText(text)
    }

fun PickedFileDomain.toUi(): PickedFileUi =
    PickedFileUi(
        name = name,
        bytes = bytes,
        contentType = contentType,
        size = size,
    )

fun PickedFileUi.toDomain(): PickedFileDomain =
    PickedFileDomain(
        name = name,
        bytes = bytes,
        contentType = contentType,
        size = size,
    )

private fun QuizQuestionType.label(): String =
    when (this) {
        QuizQuestionType.SingleChoice -> "Один вариант"
        QuizQuestionType.MultipleChoice -> "Несколько вариантов"
        QuizQuestionType.StringMatch -> "Текстовый ответ"
        QuizQuestionType.NumberMatch -> "Числовой ответ"
        QuizQuestionType.OpenText -> "Развёрнутый ответ"
        QuizQuestionType.Unknown -> ""
    }
