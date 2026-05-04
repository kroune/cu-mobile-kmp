package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.TaskApi
import io.github.kroune.cumobile.domain.model.TaskDomain
import io.github.kroune.cumobile.domain.model.TaskStatus

fun TaskApi.toDomain(): TaskDomain {
    val status = resolveTaskStatus(state, submitAt)
    return TaskDomain(
        id = id,
        status = status,
        score = score,
        extraScore = extraScore,
        deadline = parseDeadlineInstant(deadline),
        submitAt = parseInstant(submitAt),
        startedAt = parseInstant(startedAt),
        exerciseId = exercise.id,
        exerciseName = exercise.name,
        exerciseType = exercise.type,
        exerciseMaxScore = exercise.maxScore,
        exerciseDeadline = parseDeadlineInstant(exercise.deadline),
        activityName = exercise.activity?.name,
        activityWeight = exercise.activity?.weight,
        courseId = course.id,
        courseName = course.name,
        courseIsArchived = course.isArchived,
        themeId = theme.id,
        themeName = theme.name,
        longreadId = longread.id,
        isLateDaysEnabled = isLateDaysEnabled,
        lateDays = lateDays,
    )
}

private fun resolveTaskStatus(
    apiState: String,
    submitAt: String?,
): TaskStatus {
    if (apiState == "inProgress" && submitAt != null) return TaskStatus.HasSolution
    return apiState.toTaskStatus()
}
