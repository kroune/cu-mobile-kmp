package io.github.kroune.cumobile.presentation.common.model

import kotlinx.collections.immutable.ImmutableList

data class QuizQuestionUi(
    val id: String,
    val typeLabel: String,
    val score: Double,
    val description: String?,
    val recommendation: String?,
    val options: ImmutableList<QuizOptionUi>,
)

data class QuizOptionUi(
    val id: String,
    val text: String,
)

data class QuizAttemptUi(
    val id: String,
    val answers: ImmutableList<QuizAnswerResultUi>,
    val score: Double?,
    val maxScore: Double?,
)

data class QuizAnswerResultUi(
    val questionId: String,
    val result: QuestionResultUi,
    val score: Double?,
    val recommendation: String?,
    val answerValue: AnswerValueUi?,
)

enum class QuestionResultUi {
    Unknown,
    Unanswered,
    Review,
    Fail,
    Success,
    PartialSuccess,
}

sealed interface AnswerValueUi {
    data class Text(
        val content: String,
    ) : AnswerValueUi

    data class Choices(
        val optionIds: ImmutableList<String>,
    ) : AnswerValueUi
}

sealed interface QuizAnswerUi {
    data class SingleChoice(
        val optionId: String,
    ) : QuizAnswerUi

    data class MultipleChoice(
        val optionIds: Set<String>,
    ) : QuizAnswerUi

    data class StringMatch(
        val text: String,
    ) : QuizAnswerUi

    data class NumberMatch(
        val text: String,
    ) : QuizAnswerUi

    data class OpenText(
        val text: String,
    ) : QuizAnswerUi
}
