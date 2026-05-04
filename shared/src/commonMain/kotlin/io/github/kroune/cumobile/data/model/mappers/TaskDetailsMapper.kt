package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.TaskDetailsApi
import io.github.kroune.cumobile.domain.model.TaskDetailsDomain
import io.github.kroune.cumobile.domain.model.TaskDetailsExerciseDomain
import io.github.kroune.cumobile.domain.model.TaskDetailsSolutionDomain

fun TaskDetailsApi.toDomain(): TaskDetailsDomain =
    TaskDetailsDomain(
        id = id,
        score = score,
        extraScore = extraScore,
        scoreSkillLevel = scoreSkillLevel,
        status = state?.let { it.toTaskStatus() },
        submitAt = parseInstant(submitAt),
        isLateDaysEnabled = isLateDaysEnabled,
        lateDays = lateDays,
        deadline = parseDeadlineInstant(deadline),
        startedAt = parseInstant(startedAt),
        attemptStartedAt = parseInstant(attemptStartedAt),
        quizSessionId = quizSessionId,
        currentAttemptId = currentAttemptId,
        evaluatedAttemptId = evaluatedAttemptId,
        lastAttemptId = lastAttemptId,
        exercise = exercise?.let {
            TaskDetailsExerciseDomain(
                id = it.id,
                name = it.name,
                type = it.type,
                timer = it.timer,
                maxScore = it.maxScore,
                attemptsLimit = it.settings?.attemptsLimit,
                evaluationStrategy = it.settings?.evaluationStrategy?.name,
            )
        },
        solution = solution?.let {
            TaskDetailsSolutionDomain(
                solutionUrl = it.solutionUrl,
                attachments = it.attachments.map { a -> a.toDomain() },
                answers = it.answers.map { a -> a.toDomain() },
            )
        },
        studentLateDaysBalance = student?.lateDaysBalance,
    )
