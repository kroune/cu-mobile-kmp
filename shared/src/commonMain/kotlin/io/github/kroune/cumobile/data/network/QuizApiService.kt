package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.QuizAnswer
import io.github.kroune.cumobile.data.model.QuizAttempt
import io.github.kroune.cumobile.data.model.QuizQuestion
import io.github.kroune.cumobile.data.model.QuizQuestionType
import io.github.kroune.cumobile.data.model.StartAttemptResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.github.kroune.cumobile.presentation.common.invoke
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

private val logger = KotlinLogging.logger {}

@Serializable
internal data class SessionIdRequest(
    val sessionId: String,
)

@Serializable
internal data class SubmitAnswerRequest(
    val answer: SubmitAnswerBody,
    val attemptId: String,
)

@Serializable
internal data class SubmitAnswerBody(
    val questionId: String,
    val sessionId: String,
    val type: QuizQuestionType,
    val value: JsonElement,
)

internal class QuizApiService(
    private val httpClient: Lazy<HttpClient>,
) {

    suspend fun startAttempt(
        cookie: String,
        sessionId: String,
    ): StartAttemptResponse? =
        safeApiCall(logger, "start quiz attempt for sessionId=$sessionId") {
            httpClient().post(ApiEndpoints.Quizzes.ATTEMPTS) {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
                setBody(SessionIdRequest(sessionId))
            }
        }

    suspend fun getAttempt(
        cookie: String,
        attemptId: String,
    ): QuizAttempt? =
        safeApiCall(logger, "get quiz attempt attemptId=$attemptId") {
            httpClient().get(ApiEndpoints.Quizzes.attemptById(attemptId)) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    suspend fun completeAttempt(
        cookie: String,
        attemptId: String,
        sessionId: String,
    ): Boolean =
        safeApiAction(logger, "complete quiz attempt attemptId=$attemptId") {
            httpClient().post(ApiEndpoints.Quizzes.completeAttempt(attemptId)) {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
                setBody(SessionIdRequest(sessionId))
            }
        }

    suspend fun getQuestions(
        cookie: String,
        quizId: String,
    ): List<QuizQuestion>? =
        safeApiCall(logger, "get quiz questions for quizId=$quizId") {
            httpClient().get(ApiEndpoints.Quizzes.questions(quizId)) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    suspend fun listAttempts(
        cookie: String,
        sessionId: String,
    ): List<QuizAttempt>? =
        safeApiCall(logger, "list quiz attempts for sessionId=$sessionId") {
            httpClient().get(ApiEndpoints.Quizzes.sessionAttempts(sessionId)) {
                header("Cookie", cookieHeader(cookie))
            }
        }

    suspend fun submitAnswer(
        cookie: String,
        taskId: String,
        questionId: String,
        sessionId: String,
        attemptId: String,
        answer: QuizAnswer,
    ): Boolean =
        safeApiAction(logger, "submit quiz answer for taskId=$taskId, questionId=$questionId") {
            httpClient().put(ApiEndpoints.Tasks.submit(taskId)) {
                header("Cookie", cookieHeader(cookie))
                contentType(ContentType.Application.Json)
                setBody(
                    SubmitAnswerRequest(
                        answer = SubmitAnswerBody(
                            questionId = questionId,
                            sessionId = sessionId,
                            type = answer.type,
                            value = answer.toJsonElement(),
                        ),
                        attemptId = attemptId,
                    ),
                )
            }
        }
}
