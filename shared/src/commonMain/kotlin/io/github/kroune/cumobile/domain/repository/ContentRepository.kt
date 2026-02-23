package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.data.model.UploadLinkData

/** Repository for longread/material content and file links. */
interface ContentRepository {
    /** Fetches all materials for a longread. */
    suspend fun fetchLongreadMaterials(longreadId: Int): List<LongreadMaterial>?

    /** Fetches a single material by ID. */
    suspend fun fetchMaterial(materialId: Int): LongreadMaterial?

    /**
     * Gets a pre-signed download URL for a file.
     *
     * @param filename the object key / filename in storage.
     * @param version the file version string.
     * @return the download URL, or null.
     */
    suspend fun getDownloadLink(
        filename: String,
        version: String,
    ): String?

    /**
     * Gets pre-signed upload data for uploading a file.
     *
     * @param directory the target directory in storage.
     * @param filename the file name.
     * @param contentType the MIME type of the file.
     * @return [UploadLinkData] with the upload URL and metadata, or null.
     */
    suspend fun getUploadLink(
        directory: String,
        filename: String,
        contentType: String,
    ): UploadLinkData?
}
