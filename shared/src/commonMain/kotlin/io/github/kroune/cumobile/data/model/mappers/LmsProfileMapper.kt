package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.LmsProfileApi
import io.github.kroune.cumobile.domain.model.LmsProfileDomain

fun LmsProfileApi.toDomain(): LmsProfileDomain =
    LmsProfileDomain(
        id = id,
        lastName = lastName,
        firstName = firstName,
        middleName = middleName,
        universityEmail = universityEmail,
        timeAccount = timeAccount,
        studyStartYear = studyStartYear,
        studyLevel = studyLevel,
        lateDaysBalance = lateDaysBalance,
    )
