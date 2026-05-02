package io.github.kroune.cumobile.domain.model

data class LmsProfileDomain(
    val id: String,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val universityEmail: String,
    val timeAccount: String,
    val studyStartYear: Int?,
    val studyLevel: String,
    val lateDaysBalance: Int,
)
