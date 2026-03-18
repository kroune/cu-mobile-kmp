package io.github.kroune.cumobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * GitHub release info from the releases API.
 *
 * Only the fields needed for update checking are included.
 */
@Serializable
data class GithubRelease(
    @SerialName("tag_name")
    val tagName: String = "",
    @SerialName("html_url")
    val htmlUrl: String = "",
    val name: String = "",
    val body: String = "",
    val assets: List<GithubAsset> = emptyList(),
)

/**
 * Asset within a GitHub release (APK, IPA download links).
 */
@Serializable
data class GithubAsset(
    val name: String = "",
    @SerialName("browser_download_url")
    val browserDownloadUrl: String = "",
)

/**
 * Processed update information for the UI.
 *
 * @property latestVersion Semantic version string of the latest release.
 * @property releasePageUrl URL to the GitHub releases page.
 * @property apkDownloadUrl Direct APK download URL (if available).
 * @property releaseName Human-readable release name.
 */
data class UpdateInfo(
    val latestVersion: String,
    val releasePageUrl: String,
    val apkDownloadUrl: String? = null,
    val releaseName: String = "",
)
