package io.github.kroune.cumobile.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Event in the task history timeline.
 */
@Serializable
data class TaskEventApi(
    val id: String = "",
    /** ISO 8601 datetime string. */
    val occurredOn: String? = null,
    val type: String = "",
    val actorEmail: String? = null,
    val actorName: String? = null,
    val content: TaskEventContentApi = TaskEventContentApi(),
)

/**
 * Content payload of a [TaskEventApi].
 *
 * The API returns a complex nested structure. This DTO mirrors the raw JSON.
 * Fields from nested objects (reviewer name, solution URL, etc.) can be
 * accessed through the nested DTO properties or via convenience helpers.
 *
 * Note: [lateDays] is polymorphic — the API may return an `Int` or
 * an object `{"value": Int}`. Use [lateDaysValue] for safe access.
 */
@Serializable
data class TaskEventContentApi(
    val state: String? = null,
    val score: TaskEventScoreApi? = null,
    val estimation: TaskEventEstimationApi? = null,
    val solution: TaskEventSolutionApi? = null,
    val reviewer: TaskEventActorApi? = null,
    val reviewers: List<TaskEventActorApi>? = null,
    val task: TaskEventTaskApi? = null,
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
    val attached: List<MaterialAttachmentApi>? = null,
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
    val allAttachments: List<MaterialAttachmentApi>
        get() = (solution?.attachments.orEmpty()) + (attached.orEmpty())
}

/** Score info within a [TaskEventContentApi]. */
@Serializable
data class TaskEventScoreApi(
    val level: String? = null,
    val value: Double? = null,
)

/**
 * Estimation info within a [TaskEventContentApi].
 *
 * The `activity` object is nested in JSON as `{"name": ..., "weight": ...}`.
 */
@Serializable
data class TaskEventEstimationApi(
    /** ISO 8601 datetime string. */
    val deadline: String? = null,
    val maxScore: Int? = null,
    val activity: TaskEventEstimationActivityApi? = null,
) {
    val activityName: String?
        get() = activity?.name

    val activityWeight: Double?
        get() = activity?.weight
}

/** Activity within [TaskEventEstimationApi]. */
@Serializable
data class TaskEventEstimationActivityApi(
    val name: String? = null,
    val weight: Double? = null,
)

/** Solution info nested within [TaskEventContentApi]. */
@Serializable
data class TaskEventSolutionApi(
    val solutionUrl: String? = null,
    val attachments: List<MaterialAttachmentApi> = emptyList(),
)

/**
 * Actor (reviewer) info within [TaskEventContentApi].
 *
 * The reviewer's name is nested as `{"name": {"last": ..., "first": ..., "middle": ...}}`.
 */
@Serializable
data class TaskEventActorApi(
    val name: TaskEventActorNameApi? = null,
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

/** Name parts for a [TaskEventActorApi]. */
@Serializable
data class TaskEventActorNameApi(
    val last: String? = null,
    val first: String? = null,
    val middle: String? = null,
)

/**
 * Nested task info within [TaskEventContentApi].
 *
 * Contains task state, deadline, and optional estimation snapshot.
 */
@Serializable
data class TaskEventTaskApi(
    val state: String? = null,
    /** ISO 8601 datetime string. */
    val deadline: String? = null,
    val estimation: TaskEventEstimationApi? = null,
)
