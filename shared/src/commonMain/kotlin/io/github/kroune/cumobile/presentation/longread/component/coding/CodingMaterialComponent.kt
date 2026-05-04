package io.github.kroune.cumobile.presentation.longread.component.coding

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.RenderComponent
import io.github.kroune.cumobile.presentation.common.model.LongreadMaterialUi
import io.github.kroune.cumobile.presentation.common.model.MaterialAttachmentUi
import io.github.kroune.cumobile.presentation.common.model.PendingAttachmentUi
import io.github.kroune.cumobile.presentation.common.model.PickedFileUi
import io.github.kroune.cumobile.presentation.common.model.TaskCommentUi
import io.github.kroune.cumobile.presentation.common.model.TaskDetailsUi
import io.github.kroune.cumobile.presentation.common.model.TaskEventUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface CodingMaterialComponent : RenderComponent {
    val state: Value<State>
    val material: LongreadMaterialUi

    fun onIntent(intent: Intent)

    data class State(
        val isExpanded: Boolean = false,
        val selectedTab: String = "solution",
        val taskDetails: ContentState<TaskDetailsUi> = ContentState.Loading,
        val taskEvents: ContentState<ImmutableList<TaskEventUi>> = ContentState.Loading,
        val taskComments: ContentState<ImmutableList<TaskCommentUi>> = ContentState.Loading,
        val solutionUrl: String = "",
        val commentText: String = "",
        val isSubmitting: Boolean = false,
        val pendingSolutionAttachments: ImmutableList<PendingAttachmentUi> = persistentListOf(),
        val pendingCommentAttachments: ImmutableList<PendingAttachmentUi> = persistentListOf(),
        val editingCommentId: String? = null,
        val editCommentText: String = "",
        val downloadingAttachment: String? = null,
        val newDeadlinePreview: String? = null,
    )

    sealed interface Intent {
        data object ToggleExpanded : Intent

        data object RetryLoadDetails : Intent

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

            data class PreviewNewDeadline(
                val selectedDays: Int,
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
                val file: PickedFileUi,
            ) : Attachment

            data class RemoveSolutionAttachment(
                val index: Int,
            ) : Attachment

            data class PickCommentAttachment(
                val file: PickedFileUi,
            ) : Attachment

            data class RemoveCommentAttachment(
                val index: Int,
            ) : Attachment

            data class DownloadCommentAttachment(
                val attachment: MaterialAttachmentUi,
            ) : Attachment
        }
    }
}
