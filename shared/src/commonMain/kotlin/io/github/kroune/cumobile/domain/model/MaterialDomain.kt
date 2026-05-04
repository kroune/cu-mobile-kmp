package io.github.kroune.cumobile.domain.model

data class MaterialAttachmentDomain(
    val name: String,
    val filename: String,
    val mediaType: String,
    val length: Int,
    val version: String,
)

data class UploadLinkDataDomain(
    val shortName: String,
    val filename: String,
    val objectKey: String,
    val version: String,
    val url: String,
)

data class StartTaskResponseDomain(
    val quizSessionId: String?,
)
