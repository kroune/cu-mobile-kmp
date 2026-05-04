package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.mappers.toDomain
import io.github.kroune.cumobile.data.model.mappers.toSubmitParams
import io.github.kroune.cumobile.data.network.QuizApiService
import io.github.kroune.cumobile.domain.model.QuizAnswer
import io.github.kroune.cumobile.domain.model.QuizAttemptDomain
import io.github.kroune.cumobile.domain.model.QuizQuestionDomain
import io.github.kroune.cumobile.domain.model.StartAttemptResponseDomain
import io.github.kroune.cumobile.domain.repository.QuizRepository
import io.github.kroune.cumobile.util.AppDispatchers

internal class QuizRepositoryImpl(
    authLocal: Lazy<AuthLocalDataSource>,
    private val quizApi: Lazy<QuizApiService>,
    dispatchers: Lazy<AppDispatchers>,
) : CookieAwareRepository(authLocal, dispatchers),
    QuizRepository {
    override suspend fun startAttempt(sessionId: String): StartAttemptResponseDomain? =
        withCookie { quizApi.value.startAttempt(it, sessionId) }?.toDomain()

    override suspend fun getAttempt(attemptId: String): QuizAttemptDomain? =
        withCookie { quizApi.value.getAttempt(it, attemptId) }?.toDomain()

    override suspend fun completeAttempt(
        attemptId: String,
        sessionId: String,
    ): Boolean =
        withCookieOrFalse { quizApi.value.completeAttempt(it, attemptId, sessionId) }

    override suspend fun getQuestions(quizId: String): List<QuizQuestionDomain>? =
        withCookie { quizApi.value.getQuestions(it, quizId) }?.map { it.toDomain() }

    override suspend fun listAttempts(sessionId: String): List<QuizAttemptDomain>? =
        withCookie { quizApi.value.listAttempts(it, sessionId) }?.map { it.toDomain() }

    override suspend fun submitAnswer(
        taskId: String,
        questionId: String,
        sessionId: String,
        attemptId: String,
        answer: QuizAnswer,
    ): Boolean =
        withCookieOrFalse {
            quizApi.value.submitAnswer(
                cookie = it,
                taskId = taskId,
                params = answer.toSubmitParams(questionId, sessionId, attemptId),
            )
        }
}
