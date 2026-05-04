package io.github.kroune.cumobile.presentation.common.model

import kotlinx.collections.immutable.ImmutableList

data class ProfileUi(
    val fullName: String,
    val timeLogin: String,
    val educationLevelLabel: String,
    val studentCourseLabel: String?,
    val universityEmail: String?,
    val otherEmails: ImmutableList<EmailUi>,
    val phones: ImmutableList<PhoneUi>,
    val telegram: String?,
    val birthdate: String,
)

data class EmailUi(
    val value: String,
    val type: String,
)

data class PhoneUi(
    val value: String,
    val type: String,
)
