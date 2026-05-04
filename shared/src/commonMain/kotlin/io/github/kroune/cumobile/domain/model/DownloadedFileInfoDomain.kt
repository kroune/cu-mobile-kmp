package io.github.kroune.cumobile.domain.model

data class DownloadedFileInfoDomain(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
) {
    val extension: String
        get() = name.substringAfterLast('.', "").uppercase()
}
