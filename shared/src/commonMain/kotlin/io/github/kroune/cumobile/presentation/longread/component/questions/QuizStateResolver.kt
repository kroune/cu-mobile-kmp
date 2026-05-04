package io.github.kroune.cumobile.presentation.longread.component.questions

import com.arkivanov.decompose.value.MutableValue
import io.github.kroune.cumobile.domain.model.QuizAnswer
import io.github.kroune.cumobile.domain.model.QuizQuestionDomain
import io.github.kroune.cumobile.domain.model.TaskDetailsDomain
import io.github.kroune.cumobile.domain.model.TaskStatus
import io.github.kroune.cumobile.domain.repository.QuizRepository
import io.github.kroune.cumobile.presentation.common.model.mappers.toUi
import io.github.kroune.cumobile.presentation.common.model.toStatusStyle
import kotlinx.collections.immutable.toPersistentMap
import kotlin.time.Clock
import kotlin.time.Instant

internal class QuizStateResolver(
    private val state: MutableValue<QuestionsMaterialComponent.State>,
    private val quizRepository: QuizRepository,
    private val quizLifecycle: QuizLifecycleActions,
    private val onStartTimer: (Long) -> Unit,
    private val onCompleteAttempt: () -> Unit,
) {
    suspend fun applyTaskDetailsDomain(details: TaskDetailsDomain) {
        val exerciseId = details.exercise?.id
        val sessionId = details.quizSessionId
        val currentAttemptId = details.currentAttemptId

        state.value = state.value.copy(
            sessionId = sessionId,
            attemptId = currentAttemptId,
            attemptsLimit = details.exercise?.attemptsLimit,
            evaluationStrategy = details.exercise?.evaluationStrategy,
            taskStatusStyle = details.status?.toStatusStyle(),
        )

        val domainQuestions = if (exerciseId != null) {
            quizLifecycle.loadQuestions(exerciseId)
        } else {
            null
        }

        when (details.status) {
            TaskStatus.Backlog -> state.value = state.value.copy(
                phase = QuestionsMaterialComponent.QuizPhase.NotStarted,
            )
            TaskStatus.InProgress ->
                handleInProgressState(details, sessionId, currentAttemptId, domainQuestions)
            TaskStatus.Review, TaskStatus.Evaluated, TaskStatus.Failed ->
                handleCompletedState(details, sessionId)
            else -> state.value = state.value.copy(
                phase = QuestionsMaterialComponent.QuizPhase.Completed,
            )
        }
    }

    private suspend fun handleInProgressState(
        details: TaskDetailsDomain,
        sessionId: String?,
        currentAttemptId: String?,
        domainQuestions: List<QuizQuestionDomain>?,
    ) {
        if (sessionId != null && currentAttemptId != null) {
            resumeInProgressQuiz(details, domainQuestions.orEmpty())
        } else if (sessionId != null) {
            quizLifecycle.loadPastAttempts(sessionId)
            val lastAttemptId = state.value.pastAttempts
                .lastOrNull()
                ?.id
            if (lastAttemptId != null) {
                val attempt = quizRepository.getAttempt(lastAttemptId)
                state.value = state.value.copy(attemptResults = attempt?.toUi())
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
        details: TaskDetailsDomain,
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
            state.value = state.value.copy(attemptResults = attempt?.toUi())
        }
        state.value = state.value.copy(
            phase = QuestionsMaterialComponent.QuizPhase.Completed,
            canStartNewAttempt = false,
        )
    }

    private suspend fun resumeInProgressQuiz(
        details: TaskDetailsDomain,
        domainQuestions: List<QuizQuestionDomain>,
    ) {
        val attemptId = details.currentAttemptId ?: return
        val attempt = quizRepository.getAttempt(attemptId)
        if (attempt != null) {
            val questionsMap = domainQuestions.associateBy { it.id }
            val restoredAnswers = attempt.answers
                .mapNotNull { result ->
                    val question = questionsMap[result.questionId] ?: return@mapNotNull null
                    val answer = QuizAnswer.fromAnswerValue(question.type, result.answerValue)
                        ?: return@mapNotNull null
                    result.questionId to answer.toUi()
                }.toMap()
            state.value = state.value.copy(answers = restoredAnswers.toPersistentMap())
        }

        val timer = details.exercise?.timer
        val attemptStartedAt = details.attemptStartedAt
        if (timer != null && attemptStartedAt != null) {
            val totalSeconds = parseTimerToSeconds(timer)
            val elapsedSeconds = computeElapsedSeconds(attemptStartedAt)
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

private fun computeElapsedSeconds(startedAt: Instant): Long {
    val now = Clock.System.now()
    return (now - startedAt).inWholeSeconds
}
