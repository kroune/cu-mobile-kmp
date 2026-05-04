package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.GradebookGradeApi
import io.github.kroune.cumobile.data.model.GradebookResponseApi
import io.github.kroune.cumobile.data.model.GradebookSemesterApi
import io.github.kroune.cumobile.domain.model.GradebookGradeDomain
import io.github.kroune.cumobile.domain.model.GradebookResponseDomain
import io.github.kroune.cumobile.domain.model.GradebookSemesterDomain

fun GradebookResponseApi.toDomain(): GradebookResponseDomain =
    GradebookResponseDomain(
        semesters = semesters.map { it.toDomain() },
    )

fun GradebookSemesterApi.toDomain(): GradebookSemesterDomain =
    GradebookSemesterDomain(
        year = year,
        semesterNumber = semesterNumber,
        grades = grades.map { it.toDomain() },
    )

fun GradebookGradeApi.toDomain(): GradebookGradeDomain =
    GradebookGradeDomain(
        subject = subject,
        grade = grade,
        normalizedGrade = normalizedGrade,
        assessmentType = assessmentType,
        subjectType = subjectType,
    )
