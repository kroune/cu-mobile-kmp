package io.github.kroune.cumobile.presentation.common.model

enum class UploadStatusUi {
    Uploading,
    Uploaded,
    Failed,
}

data class PendingAttachmentUi(
    val name: String,
    val size: Long,
    val status: UploadStatusUi,
    val uploadedAttachment: MaterialAttachmentUi? = null,
)
