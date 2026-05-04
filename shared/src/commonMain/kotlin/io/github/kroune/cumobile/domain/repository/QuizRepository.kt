package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.domain.model.QuizAnswer
import io.github.kroune.cumobile.domain.model.QuizAttemptDomain
import io.github.kroune.cumobile.domain.model.QuizQuestionDomain
import io.github.kroune.cumobile.domain.model.StartAttemptResponseDomain

interface QuizRepository {
    suspend fun startAttempt(sessionId: String): StartAttemptResponseDomain?

    suspend fun getAttempt(attemptId: String): QuizAttemptDomain?

    suspend fun completeAttempt(
        attemptId: String,
        sessionId: String,
    ): Boolean

    suspend fun getQuestions(quizId: String): List<QuizQuestionDomain>?

    suspend fun listAttempts(sessionId: String): List<QuizAttemptDomain>?

    suspend fun submitAnswer(
        taskId: String,
        questionId: String,
        sessionId: String,
        attemptId: String,
        answer: QuizAnswer,
    ): Boolean
}
