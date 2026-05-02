package io.github.kroune.cumobile.domain.model

data class PendingAttachmentDomain(
    val name: String,
    val size: Long,
    val status: UploadStatus,
    val uploadedAttachment: MaterialAttachmentDomain? = null,
)

enum class UploadStatus {
    Uploading,
    Uploaded,
    Failed,
}
