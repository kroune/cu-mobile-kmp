package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.data.model.GithubRelease
import io.github.kroune.cumobile.data.model.UpdateInfo
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private val logger = KotlinLogging.logger {}

/**
 * Checks for app updates via the GitHub Releases API.
 *
 * Compares the current app version against the latest release
 * tag and returns [UpdateInfo] if a newer version is available.
 */
class UpdateChecker(
    httpClient: Lazy<HttpClient>,
) {
    private val httpClient by httpClient

    /**
     * Checks for an available update.
     *
     * @return [UpdateInfo] if a newer version is available, null otherwise.
     */
    suspend fun checkForUpdate(): UpdateInfo? =
        runCatchingCancellable {
            val release: GithubRelease =
                httpClient.get(GithubReleasesUrl).body()
            val latestVersion = release.tagName
                .removePrefix("v")
                .trim()
            if (isNewerVersion(latestVersion, CurrentAppVersion)) {
                val apkUrl = release.assets
                    .firstOrNull { it.name.endsWith(".apk") }
                    ?.browserDownloadUrl
                UpdateInfo(
                    latestVersion = latestVersion,
                    releasePageUrl = release.htmlUrl,
                    apkDownloadUrl = apkUrl,
                    releaseName = release.name,
                )
            } else {
                null
            }
        }.getOrElse { e ->
            logger.error(e) { "Failed to check for updates" }
            null
        }

    companion object {
        /** Current app version. Must be updated alongside libs.versions.toml. */
        const val CurrentAppVersion = "1.0.1"

        private const val GithubReleasesUrl =
            "https://api.github.com/repos/kroune/cu-mobile-kmp/releases/latest"

        /**
         * Compares two semantic version strings (X.Y.Z).
         *
         * @return true if [latest] is strictly newer than [current].
         */
        internal fun isNewerVersion(
            latest: String,
            current: String,
        ): Boolean {
            val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            return false
        }
    }
}
