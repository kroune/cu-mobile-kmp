package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.QuizAnswer
import io.github.kroune.cumobile.data.model.QuizAttempt
import io.github.kroune.cumobile.data.model.QuizQuestion
import io.github.kroune.cumobile.data.model.StartAttemptResponse

interface QuizRepository {
    suspend fun startAttempt(sessionId: String): StartAttemptResponse?

    suspend fun getAttempt(attemptId: String): QuizAttempt?

    suspend fun completeAttempt(
        attemptId: String,
        sessionId: String,
    ): Boolean

    suspend fun getQuestions(quizId: String): List<QuizQuestion>?

    suspend fun listAttempts(sessionId: String): List<QuizAttempt>?

    suspend fun submitAnswer(
        taskId: String,
        questionId: String,
        sessionId: String,
        attemptId: String,
        answer: QuizAnswer,
    ): Boolean
}
