package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.GithubReleaseApi
import io.github.kroune.cumobile.domain.model.UpdateInfoDomain

/**
 * Maps a GitHub release API response to the domain update-info model.
 *
 * @param currentVersion The current app version to compare against.
 * @param isNewerVersion A comparator that returns `true` when the latest
 *   tag is strictly newer than [currentVersion].
 * @return [UpdateInfoDomain] when an update is available, `null` otherwise.
 */
fun GithubReleaseApi.toDomain(
    currentVersion: String,
    isNewerVersion: (latest: String, current: String) -> Boolean,
): UpdateInfoDomain? {
    val latestVersion = tagName
        .removePrefix("v")
        .trim()
    if (!isNewerVersion(latestVersion, currentVersion)) return null
    val apkUrl = assets
        .firstOrNull { it.name.endsWith(".apk") }
        ?.browserDownloadUrl
    return UpdateInfoDomain(
        latestVersion = latestVersion,
        releasePageUrl = htmlUrl,
        apkDownloadUrl = apkUrl,
        releaseName = name,
    )
}
