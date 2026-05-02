package io.github.kroune.cumobile.presentation.longread.component.questions

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.RenderComponent
import io.github.kroune.cumobile.presentation.common.model.LongreadMaterialUi
import io.github.kroune.cumobile.presentation.common.model.QuizAnswerUi
import io.github.kroune.cumobile.presentation.common.model.QuizAttemptUi
import io.github.kroune.cumobile.presentation.common.model.QuizQuestionUi
import io.github.kroune.cumobile.presentation.common.model.StatusStyle
import io.github.kroune.cumobile.presentation.common.model.TaskDetailsUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

interface QuestionsMaterialComponent : RenderComponent {
    val state: Value<State>
    val material: LongreadMaterialUi

    fun onIntent(intent: Intent)

    data class State(
        val isExpanded: Boolean = false,
        val phase: QuizPhase = QuizPhase.Loading,
        val taskDetails: ContentState<TaskDetailsUi> = ContentState.Loading,
        val taskStatusStyle: StatusStyle? = null,
        val questions: ImmutableList<QuizQuestionUi> = persistentListOf(),
        val answers: ImmutableMap<String, QuizAnswerUi> = persistentMapOf(),
        val timerTotalSeconds: Long = 0,
        val timerRemainingSeconds: Long = 0,
        val isSubmitting: Boolean = false,
        val sessionId: String? = null,
        val attemptId: String? = null,
        val attemptResults: QuizAttemptUi? = null,
        val pastAttempts: ImmutableList<QuizAttemptUi> = persistentListOf(),
        val attemptsLimit: Int? = null,
        val evaluationStrategy: String? = null,
        val confirmDialog: ConfirmDialog? = null,
        val canStartNewAttempt: Boolean = false,
    )

    sealed interface QuizPhase {
        data object Loading : QuizPhase

        data object NotStarted : QuizPhase

        data object InProgress : QuizPhase

        data object Completing : QuizPhase

        data object Completed : QuizPhase

        data class Error(
            val message: String,
        ) : QuizPhase
    }

    sealed interface ConfirmDialog {
        data class StartQuiz(
            val timerDuration: String,
        ) : ConfirmDialog

        data class CompleteWithUnanswered(
            val unansweredCount: Int,
        ) : ConfirmDialog
    }

    sealed interface Intent {
        data object ToggleExpanded : Intent

        data object RetryLoad : Intent

        data object StartTask : Intent

        data object StartAttempt : Intent

        data class UpdateAnswer(
            val questionId: String,
            val answer: QuizAnswerUi,
        ) : Intent

        data object CompleteAttempt : Intent

        data object ConfirmDialogAction : Intent

        data object DismissDialog : Intent
    }
}
