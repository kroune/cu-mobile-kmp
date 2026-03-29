package io.github.kroune.cumobile.presentation.longread

import com.arkivanov.decompose.value.MutableValue
import io.github.kroune.cumobile.data.model.MaterialAttachment
import io.github.kroune.cumobile.data.model.PendingAttachment
import io.github.kroune.cumobile.data.model.PickedFile
import io.github.kroune.cumobile.data.model.UploadStatus
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger {}

/**
 * Manages file attachment upload and removal for solution
 * and comment submissions within the longread component.
 */
internal class LongreadAttachmentManager(
    private val state: MutableValue<LongreadComponent.State>,
    private val contentRepository: ContentRepository,
    private val scope: CoroutineScope,
) {
    fun uploadAttachment(
        file: PickedFile,
        isSolution: Boolean,
    ) {
        val taskId = state.value.activeTaskId ?: return
        val pending = PendingAttachment(
            name = file.name,
            size = file.size,
            status = UploadStatus.Uploading,
        )
        if (isSolution) {
            state.value = state.value.copy(
                pendingSolutionAttachments =
                    (state.value.pendingSolutionAttachments + pending).toPersistentList(),
            )
        } else {
            state.value = state.value.copy(
                pendingCommentAttachments =
                    (state.value.pendingCommentAttachments + pending).toPersistentList(),
            )
        }
        val directory = if (isSolution) {
            "tasks/$taskId/solutions"
        } else {
            "tasks/$taskId/comments"
        }
        scope.launch {
            val attachment = try {
                contentRepository.uploadFile(
                    directory = directory,
                    filename = file.name,
                    contentType = file.contentType,
                    bytes = file.bytes,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.warn(e) { "Failed to upload attachment: ${file.name} to $directory" }
                null
            }
            if (attachment == null) {
                logger.warn { "Upload returned null for attachment: ${file.name} to $directory" }
            }
            if (isSolution) {
                updateSolutionAttachment(file.name, attachment)
            } else {
                updateCommentAttachment(file.name, attachment)
            }
        }
    }

    fun removeSolutionAttachment(index: Int) {
        val list = state.value.pendingSolutionAttachments.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            state.value = state.value.copy(pendingSolutionAttachments = list.toPersistentList())
        }
    }

    fun removeCommentAttachment(index: Int) {
        val list = state.value.pendingCommentAttachments.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            state.value = state.value.copy(pendingCommentAttachments = list.toPersistentList())
        }
    }

    private fun updateSolutionAttachment(
        fileName: String,
        attachment: MaterialAttachment?,
    ) {
        val list = state.value.pendingSolutionAttachments.toMutableList()
        val idx = list.indexOfFirst {
            it.name == fileName && it.status == UploadStatus.Uploading
        }
        if (idx >= 0) {
            list[idx] = list[idx].copy(
                status = if (attachment != null) UploadStatus.Uploaded else UploadStatus.Failed,
                uploadedAttachment = attachment,
            )
            state.value = state.value.copy(pendingSolutionAttachments = list.toPersistentList())
        }
    }

    private fun updateCommentAttachment(
        fileName: String,
        attachment: MaterialAttachment?,
    ) {
        val list = state.value.pendingCommentAttachments.toMutableList()
        val idx = list.indexOfFirst {
            it.name == fileName && it.status == UploadStatus.Uploading
        }
        if (idx >= 0) {
            list[idx] = list[idx].copy(
                status = if (attachment != null) UploadStatus.Uploaded else UploadStatus.Failed,
                uploadedAttachment = attachment,
            )
            state.value =
                state.value.copy(pendingCommentAttachments = list.toPersistentList())
        }
    }
}
