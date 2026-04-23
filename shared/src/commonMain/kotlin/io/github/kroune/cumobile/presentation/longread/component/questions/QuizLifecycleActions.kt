package io.github.kroune.cumobile.presentation.longread.component.questions

import com.arkivanov.decompose.value.MutableValue
import io.github.kroune.cumobile.domain.repository.QuizRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.github.kroune.cumobile.data.model.TaskState as TS

private val logger = KotlinLogging.logger {}

internal class QuizLifecycleActions(
    private val taskId: String,
    private val state: MutableValue<QuestionsMaterialComponent.State>,
    private val taskRepository: TaskRepository,
    private val quizRepository: QuizRepository,
    private val scope: CoroutineScope,
    private val onShowError: (String) -> Unit,
    private val onStartTimer: (Long) -> Unit,
) {
    fun startTask() {
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val result = taskRepository.startTask(taskId)
            if (result == null) {
                logger.warn { "Failed to start task $taskId" }
                onShowError("Не удалось начать задание")
                state.value = state.value.copy(isSubmitting = false)
                return@launch
            }
            val sessionId = result.quizSessionId
            if (sessionId == null) {
                logger.warn { "startTask returned null quizSessionId for taskId=$taskId" }
                onShowError("Не удалось начать тест")
                state.value = state.value.copy(isSubmitting = false)
                return@launch
            }
            state.value = state.value.copy(sessionId = sessionId)

            val details = taskRepository.fetchTaskDetails(taskId)
            if (details != null) {
                state.value = state.value.copy(taskDetails = ContentState.Success(details))
            }

            startNewAttempt(sessionId)
            state.value = state.value.copy(isSubmitting = false)
        }
    }

    fun startAttempt() {
        val sessionId = state.value.sessionId ?: return
        val limit = state.value.attemptsLimit
        if (limit != null && state.value.pastAttempts.size >= limit) {
            onShowError("Все попытки использованы")
            return
        }
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            startNewAttempt(sessionId)
            state.value = state.value.copy(isSubmitting = false)
        }
    }

    fun completeAttempt(answerDebouncer: QuizAnswerDebouncer) {
        val attemptId = state.value.attemptId ?: return
        val sessionId = state.value.sessionId ?: return
        scope.launch {
            val flushed = answerDebouncer.flushAll()
            if (!flushed) {
                logger.warn { "Some answers failed to save before completing attemptId=$attemptId" }
            }

            val success = quizRepository.completeAttempt(attemptId, sessionId)
            if (!success) {
                logger.warn { "Failed to complete quiz attempt attemptId=$attemptId" }
                onShowError("Не удалось завершить тест")
                state.value = state.value.copy(
                    phase = QuestionsMaterialComponent.QuizPhase.InProgress,
                    isSubmitting = false,
                )
                return@launch
            }

            val updatedDetails = taskRepository.fetchTaskDetails(taskId)
            if (updatedDetails != null) {
                state.value = state.value.copy(
                    taskDetails = ContentState.Success(updatedDetails),
                )
            }

            val attempt = quizRepository.getAttempt(attemptId)
            loadPastAttempts(sessionId)

            val limit = state.value.attemptsLimit
            val used = state.value.pastAttempts.size
            state.value = state.value.copy(
                attemptResults = attempt,
                phase = QuestionsMaterialComponent.QuizPhase.Completed,
                isSubmitting = false,
                canStartNewAttempt = updatedDetails?.state == TS.InProgress &&
                    (limit == null || used < limit),
            )
        }
    }

    suspend fun loadQuestions(exerciseId: String) {
        val questions = quizRepository.getQuestions(exerciseId)
        if (questions != null) {
            state.value = state.value.copy(questions = questions.toPersistentList())
        } else {
            logger.warn { "Failed to load quiz questions for exerciseId=$exerciseId" }
        }
    }

    suspend fun loadPastAttempts(sessionId: String) {
        val attempts = quizRepository.listAttempts(sessionId)
        if (attempts != null) {
            state.value = state.value.copy(pastAttempts = attempts.toPersistentList())
        }
    }

    private suspend fun startNewAttempt(sessionId: String) {
        val attemptResponse = quizRepository.startAttempt(sessionId)
        if (attemptResponse == null) {
            logger.warn { "Failed to start quiz attempt for sessionId=$sessionId" }
            onShowError("Не удалось начать попытку")
            return
        }

        val details = state.value.taskDetails.dataOrNull
        val timer = details?.exercise?.timer
        val totalSeconds = if (timer != null) parseTimerToSeconds(timer) else 0L

        state.value = state.value.copy(
            attemptId = attemptResponse.attemptId,
            answers = persistentMapOf(),
            attemptResults = null,
            timerTotalSeconds = totalSeconds,
            timerRemainingSeconds = totalSeconds,
            phase = QuestionsMaterialComponent.QuizPhase.InProgress,
        )

        if (totalSeconds > 0) {
            onStartTimer(totalSeconds)
        }
    }
}
