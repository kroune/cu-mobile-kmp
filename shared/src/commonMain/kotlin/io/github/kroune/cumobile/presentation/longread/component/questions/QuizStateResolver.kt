package io.github.kroune.cumobile.presentation.longread.component.questions

import com.arkivanov.decompose.value.MutableValue
import io.github.kroune.cumobile.data.model.QuizAnswer
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.domain.repository.QuizRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.toPersistentMap
import kotlin.time.Clock
import kotlin.time.Instant
import io.github.kroune.cumobile.data.model.TaskState as TS

private val logger = KotlinLogging.logger {}

internal class QuizStateResolver(
    private val state: MutableValue<QuestionsMaterialComponent.State>,
    private val quizRepository: QuizRepository,
    private val quizLifecycle: QuizLifecycleActions,
    private val onStartTimer: (Long) -> Unit,
    private val onCompleteAttempt: () -> Unit,
) {
    suspend fun applyTaskDetails(details: TaskDetails) {
        val exerciseId = details.exercise?.id
        val sessionId = details.quizSessionId
        val currentAttemptId = details.currentAttemptId
        val settings = details.exercise?.settings

        state.value = state.value.copy(
            sessionId = sessionId,
            attemptId = currentAttemptId,
            attemptsLimit = settings?.attemptsLimit,
            evaluationStrategy = settings?.evaluationStrategy,
            taskState = details.state,
        )

        if (exerciseId != null) {
            quizLifecycle.loadQuestions(exerciseId)
        }

        when (details.state) {
            TS.Backlog -> state.value = state.value.copy(
                phase = QuestionsMaterialComponent.QuizPhase.NotStarted,
            )
            TS.InProgress -> handleInProgressState(details, sessionId, currentAttemptId)
            TS.Review, TS.Evaluated, TS.Failed ->
                handleCompletedState(details, sessionId)
            else -> state.value = state.value.copy(
                phase = QuestionsMaterialComponent.QuizPhase.Completed,
            )
        }
    }

    private suspend fun handleInProgressState(
        details: TaskDetails,
        sessionId: String?,
        currentAttemptId: String?,
    ) {
        if (sessionId != null && currentAttemptId != null) {
            resumeInProgressQuiz(details)
        } else if (sessionId != null) {
            quizLifecycle.loadPastAttempts(sessionId)
            val lastAttemptId = state.value.pastAttempts
                .lastOrNull()
                ?.id
            if (lastAttemptId != null) {
                val attempt = quizRepository.getAttempt(lastAttemptId)
                state.value = state.value.copy(attemptResults = attempt)
            }
            val limit = state.value.attemptsLimit
            val used = state.value.pastAttempts.size
            state.value = state.value.copy(
                phase = QuestionsMaterialComponent.QuizPhase.Completed,
                canStartNewAttempt = limit == null || used < limit,
            )
        } else {
            state.value = state.value.copy(
                phase = QuestionsMaterialComponent.QuizPhase.NotStarted,
            )
        }
    }

    private suspend fun handleCompletedState(
        details: TaskDetails,
        sessionId: String?,
    ) {
        if (sessionId != null) {
            quizLifecycle.loadPastAttempts(sessionId)
        }
        val attemptIdToShow = details.evaluatedAttemptId
            ?: details.lastAttemptId
            ?: details.currentAttemptId
            ?: state.value.pastAttempts
                .lastOrNull()
                ?.id
        if (attemptIdToShow != null) {
            val attempt = quizRepository.getAttempt(attemptIdToShow)
            state.value = state.value.copy(attemptResults = attempt)
        }
        state.value = state.value.copy(
            phase = QuestionsMaterialComponent.QuizPhase.Completed,
            canStartNewAttempt = false,
        )
    }

    private suspend fun resumeInProgressQuiz(details: TaskDetails) {
        val attemptId = details.currentAttemptId ?: return
        val attempt = quizRepository.getAttempt(attemptId)
        if (attempt != null) {
            val questionsMap = state.value.questions.associateBy { it.id }
            val restoredAnswers = attempt.answers
                .mapNotNull { result ->
                    val question = questionsMap[result.questionId] ?: return@mapNotNull null
                    val value = result.value ?: return@mapNotNull null
                    val answer = QuizAnswer.fromJsonElement(question.type, value)
                        ?: return@mapNotNull null
                    result.questionId to answer
                }.toMap()
            state.value = state.value.copy(answers = restoredAnswers.toPersistentMap())
        }

        val timer = details.exercise?.timer
        val attemptStartedAt = details.attemptStartedAt
        if (timer != null && attemptStartedAt != null) {
            val totalSeconds = parseTimerToSeconds(timer)
            val elapsedSeconds = computeElapsedSeconds(attemptStartedAt)
            if (elapsedSeconds == null) {
                state.value = state.value.copy(
                    phase = QuestionsMaterialComponent.QuizPhase.Error(
                        "Ошибка при восстановлении таймера",
                    ),
                )
                return
            }
            val remaining = (totalSeconds - elapsedSeconds).coerceAtLeast(0)
            state.value = state.value.copy(
                timerTotalSeconds = totalSeconds,
                timerRemainingSeconds = remaining,
            )
            if (remaining <= 0) {
                onCompleteAttempt()
                return
            }
            onStartTimer(remaining)
        }

        state.value = state.value.copy(
            phase = QuestionsMaterialComponent.QuizPhase.InProgress,
        )
    }
}

private fun computeElapsedSeconds(isoDateTime: String): Long? =
    try {
        val instant = Instant.parse(isoDateTime)
        val now = Clock.System.now()
        (now - instant).inWholeSeconds
    } catch (e: Exception) {
        logger.error(e) { "Failed to parse attemptStartedAt: $isoDateTime" }
        null
    }
