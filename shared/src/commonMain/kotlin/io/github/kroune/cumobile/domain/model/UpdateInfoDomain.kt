package io.github.kroune.cumobile.domain.model

data class UpdateInfoDomain(
    val latestVersion: String,
    val releasePageUrl: String,
    val apkDownloadUrl: String? = null,
    val releaseName: String = "",
)
