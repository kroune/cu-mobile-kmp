package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.domain.model.LmsProfileDomain
import io.github.kroune.cumobile.domain.model.ProfileDomain

/** Repository for student profile and avatar operations. */
interface ProfileRepository {
    /** Fetches the student's hub profile. */
    suspend fun fetchProfile(): ProfileDomain?

    /** Fetches the student's LMS profile. */
    suspend fun fetchLmsProfile(): LmsProfileDomain?

    /** Fetches the student's avatar as raw bytes. */
    suspend fun fetchAvatar(): ByteArray

    /** Uploads a new avatar image. */
    suspend fun uploadAvatar(
        bytes: ByteArray,
        contentType: String,
    ): Boolean

    /** Deletes the student's avatar. */
    suspend fun deleteAvatar(): Boolean
}
