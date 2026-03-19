package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Student profile from the micro-LMS service.
 */
@Serializable
data class StudentLmsProfile(
    val id: String = "",
    val lastName: String = "",
    val firstName: String = "",
    val middleName: String? = null,
    val universityEmail: String = "",
    val timeAccount: String = "",
    val studyStartYear: Int? = null,
    val studyLevel: String = "",
    val lateDaysBalance: Int = 0,
)
