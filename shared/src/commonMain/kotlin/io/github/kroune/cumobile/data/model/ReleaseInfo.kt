package io.github.kroune.cumobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * GitHub release info from the releases API.
 *
 * Only the fields needed for update checking are included.
 */
@Serializable
data class GithubReleaseApi(
    @SerialName("tag_name")
    val tagName: String = "",
    @SerialName("html_url")
    val htmlUrl: String = "",
    val name: String = "",
    val body: String = "",
    val assets: List<GithubAssetApi> = emptyList(),
)

/**
 * Asset within a GitHub release (APK, IPA download links).
 */
@Serializable
data class GithubAssetApi(
    val name: String = "",
    @SerialName("browser_download_url")
    val browserDownloadUrl: String = "",
)
