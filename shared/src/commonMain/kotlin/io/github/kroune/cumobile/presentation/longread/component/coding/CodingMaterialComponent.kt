package io.github.kroune.cumobile.presentation.longread.component.coding

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.data.model.PendingAttachment
import io.github.kroune.cumobile.data.model.PickedFile
import io.github.kroune.cumobile.data.model.TaskComment
import io.github.kroune.cumobile.data.model.TaskDetails
import io.github.kroune.cumobile.data.model.TaskEvent
import io.github.kroune.cumobile.presentation.common.RenderComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * MVI component for a single coding material within the longread.
 *
 * Owns all task-specific state (expansion, tabs, comments, attachments,
 * task actions) that was previously managed by the parent LongreadComponent.
 */
interface CodingMaterialComponent : RenderComponent {
    val state: Value<State>

    /**
     * Immutable material data fixed at creation time.
     *
     * Intentionally NOT in [State] — it never changes during the component's
     * lifetime and would bloat every `state.copy()` with a large unchanged object.
     */
    val material: LongreadMaterial

    fun onIntent(intent: Intent)

    data class State(
        val isExpanded: Boolean = false,
        val selectedTab: String = "solution",
        val taskDetails: TaskDetails? = null,
        val taskEvents: ImmutableList<TaskEvent> = persistentListOf(),
        val taskComments: ImmutableList<TaskComment> = persistentListOf(),
        val solutionUrl: String = "",
        val commentText: String = "",
        val isSubmitting: Boolean = false,
        val pendingSolutionAttachments: ImmutableList<PendingAttachment> = persistentListOf(),
        val pendingCommentAttachments: ImmutableList<PendingAttachment> = persistentListOf(),
        val editingCommentId: String? = null,
        val editCommentText: String = "",
        val downloadingAttachment: String? = null,
    )

    sealed interface Intent {
        data object ToggleExpanded : Intent

        data class SelectTab(
            val tab: String,
        ) : Intent

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

            data class DownloadCommentAttachment(
                val attachment: MaterialAttachment,
            ) : Attachment
        }
    }
}
