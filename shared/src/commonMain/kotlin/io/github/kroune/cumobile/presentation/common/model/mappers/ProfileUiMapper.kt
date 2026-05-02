package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.EducationLevel
import io.github.kroune.cumobile.domain.model.LmsProfileDomain
import io.github.kroune.cumobile.domain.model.ProfileDomain
import io.github.kroune.cumobile.presentation.common.model.EmailUi
import io.github.kroune.cumobile.presentation.common.model.LmsProfileUi
import io.github.kroune.cumobile.presentation.common.model.PhoneUi
import io.github.kroune.cumobile.presentation.common.model.ProfileUi
import kotlinx.collections.immutable.toImmutableList

fun ProfileDomain.toUi(): ProfileUi =
    ProfileUi(
        fullName = "$lastName $firstName $middleName".trim(),
        timeLogin = timeLogin,
        educationLevelLabel = educationLevel.label(),
        studentCourseLabel = studentCourse?.let { "$it курс" },
        universityEmail = universityEmail,
        otherEmails = otherEmails.map { EmailUi(value = it.value, type = it.type) }.toImmutableList(),
        phones = phones.map { PhoneUi(value = it.value, type = it.type) }.toImmutableList(),
        telegram = telegram,
        birthdate = birthdate,
    )

private fun EducationLevel.label(): String =
    when (this) {
        EducationLevel.Bachelor -> "Бакалавриат"
        EducationLevel.Master -> "Магистратура"
        EducationLevel.Specialist -> "Специалитет"
        EducationLevel.Unknown -> ""
    }

fun LmsProfileDomain.toUi(): LmsProfileUi =
    LmsProfileUi(
        lateDaysBalance,
    )
