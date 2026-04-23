package io.github.kroune.cumobile.presentation.longread.component.questions

import io.github.kroune.cumobile.data.model.QuizAnswer
import io.github.kroune.cumobile.domain.repository.QuizRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

// All public methods must be called from the Main dispatcher (scope is Main-confined).
internal class QuizAnswerDebouncer(
    private val taskId: String,
    private val quizRepository: QuizRepository,
    private val scope: CoroutineScope,
    private val getSessionId: () -> String?,
    private val getAttemptId: () -> String?,
    private val onSaveError: (String) -> Unit,
) {
    private val pendingJobs = mutableMapOf<String, Job>()
    private val pendingValues = mutableMapOf<String, QuizAnswer>()

    fun submit(
        questionId: String,
        answer: QuizAnswer,
    ) {
        pendingValues[questionId] = answer
        pendingJobs[questionId]?.cancel()
        pendingJobs[questionId] = scope.launch {
            delay(DEBOUNCE_MS)
            sendAnswer(questionId, answer)
            pendingValues.remove(questionId)
        }
    }

    suspend fun flushAll(): Boolean {
        pendingJobs.values.forEach { it.cancel() }
        pendingJobs.clear()
        val toSend = pendingValues.toMap()
        pendingValues.clear()
        if (toSend.isEmpty()) return true
        return coroutineScope {
            toSend.map { (questionId, answer) ->
                async { sendAnswer(questionId, answer) }
            }.awaitAll().all { it }
        }
    }

    private suspend fun sendAnswer(
        questionId: String,
        answer: QuizAnswer,
    ): Boolean {
        val sessionId = getSessionId() ?: return false
        val attemptId = getAttemptId() ?: return false
        val success = quizRepository.submitAnswer(
            taskId = taskId,
            questionId = questionId,
            sessionId = sessionId,
            attemptId = attemptId,
            answer = answer,
        )
        if (!success) {
            logger.warn { "Failed to auto-save answer for questionId=$questionId" }
            onSaveError("При сохранении решения произошла ошибка")
        }
        return success
    }

    companion object {
        private const val DEBOUNCE_MS = 500L
    }
}
