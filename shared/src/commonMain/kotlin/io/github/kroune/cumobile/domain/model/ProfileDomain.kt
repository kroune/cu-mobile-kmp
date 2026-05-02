package io.github.kroune.cumobile.domain.model

import kotlinx.collections.immutable.ImmutableList

enum class EducationLevel(
    val apiValue: String,
) {
    Bachelor("bachelor"),
    Master("master"),
    Specialist("specialist"),
    Unknown(""),
    ;

    companion object {
        fun fromApi(value: String): EducationLevel =
            entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: Unknown
    }
}

data class ProfileDomain(
    val id: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val timeLogin: String,
    val educationLevel: EducationLevel,
    val universityEmail: String?,
    val otherEmails: ImmutableList<EmailDomain>,
    val phones: ImmutableList<PhoneDomain>,
    val birthdate: String,
    val telegram: String?,
    val studentCourse: Int?,
)

data class EmailDomain(
    val value: String,
    val type: String,
)

data class PhoneDomain(
    val value: String,
    val type: String,
)
