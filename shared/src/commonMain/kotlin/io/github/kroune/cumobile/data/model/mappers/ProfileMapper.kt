package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.ProfileApi
import io.github.kroune.cumobile.domain.model.EducationLevel
import io.github.kroune.cumobile.domain.model.EmailDomain
import io.github.kroune.cumobile.domain.model.PhoneDomain
import io.github.kroune.cumobile.domain.model.ProfileDomain
import kotlinx.collections.immutable.toImmutableList

fun ProfileApi.toDomain(): ProfileDomain {
    val uniEmail = emails.firstOrNull { "@edu.centraluniversity.ru" in it.value }?.value
        ?: emails.firstOrNull { "@centraluniversity.ru" in it.value }?.value
        ?: emails.firstOrNull { "university" in it.type.lowercase() }?.value
        ?: emails.firstOrNull()?.value
    return ProfileDomain(
        id = id,
        firstName = firstName,
        lastName = lastName,
        middleName = middleName,
        timeLogin = timeLogin,
        educationLevel = EducationLevel.fromApi(educationLevel),
        universityEmail = uniEmail,
        otherEmails = emails
            .filter { it.value != uniEmail }
            .map { EmailDomain(value = it.value, type = it.type) }
            .toImmutableList(),
        phones = phones
            .map { PhoneDomain(value = it.value, type = it.type) }
            .toImmutableList(),
        birthdate = birthdate,
        telegram = telegram,
        studentCourse = course,
    )
}
