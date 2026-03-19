package io.github.kroune.cumobile.data.model

/**
 * Tracks the upload state of a file attachment picked by the user.
 *
 * Stored in component state to show upload progress in the UI.
 * The [uploadedAttachment] is populated once upload completes successfully
 * and is later used when submitting solutions or comments.
 */
data class PendingAttachment(
    val name: String,
    val size: Long,
    val status: UploadStatus,
    val uploadedAttachment: MaterialAttachment? = null,
)

/** Upload lifecycle states for a [PendingAttachment]. */
enum class UploadStatus {
    Uploading,
    Uploaded,
    Failed,
}
