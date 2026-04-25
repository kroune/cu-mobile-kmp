package io.github.kroune.cumobile.data.model

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

private val logger = KotlinLogging.logger {}

@Serializable
data class QuizQuestion(
    val id: String,
    val type: QuizQuestionType = QuizQuestionType.Unknown,
    val score: Double = 0.0,
    val content: QuizQuestionContent? = null,
    val recommendation: String? = null,
    val options: List<QuizOption> = emptyList(),
)

@Serializable
data class QuizQuestionContent(
    val description: String? = null,
)

@Serializable
data class QuizOption(
    val id: String = "",
    val text: String = "",
)

@Serializable
data class QuizAttempt(
    val id: String = "",
    val answers: List<QuizAnswerResult> = emptyList(),
    val score: Double? = null,
    val maxScore: Double? = null,
)

@Serializable
data class QuizAnswerResult(
    val questionId: String = "",
    val result: QuestionResult = QuestionResult.Unknown,
    val score: Double? = null,
    val recommendation: String? = null,
    val value: JsonElement? = null,
)

@Serializable
data class StartAttemptResponse(
    val attemptId: String = "",
)

sealed interface QuizAnswer {
    val type: QuizQuestionType

    fun toJsonElement(): JsonElement

    data class SingleChoice(
        val optionId: String,
    ) : QuizAnswer {
        override val type = QuizQuestionType.SingleChoice

        override fun toJsonElement() =
            JsonPrimitive(optionId)
    }

    data class MultipleChoice(
        val optionIds: Set<String>,
    ) : QuizAnswer {
        override val type = QuizQuestionType.MultipleChoice

        override fun toJsonElement() =
            JsonArray(optionIds.map { JsonPrimitive(it) })
    }

    data class StringMatch(
        val text: String,
    ) : QuizAnswer {
        override val type = QuizQuestionType.StringMatch

        override fun toJsonElement() =
            JsonPrimitive(text)
    }

    data class NumberMatch(
        val text: String,
    ) : QuizAnswer {
        override val type = QuizQuestionType.NumberMatch

        override fun toJsonElement(): JsonElement {
            val num = text.replace(',', '.').toDoubleOrNull()
            return if (num != null) JsonPrimitive(num) else JsonPrimitive(text)
        }
    }

    data class OpenText(
        val text: String,
    ) : QuizAnswer {
        override val type = QuizQuestionType.OpenText

        override fun toJsonElement() =
            JsonPrimitive(text)
    }

    companion object {
        fun fromJsonElement(
            questionType: QuizQuestionType,
            element: JsonElement,
        ): QuizAnswer? =
            try {
                when (questionType) {
                    QuizQuestionType.SingleChoice ->
                        SingleChoice(element.jsonPrimitive.content)
                    QuizQuestionType.MultipleChoice ->
                        MultipleChoice(
                            element.jsonArray.map { it.jsonPrimitive.content }.toSet(),
                        )
                    QuizQuestionType.StringMatch ->
                        StringMatch(element.jsonPrimitive.content)
                    QuizQuestionType.NumberMatch -> {
                        val content = element.jsonPrimitive.doubleOrNull?.let { num ->
                            if (num == num.toLong().toDouble()) {
                                num.toLong().toString()
                            } else {
                                num.toString()
                            }
                        } ?: element.jsonPrimitive.content
                        NumberMatch(content)
                    }
                    QuizQuestionType.OpenText ->
                        OpenText(element.jsonPrimitive.content)
                    QuizQuestionType.Unknown -> null
                }
            } catch (e: IllegalArgumentException) {
                logger.warn(e) { "Failed to parse QuizAnswer for type=$questionType" }
                null
            }
    }
}

@Serializable(with = QuizQuestionTypeSerializer::class)
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

internal object QuizQuestionTypeSerializer : KSerializer<QuizQuestionType> {
    override val descriptor = PrimitiveSerialDescriptor("QuizQuestionType", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: QuizQuestionType,
    ) =
        encoder.encodeString(value.apiValue)

    override fun deserialize(decoder: Decoder): QuizQuestionType =
        QuizQuestionType.fromApi(decoder.decodeString())
}

@Serializable(with = QuestionResultSerializer::class)
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

internal object QuestionResultSerializer : KSerializer<QuestionResult> {
    override val descriptor = PrimitiveSerialDescriptor("QuestionResult", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: QuestionResult,
    ) =
        encoder.encodeString(value.apiValue)

    override fun deserialize(decoder: Decoder): QuestionResult =
        QuestionResult.fromApi(decoder.decodeString())
}

@Serializable
enum class EvaluationStrategy {
    @SerialName("Best")
    Best,

    @SerialName("Last")
    Last,
}
