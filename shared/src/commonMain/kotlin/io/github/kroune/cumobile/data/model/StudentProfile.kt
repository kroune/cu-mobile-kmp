package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable

/**
 * Student profile from the Hub service.
 *
 * API endpoint: `GET /hub/students/me`
 */
@Serializable
data class ProfileApi(
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
    val course: Int? = null,
    val gender: String = "",
    val enrollmentPhase: String = "",
    val educationLevel: String = "",
    val emails: List<EmailInfoApi> = emptyList(),
    val phones: List<PhoneInfoApi> = emptyList(),
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

/** Email entry within [ProfileApi]. */
@Serializable
data class EmailInfoApi(
    val value: String = "",
    val type: String = "",
)

/** Phone entry within [ProfileApi]. */
@Serializable
data class PhoneInfoApi(
    val value: String = "",
    val type: String = "",
)
