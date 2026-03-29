package io.github.kroune.cumobile.presentation.longread.component.video

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.presentation.common.RenderComponent
import io.github.kroune.cumobile.presentation.longread.ui.video.VideoMaterialCard

/**
 * Simple material component for video/videoPlatform materials.
 */
class VideoMaterialComponent(
    componentContext: ComponentContext,
    val material: LongreadMaterial,
) : ComponentContext by componentContext,
    RenderComponent {
    @Composable
    override fun Render() {
        VideoMaterialCard(material = material)
    }
}
