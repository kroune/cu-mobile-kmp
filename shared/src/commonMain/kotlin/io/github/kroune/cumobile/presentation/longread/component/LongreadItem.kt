package io.github.kroune.cumobile.presentation.longread.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.kroune.cumobile.presentation.common.RenderComponent
import io.github.kroune.cumobile.presentation.longread.component.audio.AudioMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.coding.CodingMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.file.FileMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.image.ImageMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.markdown.MarkdownMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.questions.QuestionsMaterialComponent
import io.github.kroune.cumobile.presentation.longread.component.video.VideoMaterialComponent

/**
 * Sealed wrapper around material child components.
 *
 * Each variant wraps a specific component and forwards [Render].
 * Used as the "T" type in `LazyChildItems<MaterialConfig, LongreadItem>`.
 */
@Stable
sealed interface LongreadItem : RenderComponent {
    @Stable
    class Markdown(
        private val component: MarkdownMaterialComponent,
    ) : LongreadItem {
        @Composable
        override fun Render() =
            component.Render()
    }

    @Stable
    class File(
        private val component: FileMaterialComponent,
    ) : LongreadItem {
        @Composable
        override fun Render() =
            component.Render()
    }

    @Stable
    class Coding(
        val component: CodingMaterialComponent,
    ) : LongreadItem {
        @Composable
        override fun Render() =
            component.Render()
    }

    @Stable
    class Questions(
        val component: QuestionsMaterialComponent,
    ) : LongreadItem {
        @Composable
        override fun Render() =
            component.Render()
    }

    @Stable
    class Image(
        private val component: ImageMaterialComponent,
    ) : LongreadItem {
        @Composable
        override fun Render() =
            component.Render()
    }

    @Stable
    class Video(
        private val component: VideoMaterialComponent,
    ) : LongreadItem {
        @Composable
        override fun Render() =
            component.Render()
    }

    @Stable
    class Audio(
        private val component: AudioMaterialComponent,
    ) : LongreadItem {
        @Composable
        override fun Render() =
            component.Render()
    }
}
