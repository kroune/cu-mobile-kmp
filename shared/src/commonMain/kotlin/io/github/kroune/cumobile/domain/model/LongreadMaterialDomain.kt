package io.github.kroune.cumobile.domain.model

import kotlin.time.Instant

data class LongreadMaterialDomain(
    val id: String,
    val discriminator: String = "",
    val viewContentRaw: String? = null,
    val filename: String? = null,
    val version: String? = null,
    val length: Int? = null,
    val name: String? = null,
    val contentName: String? = null,
    val attachments: List<MaterialAttachmentDomain> = emptyList(),
    val estimationDeadline: Instant? = null,
    val estimationMaxScore: Int? = null,
    val estimationActivityName: String? = null,
    val estimationActivityWeight: Double? = null,
    val taskId: String? = null,
)

/** Alias for [viewContentRaw] used in UI components. */
val LongreadMaterialDomain.viewContent: String? get() = viewContentRaw

/** Known discriminator values for [LongreadMaterialDomain.discriminator]. */
object LongreadDiscriminator {
    const val Markdown = "markdown"
    const val File = "file"
    const val Coding = "coding"
    const val Questions = "questions"
    const val VideoPlatform = "videoPlatform"
    const val Audio = "audio"
    const val Video = "video"
    const val Image = "image"
}

val LongreadMaterialDomain.isMarkdown: Boolean get() = discriminator == LongreadDiscriminator.Markdown
val LongreadMaterialDomain.isFile: Boolean get() = discriminator == LongreadDiscriminator.File
val LongreadMaterialDomain.isCoding: Boolean get() = discriminator == LongreadDiscriminator.Coding
val LongreadMaterialDomain.isQuestions: Boolean get() = discriminator == LongreadDiscriminator.Questions
val LongreadMaterialDomain.isVideoPlatform: Boolean get() = discriminator == LongreadDiscriminator.VideoPlatform
val LongreadMaterialDomain.isAudio: Boolean get() = discriminator == LongreadDiscriminator.Audio
val LongreadMaterialDomain.isVideo: Boolean get() = discriminator == LongreadDiscriminator.Video
val LongreadMaterialDomain.isImage: Boolean get() = discriminator == LongreadDiscriminator.Image
