package io.github.kroune.cumobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class QuizQuestionApi(
    val id: String,
    val type: String = "",
    val score: Double = 0.0,
    val content: QuizQuestionContentApi? = null,
    val recommendation: String? = null,
    val options: List<QuizOptionApi> = emptyList(),
)

@Serializable
data class QuizQuestionContentApi(
    val description: String? = null,
)

@Serializable
data class QuizOptionApi(
    val id: String = "",
    val text: String = "",
)

@Serializable
data class QuizAttemptApi(
    val id: String = "",
    val answers: List<QuizAnswerResultApi> = emptyList(),
    val score: Double? = null,
    val maxScore: Double? = null,
)

@Serializable
data class QuizAnswerResultApi(
    val questionId: String = "",
    val result: String = "Unknown",
    val score: Double? = null,
    val recommendation: String? = null,
    val value: JsonElement? = null,
)

@Serializable
data class StartAttemptResponseApi(
    val attemptId: String = "",
)

@Serializable
enum class EvaluationStrategy {
    @SerialName("Best")
    Best,

    @SerialName("Last")
    Last,
}
