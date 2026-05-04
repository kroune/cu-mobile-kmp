package io.github.kroune.cumobile.presentation.common.model

data class DownloadedFileInfoUi(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val extension: String,
    val sizeLabel: String,
    val dateLabel: String,
)
