package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.StudentLmsProfile
import io.github.kroune.cumobile.data.model.StudentProfile

/** Repository for student profile and avatar operations. */
interface ProfileRepository {
    /** Fetches the student's hub profile. */
    suspend fun fetchProfile(): StudentProfile?

    /** Fetches the student's LMS profile. */
    suspend fun fetchLmsProfile(): StudentLmsProfile?

    /** Fetches the student's avatar as raw bytes. */
    suspend fun fetchAvatar(): ByteArray?

    /** Deletes the student's avatar. */
    suspend fun deleteAvatar(): Boolean
}
