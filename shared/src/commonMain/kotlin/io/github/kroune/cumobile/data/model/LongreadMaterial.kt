package io.github.kroune.cumobile.data.model

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val logger = KotlinLogging.logger {}

/** Wrapper for the paginated longread materials API response. */
@Serializable
data class LongreadMaterialsResponse(
    val items: List<LongreadMaterial> = emptyList(),
)

/**
 * Material within a longread.
 *
 * Fetched via longread materials and single material endpoints.
 *
 * Known [discriminator] values: `"markdown"`, `"file"`, `"coding"`, `"questions"`.
 *
 * The [viewContentRaw] field is polymorphic: it may be a JSON string, a JSON
 * object with `"value"` or `"description"` keys, or a JSON-encoded string that
 * parses to such an object. Use [viewContent] for safe string extraction.
 */
@Serializable
data class LongreadMaterial(
    val id: String = "",
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
    val taskId: String? = null,
) {
    val isMarkdown: Boolean get() = discriminator == Discriminator.Markdown
    val isFile: Boolean get() = discriminator == Discriminator.File
    val isCoding: Boolean get() = discriminator == Discriminator.Coding
    val isQuestions: Boolean get() = discriminator == Discriminator.Questions
    val isVideoPlatform: Boolean get() = discriminator == Discriminator.VideoPlatform
    val isAudio: Boolean get() = discriminator == Discriminator.Audio
    val isVideo: Boolean get() = discriminator == Discriminator.Video
    val isImage: Boolean get() = discriminator == Discriminator.Image

    /** Known discriminator values for [LongreadMaterial.discriminator]. */
    object Discriminator {
        const val Markdown = "markdown"
        const val File = "file"
        const val Coding = "coding"
        const val Questions = "questions"
        const val VideoPlatform = "videoPlatform"
        const val Audio = "audio"
        const val Video = "video"
        const val Image = "image"
    }

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
    val maxScore: Int? = null,
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
 *
 * Handles three shapes:
 * 1. JSON object → extracts `"value"` or `"description"` field
 * 2. String that is a JSON-encoded object → parses it, then extracts as above
 * 3. Plain string → returned as-is
 */
private fun extractViewContent(element: JsonElement?): String? {
    if (element == null) return null
    return when (element) {
        is JsonPrimitive -> {
            val content = element.contentOrNull ?: return null
            tryExtractFromJsonString(content) ?: content
        }
        is JsonObject -> extractFromObject(element)
        else -> element.toString()
    }
}

/** Extracts `"value"` or `"description"` from a [JsonObject]. */
private fun extractFromObject(obj: JsonObject): String? =
    obj["value"]?.jsonPrimitive?.contentOrNull
        ?: obj["description"]?.jsonPrimitive?.contentOrNull

/**
 * Attempts to parse [content] as a JSON object and extract
 * `"value"` or `"description"`. Returns null if [content] is
 * not a JSON-encoded object.
 */
private fun tryExtractFromJsonString(content: String): String? {
    if (!content.trimStart().startsWith("{")) return null
    return try {
        val parsed = kotlinx.serialization.json.Json
            .parseToJsonElement(content)
        if (parsed is JsonObject) extractFromObject(parsed) else null
    } catch (e: Exception) {
        logger.warn(e) { "Failed to parse viewContent as JSON object" }
        null
    }
}
