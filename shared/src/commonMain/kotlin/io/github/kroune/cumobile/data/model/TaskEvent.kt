package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Event in the task history timeline.
 */
@Serializable
data class TaskEvent(
    val id: String = "",
    /** ISO 8601 datetime string. */
    val occurredOn: String? = null,
    val type: String = "",
    val actorEmail: String? = null,
    val actorName: String? = null,
    val content: TaskEventContent = TaskEventContent(),
)

/**
 * Content payload of a [TaskEvent].
 *
 * The API returns a complex nested structure. This DTO mirrors the raw JSON.
 * Fields from nested objects (reviewer name, solution URL, etc.) can be
 * accessed through the nested DTO properties or via convenience helpers.
 *
 * Note: [lateDays] is polymorphic — the API may return an `Int` or
 * an object `{"value": Int}`. Use [lateDaysValue] for safe access.
 */
@Serializable
data class TaskEventContent(
    val state: String? = null,
    val score: TaskEventScore? = null,
    val estimation: TaskEventEstimation? = null,
    val solution: TaskEventSolution? = null,
    val reviewer: TaskEventActor? = null,
    val reviewers: List<TaskEventActor>? = null,
    val task: TaskEventTask? = null,
    /** Exercise name at the top level of the content object. */
    val name: String? = null,
    /**
     * Polymorphic: either an `Int` or `{"value": Int}`.
     * Use [lateDaysValue] for safe extraction.
     */
    val lateDays: JsonElement? = null,
    /** ISO 8601 datetime string (top-level deadline in content). */
    val deadline: String? = null,
    /** Attachments added directly to the event. */
    val attached: List<MaterialAttachment>? = null,
) {
    /**
     * Extracts the integer late-days value regardless of JSON shape.
     * Handles both `"lateDays": 3` and `"lateDays": {"value": 3}`.
     */
    val lateDaysValue: Int?
        get() {
            if (lateDays == null) return null
            // Try as a plain number
            lateDays.toString().toIntOrNull()?.let { return it }
            // Not a number — likely {"value": N}, handled in domain layer
            return null
        }

    /** Merged attachments from [solution] and [attached]. */
    val allAttachments: List<MaterialAttachment>
        get() = (solution?.attachments.orEmpty()) + (attached.orEmpty())
}

/** Score info within a [TaskEventContent]. */
@Serializable
data class TaskEventScore(
    val level: String? = null,
    val value: Double? = null,
)

/**
 * Estimation info within a [TaskEventContent].
 *
 * The `activity` object is nested in JSON as `{"name": ..., "weight": ...}`.
 */
@Serializable
data class TaskEventEstimation(
    /** ISO 8601 datetime string. */
    val deadline: String? = null,
    val maxScore: Int? = null,
    val activity: TaskEventEstimationActivity? = null,
) {
    val activityName: String?
        get() = activity?.name

    val activityWeight: Double?
        get() = activity?.weight
}

/** Activity within [TaskEventEstimation]. */
@Serializable
data class TaskEventEstimationActivity(
    val name: String? = null,
    val weight: Double? = null,
)

/** Solution info nested within [TaskEventContent]. */
@Serializable
data class TaskEventSolution(
    val solutionUrl: String? = null,
    val attachments: List<MaterialAttachment> = emptyList(),
)

/**
 * Actor (reviewer) info within [TaskEventContent].
 *
 * The reviewer's name is nested as `{"name": {"last": ..., "first": ..., "middle": ...}}`.
 */
@Serializable
data class TaskEventActor(
    val name: TaskEventActorName? = null,
) {
    /** Full name in "Last First Middle" format. */
    val fullName: String?
        get() {
            val n = name ?: return null
            return listOfNotNull(n.last, n.first, n.middle)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { null }
        }
}

/** Name parts for a [TaskEventActor]. */
@Serializable
data class TaskEventActorName(
    val last: String? = null,
    val first: String? = null,
    val middle: String? = null,
)

/**
 * Nested task info within [TaskEventContent].
 *
 * Contains task state, deadline, and optional estimation snapshot.
 */
@Serializable
data class TaskEventTask(
    val state: String? = null,
    /** ISO 8601 datetime string. */
    val deadline: String? = null,
    val estimation: TaskEventEstimation? = null,
)
