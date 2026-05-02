package io.github.kroune.cumobile.presentation.common.model

data class UpdateInfoUi(
    val latestVersion: String,
    val releasePageUrl: String,
    val apkDownloadUrl: String?,
    val releaseName: String,
)
