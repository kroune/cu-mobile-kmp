package io.github.kroune.cumobile.domain.model

enum class QuizQuestionType(
    val apiValue: String,
) {
    SingleChoice("SingleChoice"),
    MultipleChoice("MultipleChoice"),
    StringMatch("StringMatch"),
    NumberMatch("NumberMatch"),
    OpenText("OpenText"),
    Unknown(""),
    ;

    companion object {
        fun fromApi(value: String): QuizQuestionType =
            entries.find { it.apiValue == value } ?: Unknown
    }
}

enum class QuestionResult(
    val apiValue: String,
) {
    Unknown("Unknown"),
    Unanswered("Unanswered"),
    Review("Review"),
    Fail("Fail"),
    Success("Success"),
    PartialSuccess("PartialSuccess"),
    ;

    companion object {
        fun fromApi(value: String): QuestionResult =
            entries.find { it.apiValue == value } ?: Unknown
    }
}

data class QuizQuestionDomain(
    val id: String,
    val type: QuizQuestionType,
    val score: Double,
    val description: String?,
    val recommendation: String?,
    val options: List<QuizOptionDomain>,
)

data class QuizOptionDomain(
    val id: String,
    val text: String,
)

data class QuizAttemptDomain(
    val id: String,
    val answers: List<QuizAnswerResultDomain>,
    val score: Double?,
    val maxScore: Double?,
)

sealed interface AnswerValue {
    data class Text(
        val content: String,
    ) : AnswerValue

    data class Choices(
        val optionIds: List<String>,
    ) : AnswerValue
}

data class QuizAnswerResultDomain(
    val questionId: String,
    val result: QuestionResult,
    val score: Double?,
    val recommendation: String?,
    val answerValue: AnswerValue?,
)

data class StartAttemptResponseDomain(
    val attemptId: String,
)

sealed interface QuizAnswer {
    companion object {
        fun fromAnswerValue(
            type: QuizQuestionType,
            value: AnswerValue?,
        ): QuizAnswer? {
            if (value == null) return null
            return when (type) {
                QuizQuestionType.SingleChoice ->
                    (value as? AnswerValue.Text)?.let { SingleChoice(it.content) }
                QuizQuestionType.MultipleChoice ->
                    (value as? AnswerValue.Choices)?.let { MultipleChoice(it.optionIds.toSet()) }
                QuizQuestionType.StringMatch ->
                    (value as? AnswerValue.Text)?.let { StringMatch(it.content) }
                QuizQuestionType.NumberMatch ->
                    (value as? AnswerValue.Text)?.let { NumberMatch(it.content) }
                QuizQuestionType.OpenText ->
                    (value as? AnswerValue.Text)?.let { OpenText(it.content) }
                QuizQuestionType.Unknown -> null
            }
        }
    }

    data class SingleChoice(
        val optionId: String,
    ) : QuizAnswer

    data class MultipleChoice(
        val optionIds: Set<String>,
    ) : QuizAnswer

    data class StringMatch(
        val text: String,
    ) : QuizAnswer

    data class NumberMatch(
        val text: String,
    ) : QuizAnswer

    data class OpenText(
        val text: String,
    ) : QuizAnswer
}
