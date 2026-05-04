package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.CourseGradeDomain
import io.github.kroune.cumobile.domain.model.GradebookResponseDomain
import io.github.kroune.cumobile.presentation.common.model.CourseGradeUi
import io.github.kroune.cumobile.presentation.common.model.GradebookGradeUi
import io.github.kroune.cumobile.presentation.common.model.GradebookSemesterUi
import io.github.kroune.cumobile.presentation.common.model.GradebookUi
import io.github.kroune.cumobile.presentation.common.ui.gradeDescription
import kotlinx.collections.immutable.toImmutableList

fun GradebookResponseDomain.toUi(): GradebookUi =
    GradebookUi(
        semesters = semesters
            .map { semester ->
                GradebookSemesterUi(
                    year = semester.year,
                    semesterNumber = semester.semesterNumber,
                    grades = semester.grades
                        .map { grade ->
                            GradebookGradeUi(
                                subject = grade.subject,
                                normalizedGrade = grade.normalizedGrade,
                                assessmentType = grade.assessmentType,
                                subjectType = grade.subjectType,
                            )
                        }.toImmutableList(),
                )
            }.toImmutableList(),
    )

fun CourseGradeDomain.toUi(): CourseGradeUi =
    CourseGradeUi(
        id = id,
        name = name,
        description = description,
        total = total,
        totalFormatted = total.toString(),
        totalDescription = gradeDescription(total),
    )
