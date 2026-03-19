package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Response from the content upload link endpoint.
 */
@Serializable
data class UploadLinkData(
    val shortName: String = "",
    val filename: String = "",
    val objectKey: String = "",
    val version: String = "",
    val url: String = "",
)
