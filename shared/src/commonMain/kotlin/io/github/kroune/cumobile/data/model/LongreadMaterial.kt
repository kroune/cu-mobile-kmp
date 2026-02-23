package io.github.kroune.cumobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Material within a longread.
 *
 * API endpoints:
 * - List: `GET /micro-lms/longreads/{longreadId}/materials?limit=10000`
 * - Single: `GET /micro-lms/materials/{materialId}`
 *
 * Known [discriminator] values: `"markdown"`, `"file"`, `"coding"`, `"questions"`.
 *
 * The [viewContentRaw] field is polymorphic: it may be a JSON string, a JSON
 * object with `"value"` or `"description"` keys, or a JSON-encoded string that
 * parses to such an object. Use [viewContent] for safe string extraction.
 */
@Serializable
data class LongreadMaterial(
    val id: Int = 0,
    val discriminator: String = "",
    /**
     * Polymorphic: may be a string, a JSON object, or null.
     * Use [viewContent] for safe extraction.
     */
    @SerialName("viewContent")
    val viewContentRaw: JsonElement? = null,
    val filename: String? = null,
    val version: String? = null,
    val length: Int? = null,
    val name: String? = null,
    val content: LongreadMaterialContent? = null,
    val attachments: List<MaterialAttachment> = emptyList(),
    val estimation: MaterialEstimation? = null,
    val taskId: Int? = null,
) {
    val isMarkdown: Boolean get() = discriminator == "markdown"
    val isFile: Boolean get() = discriminator == "file"
    val isCoding: Boolean get() = discriminator == "coding"
    val isQuestions: Boolean get() = discriminator == "questions"

    /** Content name from the nested [content] object. */
    val contentName: String?
        get() = content?.name

    /**
     * Extracts the display content string from [viewContentRaw].
     *
     * Handles three shapes:
     * 1. Plain string → returned as-is
     * 2. JSON object → extracts `"value"` or `"description"` field
     * 3. null → returns null
     */
    val viewContent: String?
        get() = extractViewContent(viewContentRaw)
}

/** Nested `content` object inside [LongreadMaterial]. */
@Serializable
data class LongreadMaterialContent(
    val name: String? = null,
)

/** File attachment for materials, solutions, comments, and events. */
@Serializable
data class MaterialAttachment(
    val name: String = "",
    val filename: String = "",
    val mediaType: String = "",
    val length: Int = 0,
    val version: String = "",
)

/**
 * Grading estimation info for a material/exercise.
 *
 * The `activity` object is nested in JSON as `{"name": ..., "weight": ...}`.
 */
@Serializable
data class MaterialEstimation(
    /** ISO 8601 datetime string. */
    val deadline: String? = null,
    val maxScore: Int = 0,
    val activity: MaterialEstimationActivity? = null,
) {
    val activityName: String?
        get() = activity?.name

    val activityWeight: Double?
        get() = activity?.weight
}

/** Activity info within [MaterialEstimation]. */
@Serializable
data class MaterialEstimationActivity(
    val name: String? = null,
    val weight: Double? = null,
)

/**
 * Extracts a display-ready string from a polymorphic `viewContent` JSON element.
 */
private fun extractViewContent(element: JsonElement?): String? {
    if (element == null) return null
    return when (element) {
        is JsonPrimitive -> element.contentOrNull
        is JsonObject -> {
            element["value"]?.jsonPrimitive?.contentOrNull
                ?: element["description"]?.jsonPrimitive?.contentOrNull
        }
        else -> element.toString()
    }
}
