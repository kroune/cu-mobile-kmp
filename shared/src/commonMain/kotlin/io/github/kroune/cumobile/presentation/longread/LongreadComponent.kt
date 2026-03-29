package io.github.kroune.cumobile.presentation.longread

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.PendingAttachment
import io.github.kroune.cumobile.data.model.PickedFile
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.Flow

/**
 * MVI component for the longread/material viewer screen.
 *
 * Displays materials within a longread (markdown, file, coding, questions).
 * For coding materials, provides task management: start task, submit
 * solution, comments, events timeline, and late days management.
 */
interface LongreadComponent {
    val state: Value<State>
    val effects: Flow<Effect>

    fun onIntent(intent: Intent)

    sealed interface Effect {
        data class ShowError(
            val message: String,
        ) : Effect

        data class ShowSuccess(
            val message: String,
        ) : Effect
    }

    data class State(
        val longreadId: String = "",
        val courseId: String = "",
        val themeId: String = "",
        val materials: ImmutableList<LongreadMaterial> = persistentListOf(),
        val isLoading: Boolean = false,
        val error: String? = null,
        /** Task details keyed by taskId for coding materials. */
        val taskDetails: ImmutableMap<String, TaskDetails> = persistentMapOf(),
        /** Events for the currently selected task. */
        val taskEvents: ImmutableList<TaskEvent> = persistentListOf(),
        /** Comments for the currently selected task. */
        val taskComments: ImmutableList<TaskComment> = persistentListOf(),
        /** Currently selected task ID (for expanded coding material). */
        val activeTaskId: String? = null,
        /** Selected tab within the active task: "solution", "comments", "info". */
        val selectedTaskTab: String = "solution",
        /** Solution URL input for the active task. */
        val solutionUrl: String = "",
        /** Comment text input for the active task. */
        val commentText: String = "",
        /** Whether a task action is in progress. */
        val isSubmitting: Boolean = false,
        /** Pending file attachments for the solution (upload in progress or complete). */
        val pendingSolutionAttachments: ImmutableList<PendingAttachment> = persistentListOf(),
        /** Pending file attachments for a new comment (upload in progress or complete). */
        val pendingCommentAttachments: ImmutableList<PendingAttachment> = persistentListOf(),
        /** ID of the comment currently being edited, or null. */
        val editingCommentId: String? = null,
        /** Text for the comment being edited. */
        val editCommentText: String = "",
        /** Whether the search bar is visible. */
        val isSearchVisible: Boolean = false,
        /** Current search query text. */
        val searchQuery: String = "",
        /** Total number of search matches across all materials. */
        val searchMatchCount: Int = 0,
        /** Index of the currently focused match (0-based). */
        val currentMatchIndex: Int = 0,
    ) {
        /** Title derived from the first material's name or a fallback. */
        val title: String
            get() = materials.firstOrNull()?.contentName
                ?: materials.firstOrNull()?.name
                ?: "Лонгрид"

        /** Coding materials that have a taskId. */
        val codingMaterials: List<LongreadMaterial>
            get() = materials.filter { it.isCoding && it.taskId != null }
    }

    sealed interface Intent {
        /** Navigation and general screen actions. */
        sealed interface Navigation : Intent {
            data object Back : Navigation
            data object Refresh : Navigation
            data object NavigateToFiles : Navigation

            data class SelectTask(
                val taskId: String,
            ) : Navigation

            data class SelectTaskTab(
                val tab: String,
            ) : Navigation

            data class DownloadFile(
                val material: LongreadMaterial,
            ) : Navigation
        }

        /** Task mutation actions (start, submit, late days). */
        sealed interface Task : Intent {
            data object StartTask : Task
            data object SubmitSolution : Task
            data object CancelLateDays : Task

            data class UpdateSolutionUrl(
                val url: String,
            ) : Task

            data class ProlongLateDays(
                val days: Int,
            ) : Task
        }

        /** Comment CRUD actions. */
        sealed interface Comment : Intent {
            data object CreateComment : Comment
            data object SaveEditComment : Comment
            data object CancelEditComment : Comment

            data class UpdateCommentText(
                val text: String,
            ) : Comment

            data class StartEditComment(
                val commentId: String,
                val currentText: String,
            ) : Comment

            data class UpdateEditCommentText(
                val text: String,
            ) : Comment

            data class DeleteComment(
                val commentId: String,
            ) : Comment
        }

        /** File attachment actions for solutions and comments. */
        sealed interface Attachment : Intent {
            data class PickSolutionAttachment(
                val file: PickedFile,
            ) : Attachment

            data class RemoveSolutionAttachment(
                val index: Int,
            ) : Attachment

            data class PickCommentAttachment(
                val file: PickedFile,
            ) : Attachment

            data class RemoveCommentAttachment(
                val index: Int,
            ) : Attachment
        }

        /** Search actions. */
        sealed interface Search : Intent {
            data object ToggleSearch : Search
            data object NextMatch : Search
            data object PreviousMatch : Search

            data class UpdateSearchQuery(
                val query: String,
            ) : Search
        }
    }
}
