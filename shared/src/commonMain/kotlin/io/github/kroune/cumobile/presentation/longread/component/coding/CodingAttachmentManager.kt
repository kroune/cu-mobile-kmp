package io.github.kroune.cumobile.presentation.longread.component.coding

import com.arkivanov.decompose.value.MutableValue
import io.github.kroune.cumobile.domain.model.MaterialAttachmentDomain
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.presentation.common.model.PendingAttachmentUi
import io.github.kroune.cumobile.presentation.common.model.PickedFileUi
import io.github.kroune.cumobile.presentation.common.model.UploadStatusUi
import io.github.kroune.cumobile.presentation.common.model.mappers.toDomain
import io.github.kroune.cumobile.presentation.common.model.mappers.toUi
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger {}

/**
 * Manages file attachment upload and removal for a single coding task.
 *
 * Adapted from the former LongreadAttachmentManager but scoped to one task.
 */
internal class CodingAttachmentManager(
    private val taskId: String,
    private val state: MutableValue<CodingMaterialComponent.State>,
    private val contentRepository: ContentRepository,
    private val scope: CoroutineScope,
) {
    init {
        require(taskId.isNotBlank()) { "taskId must not be blank" }
    }

    fun uploadAttachment(
        file: PickedFileUi,
        isSolution: Boolean,
    ) {
        val pending = PendingAttachmentUi(
            name = file.name,
            size = file.size,
            status = UploadStatusUi.Uploading,
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
        val domainFile = file.toDomain()
        scope.launch {
            val attachment = try {
                contentRepository.uploadFile(
                    directory = directory,
                    filename = domainFile.name,
                    contentType = domainFile.contentType,
                    bytes = domainFile.bytes,
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
        attachment: MaterialAttachmentDomain?,
    ) {
        val list = state.value.pendingSolutionAttachments.toMutableList()
        val idx = list.indexOfFirst {
            it.name == fileName && it.status == UploadStatusUi.Uploading
        }
        if (idx >= 0) {
            list[idx] = list[idx].copy(
                status = if (attachment != null) UploadStatusUi.Uploaded else UploadStatusUi.Failed,
                uploadedAttachment = attachment?.toUi(),
            )
            state.value = state.value.copy(pendingSolutionAttachments = list.toPersistentList())
        }
    }

    private fun updateCommentAttachment(
        fileName: String,
        attachment: MaterialAttachmentDomain?,
    ) {
        val list = state.value.pendingCommentAttachments.toMutableList()
        val idx = list.indexOfFirst {
            it.name == fileName && it.status == UploadStatusUi.Uploading
        }
        if (idx >= 0) {
            list[idx] = list[idx].copy(
                status = if (attachment != null) UploadStatusUi.Uploaded else UploadStatusUi.Failed,
                uploadedAttachment = attachment?.toUi(),
            )
            state.value = state.value.copy(pendingCommentAttachments = list.toPersistentList())
        }
    }
}
