package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.QuizAnswerResultApi
import io.github.kroune.cumobile.data.model.QuizAttemptApi
import io.github.kroune.cumobile.data.model.QuizQuestionApi
import io.github.kroune.cumobile.data.model.StartAttemptResponseApi
import io.github.kroune.cumobile.domain.model.AnswerValue
import io.github.kroune.cumobile.domain.model.QuestionResult
import io.github.kroune.cumobile.domain.model.QuizAnswer
import io.github.kroune.cumobile.domain.model.QuizAnswerResultDomain
import io.github.kroune.cumobile.domain.model.QuizAttemptDomain
import io.github.kroune.cumobile.domain.model.QuizOptionDomain
import io.github.kroune.cumobile.domain.model.QuizQuestionDomain
import io.github.kroune.cumobile.domain.model.QuizQuestionType
import io.github.kroune.cumobile.domain.model.StartAttemptResponseDomain
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

private val logger = KotlinLogging.logger {}

fun QuizQuestionApi.toDomain(): QuizQuestionDomain =
    QuizQuestionDomain(
        id = id,
        type = QuizQuestionType.fromApi(type),
        score = score,
        description = content?.description,
        recommendation = recommendation,
        options = options.map { QuizOptionDomain(id = it.id, text = it.text) },
    )

fun QuizAttemptApi.toDomain(): QuizAttemptDomain =
    QuizAttemptDomain(
        id = id,
        answers = answers.map { it.toDomain() },
        score = score,
        maxScore = maxScore,
    )

fun QuizAnswerResultApi.toDomain(): QuizAnswerResultDomain =
    QuizAnswerResultDomain(
        questionId = questionId,
        result = QuestionResult.fromApi(result),
        score = score,
        recommendation = recommendation,
        answerValue = value?.toAnswerValue(),
    )

fun StartAttemptResponseApi.toDomain(): StartAttemptResponseDomain =
    StartAttemptResponseDomain(attemptId)

fun QuizAnswer.toJsonElement(): JsonElement =
    when (this) {
        is QuizAnswer.SingleChoice -> JsonPrimitive(optionId)
        is QuizAnswer.MultipleChoice -> JsonArray(optionIds.map { JsonPrimitive(it) })
        is QuizAnswer.StringMatch -> JsonPrimitive(text)
        is QuizAnswer.NumberMatch -> {
            val num = text.replace(',', '.').toDoubleOrNull()
            if (num != null) JsonPrimitive(num) else JsonPrimitive(text)
        }
        is QuizAnswer.OpenText -> JsonPrimitive(text)
    }

fun QuizAnswer.apiQuestionType(): String =
    when (this) {
        is QuizAnswer.SingleChoice -> "SingleChoice"
        is QuizAnswer.MultipleChoice -> "MultipleChoice"
        is QuizAnswer.StringMatch -> "StringMatch"
        is QuizAnswer.NumberMatch -> "NumberMatch"
        is QuizAnswer.OpenText -> "OpenText"
    }

private fun JsonElement.toAnswerValue(): AnswerValue? =
    try {
        when (this) {
            is JsonArray -> AnswerValue.Choices(
                jsonArray.map { it.jsonPrimitive.content },
            )
            is JsonPrimitive -> {
                val num = doubleOrNull
                val content = if (num != null) {
                    if (num == num.toLong().toDouble()) {
                        num.toLong().toString()
                    } else {
                        num.toString()
                    }
                } else {
                    this.content
                }
                AnswerValue.Text(content)
            }
            else -> null
        }
    } catch (e: IllegalArgumentException) {
        logger.warn(e) { "Failed to convert JsonElement to AnswerValue" }
        null
    }
