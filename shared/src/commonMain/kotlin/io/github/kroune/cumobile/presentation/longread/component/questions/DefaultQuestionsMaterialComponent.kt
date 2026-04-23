package io.github.kroune.cumobile.presentation.longread.component.questions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.QuizAnswer
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.domain.repository.QuizRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.componentScope
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.longread.ui.questions.QuestionsMaterialCard
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import io.github.kroune.cumobile.data.model.TaskState as TS

private val logger = KotlinLogging.logger {}

internal class DefaultQuestionsMaterialComponent(
    componentContext: ComponentContext,
    override val material: LongreadMaterial,
    private val taskId: String,
    initiallyExpanded: Boolean = false,
    taskRepository: Lazy<TaskRepository>,
    quizRepository: Lazy<QuizRepository>,
    private val onShowError: (String) -> Unit,
) : QuestionsMaterialComponent,
    ComponentContext by componentContext {
    private val taskRepository by taskRepository
    private val quizRepository by quizRepository

    private val scope = componentScope()
    private val _state = MutableValue(
        QuestionsMaterialComponent.State(isExpanded = initiallyExpanded),
    )
    override val state: Value<QuestionsMaterialComponent.State> = _state

    private val answerDebouncer by lazy {
        QuizAnswerDebouncer(
            taskId = taskId,
            quizRepository = this.quizRepository,
            scope = scope,
            getSessionId = { _state.value.sessionId },
            getAttemptId = { _state.value.attemptId },
            onSaveError = onShowError,
        )
    }

    private val quizLifecycle by lazy {
        QuizLifecycleActions(
            taskId = taskId,
            state = _state,
            taskRepository = this.taskRepository,
            quizRepository = this.quizRepository,
            scope = scope,
            onShowError = onShowError,
            onStartTimer = ::startTimer,
        )
    }

    private var timerJob: Job? = null
    private var loadJob: Job? = null

    init {
        loadInitialData()
    }

    override fun onIntent(intent: QuestionsMaterialComponent.Intent) {
        when (intent) {
            QuestionsMaterialComponent.Intent.ToggleExpanded ->
                _state.value = _state.value.copy(isExpanded = !_state.value.isExpanded)
            QuestionsMaterialComponent.Intent.RetryLoad -> loadInitialData()
            QuestionsMaterialComponent.Intent.StartTask -> handleStartTask()
            QuestionsMaterialComponent.Intent.StartAttempt -> quizLifecycle.startAttempt()
            is QuestionsMaterialComponent.Intent.UpdateAnswer ->
                handleUpdateAnswer(intent.questionId, intent.answer)
            QuestionsMaterialComponent.Intent.CompleteAttempt -> handleCompleteAttemptIntent()
            QuestionsMaterialComponent.Intent.ConfirmDialogAction -> handleDialogConfirm()
            QuestionsMaterialComponent.Intent.DismissDialog ->
                _state.value = _state.value.copy(confirmDialog = null)
        }
    }

    private fun handleStartTask() {
        val details = _state.value.taskDetails.dataOrNull
        val timer = details?.exercise?.timer
        if (timer != null && parseTimerToSeconds(timer) > 0) {
            _state.value = _state.value.copy(
                confirmDialog = QuestionsMaterialComponent.ConfirmDialog.StartQuiz(timer),
            )
        } else {
            quizLifecycle.startTask()
        }
    }

    private fun handleCompleteAttemptIntent() {
        val unanswered = _state.value.questions.count { q ->
            q.id !in _state.value.answers
        }
        if (unanswered > 0) {
            _state.value = _state.value.copy(
                confirmDialog = QuestionsMaterialComponent.ConfirmDialog.CompleteWithUnanswered(
                    unanswered,
                ),
            )
        } else {
            handleCompleteAttempt()
        }
    }

    private fun handleDialogConfirm() {
        when (_state.value.confirmDialog) {
            is QuestionsMaterialComponent.ConfirmDialog.StartQuiz -> {
                _state.value = _state.value.copy(confirmDialog = null)
                quizLifecycle.startTask()
            }
            is QuestionsMaterialComponent.ConfirmDialog.CompleteWithUnanswered -> {
                _state.value = _state.value.copy(confirmDialog = null)
                handleCompleteAttempt()
            }
            null -> {}
        }
    }

    private fun loadInitialData() {
        loadJob?.cancel()
        loadJob = scope.launch {
            _state.value = _state.value.copy(
                phase = QuestionsMaterialComponent.QuizPhase.Loading,
                taskDetails = ContentState.Loading,
            )
            val details = taskRepository.fetchTaskDetails(taskId)
            if (details == null) {
                logger.warn { "Failed to load task details for taskId=$taskId" }
                _state.value = _state.value.copy(
                    taskDetails = ContentState.Error("Не удалось загрузить задание"),
                    phase = QuestionsMaterialComponent.QuizPhase.Error(
                        "Не удалось загрузить задание",
                    ),
                )
                return@launch
            }
            _state.value = _state.value.copy(taskDetails = ContentState.Success(details))
            applyTaskDetailsToState(details)
        }
    }

    private suspend fun applyTaskDetailsToState(details: TaskDetails) {
        val exerciseId = details.exercise?.id
        val sessionId = details.quizSessionId
        val currentAttemptId = details.currentAttemptId
        val settings = details.exercise?.settings

        _state.value = _state.value.copy(
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
            TS.Backlog -> _state.value = _state.value.copy(
                phase = QuestionsMaterialComponent.QuizPhase.NotStarted,
            )
            TS.InProgress -> handleInProgressState(details, sessionId, currentAttemptId)
            TS.Review, TS.Evaluated, TS.Failed ->
                handleCompletedState(details, sessionId)
            else -> _state.value = _state.value.copy(
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
            val lastAttemptId = _state.value.pastAttempts
                .lastOrNull()
                ?.id
            if (lastAttemptId != null) {
                val attempt = quizRepository.getAttempt(lastAttemptId)
                _state.value = _state.value.copy(attemptResults = attempt)
            }
            val limit = _state.value.attemptsLimit
            val used = _state.value.pastAttempts.size
            _state.value = _state.value.copy(
                phase = QuestionsMaterialComponent.QuizPhase.Completed,
                canStartNewAttempt = limit == null || used < limit,
            )
        } else {
            _state.value = _state.value.copy(
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
            ?: _state.value.pastAttempts
                .lastOrNull()
                ?.id
        if (attemptIdToShow != null) {
            val attempt = quizRepository.getAttempt(attemptIdToShow)
            _state.value = _state.value.copy(attemptResults = attempt)
        }
        _state.value = _state.value.copy(
            phase = QuestionsMaterialComponent.QuizPhase.Completed,
            canStartNewAttempt = false,
        )
    }

    private suspend fun resumeInProgressQuiz(details: TaskDetails) {
        val attemptId = details.currentAttemptId ?: return
        val attempt = quizRepository.getAttempt(attemptId)
        if (attempt != null) {
            val questionsMap = _state.value.questions.associateBy { it.id }
            val restoredAnswers = attempt.answers
                .mapNotNull { result ->
                    val question = questionsMap[result.questionId] ?: return@mapNotNull null
                    val value = result.value ?: return@mapNotNull null
                    val answer = QuizAnswer.fromJsonElement(question.type, value)
                        ?: return@mapNotNull null
                    result.questionId to answer
                }.toMap()
            _state.value = _state.value.copy(answers = restoredAnswers.toPersistentMap())
        }

        val timer = details.exercise?.timer
        val attemptStartedAt = details.attemptStartedAt
        if (timer != null && attemptStartedAt != null) {
            val totalSeconds = parseTimerToSeconds(timer)
            val elapsedSeconds = computeElapsedSeconds(attemptStartedAt)
            if (elapsedSeconds == null) {
                _state.value = _state.value.copy(
                    phase = QuestionsMaterialComponent.QuizPhase.Error(
                        "Ошибка при восстановлении таймера",
                    ),
                )
                return
            }
            val remaining = (totalSeconds - elapsedSeconds).coerceAtLeast(0)
            _state.value = _state.value.copy(
                timerTotalSeconds = totalSeconds,
                timerRemainingSeconds = remaining,
            )
            if (remaining <= 0) {
                handleCompleteAttempt()
                return
            }
            startTimer(remaining)
        }

        _state.value = _state.value.copy(
            phase = QuestionsMaterialComponent.QuizPhase.InProgress,
        )
    }

    private fun handleUpdateAnswer(
        questionId: String,
        answer: QuizAnswer,
    ) {
        if (_state.value.phase != QuestionsMaterialComponent.QuizPhase.InProgress) return
        _state.value = _state.value.copy(
            answers = _state.value.answers
                .toPersistentMap()
                .put(questionId, answer),
        )
        answerDebouncer.submit(questionId, answer)
    }

    private fun handleCompleteAttempt() {
        if (_state.value.phase != QuestionsMaterialComponent.QuizPhase.InProgress) return
        _state.value = _state.value.copy(
            phase = QuestionsMaterialComponent.QuizPhase.Completing,
            isSubmitting = true,
        )
        timerJob?.cancel()
        quizLifecycle.completeAttempt(answerDebouncer)
    }

    private fun startTimer(durationSeconds: Long) {
        timerJob?.cancel()
        val endTime = Clock.System.now() + durationSeconds.seconds
        timerJob = scope.launch {
            while (true) {
                val remaining = (endTime - Clock.System.now()).inWholeSeconds
                if (remaining <= 0) break
                _state.value = _state.value.copy(timerRemainingSeconds = remaining)
                delay(TIMER_TICK_MS)
            }
            _state.value = _state.value.copy(timerRemainingSeconds = 0)
            handleCompleteAttempt()
        }
    }

    @Composable
    override fun Render() {
        val componentState by state.subscribeAsState()
        QuestionsMaterialCard(
            material = material,
            state = componentState,
            onIntent = ::onIntent,
        )
    }

    companion object {
        private const val TIMER_TICK_MS = 1000L
    }
}

private const val SECONDS_PER_HOUR = 3600L
private const val SECONDS_PER_MINUTE = 60L
private const val TIMER_PARTS_COUNT = 3

internal fun parseTimerToSeconds(timer: String): Long {
    val parts = timer.split(":")
    if (parts.size != TIMER_PARTS_COUNT) {
        logger.warn { "Malformed timer string: '$timer', expected HH:MM:SS format" }
        return 0
    }
    val hours = parts[0].toLongOrNull() ?: 0
    val minutes = parts[1].toLongOrNull() ?: 0
    val seconds = parts[2].toLongOrNull() ?: 0
    return hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds
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
