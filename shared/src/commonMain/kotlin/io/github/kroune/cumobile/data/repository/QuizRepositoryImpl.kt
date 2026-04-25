package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.model.QuizAnswer
import io.github.kroune.cumobile.data.model.QuizAttempt
import io.github.kroune.cumobile.data.model.QuizQuestion
import io.github.kroune.cumobile.data.model.StartAttemptResponse
import io.github.kroune.cumobile.data.network.QuizApiService
import io.github.kroune.cumobile.domain.repository.QuizRepository
import io.github.kroune.cumobile.util.AppDispatchers

internal class QuizRepositoryImpl(
    authLocal: Lazy<AuthLocalDataSource>,
    private val quizApi: Lazy<QuizApiService>,
    dispatchers: Lazy<AppDispatchers>,
) : CookieAwareRepository(authLocal, dispatchers),
    QuizRepository {
    override suspend fun startAttempt(sessionId: String): StartAttemptResponse? =
        withCookie { quizApi.value.startAttempt(it, sessionId) }

    override suspend fun getAttempt(attemptId: String): QuizAttempt? =
        withCookie { quizApi.value.getAttempt(it, attemptId) }

    override suspend fun completeAttempt(
        attemptId: String,
        sessionId: String,
    ): Boolean =
        withCookieOrFalse { quizApi.value.completeAttempt(it, attemptId, sessionId) }

    override suspend fun getQuestions(quizId: String): List<QuizQuestion>? =
        withCookie { quizApi.value.getQuestions(it, quizId) }

    override suspend fun listAttempts(sessionId: String): List<QuizAttempt>? =
        withCookie { quizApi.value.listAttempts(it, sessionId) }

    override suspend fun submitAnswer(
        taskId: String,
        questionId: String,
        sessionId: String,
        attemptId: String,
        answer: QuizAnswer,
    ): Boolean =
        withCookieOrFalse {
            quizApi.value.submitAnswer(it, taskId, questionId, sessionId, attemptId, answer)
        }
}
