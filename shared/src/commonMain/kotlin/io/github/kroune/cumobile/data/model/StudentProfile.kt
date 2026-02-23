package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Student profile from the Hub service.
 *
 * API endpoint: `GET /hub/students/me`
 */
@Serializable
data class StudentProfile(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val birthdate: String = "",
    val birthPlace: String? = null,
    val telegram: String? = null,
    val timeLogin: String = "",
    val inn: String = "",
    val snils: String = "",
    val course: Int = 0,
    val gender: String = "",
    val enrollmentPhase: String = "",
    val educationLevel: String = "",
    val emails: List<EmailInfo> = emptyList(),
    val phones: List<PhoneInfo> = emptyList(),
) {
    /** Full name in "Last First Middle" format. */
    val fullName: String
        get() = "$lastName $firstName $middleName".trim()

    /** University email, preferring `@edu.centraluniversity.ru` domain. */
    val universityEmail: String?
        get() = emails.firstOrNull { "@edu.centraluniversity.ru" in it.value }?.value
            ?: emails.firstOrNull { "@centraluniversity.ru" in it.value }?.value
            ?: emails.firstOrNull { "university" in it.type.lowercase() }?.value
            ?: emails.firstOrNull()?.value
}

/** Email entry within [StudentProfile]. */
@Serializable
data class EmailInfo(
    val value: String = "",
    val type: String = "",
)

/** Phone entry within [StudentProfile]. */
@Serializable
data class PhoneInfo(
    val value: String = "",
    val type: String = "",
)
