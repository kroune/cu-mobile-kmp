package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Response from the start-task API endpoint.
 *
 * Contains the quiz session ID when the started task is a quiz.
 */
@Serializable
data class StartTaskResponse(
    val quizSessionId: String? = null,
)
