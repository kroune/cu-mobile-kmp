package io.github.kroune.cumobile.presentation.longread.component.questions

import com.arkivanov.decompose.value.MutableValue
import io.github.kroune.cumobile.domain.model.QuizQuestionDomain
import io.github.kroune.cumobile.domain.model.TaskStatus
import io.github.kroune.cumobile.domain.repository.QuizRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.common.model.mappers.toUi
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

internal class QuizLifecycleCallbacks(
    val onShowError: (String) -> Unit,
    val onStartTimer: (Long) -> Unit,
)

internal class QuizLifecycleActions(
    private val taskId: String,
    private val state: MutableValue<QuestionsMaterialComponent.State>,
    private val taskRepository: TaskRepository,
    private val quizRepository: QuizRepository,
    private val scope: CoroutineScope,
    private val callbacks: QuizLifecycleCallbacks,
) {
    fun startTask() {
        scope.launch {
            state.value = state.value.copy(isSubmitting = true)
            val result = taskRepository.startTask(taskId)
            if (result == null) {
                logger.warn { "Failed to start task $taskId" }
                callbacks.onShowError("Не удалось начать задание")
                state.value = state.value.copy(isSubmitting = false)
                return@launch
            }
            val sessionId = result.quizSessionId
            if (sessionId == null) {
                logger.warn { "startTask returned null quizSessionId for taskId=$taskId" }
                callbacks.onShowError("Не удалось начать тест")
                state.value = state.value.copy(isSubmitting = false)
                return@launch
            }
            state.value = state.value.copy(sessionId = sessionId)

            val details = taskRepository.fetchTaskDetails(taskId)
            if (details != null) {
                state.value = state.value.copy(taskDetails = ContentState.Success(details.toUi()))
            }

            startNewAttempt(sessionId)
            state.value = state.value.copy(isSubmitting = false)
        }
    }

    fun startAttempt() {
        val sessionId = state.value.sessionId ?: return
        val limit = state.value.attemptsLimit
        if (limit != null && state.value.pastAttempts.size >= limit) {
            callbacks.onShowError("Все попытки использованы")
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
                callbacks.onShowError("Не удалось завершить тест")
                state.value = state.value.copy(
                    phase = QuestionsMaterialComponent.QuizPhase.InProgress,
                    isSubmitting = false,
                )
                return@launch
            }

            val updatedDetails = taskRepository.fetchTaskDetails(taskId)
            if (updatedDetails != null) {
                state.value = state.value.copy(
                    taskDetails = ContentState.Success(updatedDetails.toUi()),
                )
            }

            val attempt = quizRepository.getAttempt(attemptId)
            loadPastAttempts(sessionId)

            val limit = state.value.attemptsLimit
            val used = state.value.pastAttempts.size
            state.value = state.value.copy(
                attemptResults = attempt?.toUi(),
                phase = QuestionsMaterialComponent.QuizPhase.Completed,
                isSubmitting = false,
                canStartNewAttempt = updatedDetails?.status == TaskStatus.InProgress &&
                    (limit == null || used < limit),
            )
        }
    }

    suspend fun loadQuestions(exerciseId: String): List<QuizQuestionDomain>? {
        val questions = quizRepository.getQuestions(exerciseId)
        if (questions != null) {
            state.value = state.value.copy(
                questions = questions.map { it.toUi() }.toPersistentList(),
            )
        } else {
            logger.warn { "Failed to load quiz questions for exerciseId=$exerciseId" }
        }
        return questions
    }

    suspend fun loadPastAttempts(sessionId: String) {
        val attempts = quizRepository.listAttempts(sessionId)
        if (attempts != null) {
            state.value = state.value.copy(
                pastAttempts = attempts.map { it.toUi() }.toPersistentList(),
            )
        }
    }

    private suspend fun startNewAttempt(sessionId: String) {
        val attemptResponse = quizRepository.startAttempt(sessionId)
        if (attemptResponse == null) {
            logger.warn { "Failed to start quiz attempt for sessionId=$sessionId" }
            callbacks.onShowError("Не удалось начать попытку")
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
            callbacks.onStartTimer(totalSeconds)
        }
    }
}
