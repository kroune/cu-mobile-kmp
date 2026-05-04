package io.github.kroune.cumobile.domain.model

enum class EducationLevel {
    Bachelor,
    Master,
    Specialist,
    Unknown,
}

data class ProfileDomain(
    val id: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val timeLogin: String,
    val educationLevel: EducationLevel,
    val universityEmail: String?,
    val otherEmails: List<EmailDomain>,
    val phones: List<PhoneDomain>,
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
