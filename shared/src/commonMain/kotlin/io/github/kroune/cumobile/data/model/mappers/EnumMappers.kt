package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.domain.model.CourseCategory
import io.github.kroune.cumobile.domain.model.EducationLevel
import io.github.kroune.cumobile.domain.model.QuestionResult
import io.github.kroune.cumobile.domain.model.QuizQuestionType
import io.github.kroune.cumobile.domain.model.TaskStatus

fun String.toTaskStatus(): TaskStatus =
    when (this) {
        "backlog" -> TaskStatus.Backlog
        "inProgress" -> TaskStatus.InProgress
        "hasSolution" -> TaskStatus.HasSolution
        "review" -> TaskStatus.Review
        "revision" -> TaskStatus.Revision
        "rework" -> TaskStatus.Rework
        "failed" -> TaskStatus.Failed
        "rejected" -> TaskStatus.Rejected
        "evaluated" -> TaskStatus.Evaluated
        else -> TaskStatus.Unknown
    }

fun TaskStatus.toApiValue(): String =
    when (this) {
        TaskStatus.Backlog -> "backlog"
        TaskStatus.InProgress -> "inProgress"
        TaskStatus.HasSolution -> "hasSolution"
        TaskStatus.Review -> "review"
        TaskStatus.Revision -> "revision"
        TaskStatus.Rework -> "rework"
        TaskStatus.Failed -> "failed"
        TaskStatus.Rejected -> "rejected"
        TaskStatus.Evaluated -> "evaluated"
        TaskStatus.Unknown -> "unknown"
    }

fun String.toCourseCategory(): CourseCategory =
    when (this) {
        "general" -> CourseCategory.General
        "mathematics" -> CourseCategory.Mathematics
        "development" -> CourseCategory.Development
        "stem" -> CourseCategory.Stem
        "business" -> CourseCategory.Business
        "softSkills" -> CourseCategory.SoftSkills
        else -> CourseCategory.Unknown
    }

fun String.toQuizQuestionType(): QuizQuestionType =
    when (this) {
        "SingleChoice" -> QuizQuestionType.SingleChoice
        "MultipleChoice" -> QuizQuestionType.MultipleChoice
        "StringMatch" -> QuizQuestionType.StringMatch
        "NumberMatch" -> QuizQuestionType.NumberMatch
        "OpenText" -> QuizQuestionType.OpenText
        else -> QuizQuestionType.Unknown
    }

fun String.toQuestionResult(): QuestionResult =
    when (this) {
        "Unknown" -> QuestionResult.Unknown
        "Unanswered" -> QuestionResult.Unanswered
        "Review" -> QuestionResult.Review
        "Fail" -> QuestionResult.Fail
        "Success" -> QuestionResult.Success
        "PartialSuccess" -> QuestionResult.PartialSuccess
        else -> QuestionResult.Unknown
    }

fun String.toEducationLevel(): EducationLevel =
    when (this.lowercase()) {
        "bachelor" -> EducationLevel.Bachelor
        "master" -> EducationLevel.Master
        "specialist" -> EducationLevel.Specialist
        else -> EducationLevel.Unknown
    }
