package io.github.kroune.cumobile.presentation.longread.component

import kotlinx.serialization.Serializable

/**
 * Serializable configuration for each material in the longread ChildItems.
 *
 * Each variant maps 1:1 to a [LongreadMaterial][io.github.kroune.cumobile.data.model.LongreadMaterial]
 * discriminator. The [id] must be unique across the items list.
 */
@Serializable
sealed interface MaterialConfig {
    val id: String

    @Serializable
    data class Markdown(
        override val id: String,
    ) : MaterialConfig

    @Serializable
    data class File(
        override val id: String,
    ) : MaterialConfig

    @Serializable
    data class Coding(
        override val id: String,
        val taskId: String,
    ) : MaterialConfig

    @Serializable
    data class Questions(
        override val id: String,
        val taskId: String,
    ) : MaterialConfig

    @Serializable
    data class Image(
        override val id: String,
    ) : MaterialConfig

    @Serializable
    data class Video(
        override val id: String,
    ) : MaterialConfig

    @Serializable
    data class Audio(
        override val id: String,
    ) : MaterialConfig
}
