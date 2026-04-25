package io.github.kroune.cumobile.presentation.longread.component.questions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.QuizAnswer
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
            callbacks = QuizLifecycleCallbacks(
                onShowError = onShowError,
                onStartTimer = ::startTimer,
            ),
        )
    }

    private val stateResolver by lazy {
        QuizStateResolver(
            state = _state,
            quizRepository = this.quizRepository,
            quizLifecycle = quizLifecycle,
            onStartTimer = ::startTimer,
            onCompleteAttempt = ::handleCompleteAttempt,
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
            stateResolver.applyTaskDetails(details)
        }
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

private const val SecondsPerHour = 3600L
private const val SecondsPerMinute = 60L
private const val TimerPartsCount = 3

internal fun parseTimerToSeconds(timer: String): Long {
    val parts = timer.split(":")
    if (parts.size != TimerPartsCount) {
        logger.warn { "Malformed timer string: '$timer', expected HH:MM:SS format" }
        return 0
    }
    val hours = parts[0].toLongOrNull() ?: 0
    val minutes = parts[1].toLongOrNull() ?: 0
    val seconds = parts[2].toLongOrNull() ?: 0
    return hours * SecondsPerHour + minutes * SecondsPerMinute + seconds
}
