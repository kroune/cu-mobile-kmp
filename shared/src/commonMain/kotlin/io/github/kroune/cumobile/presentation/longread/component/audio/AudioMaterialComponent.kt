package io.github.kroune.cumobile.presentation.longread.component.audio

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import io.github.kroune.cumobile.presentation.common.RenderComponent
import io.github.kroune.cumobile.presentation.common.model.LongreadMaterialUi
import io.github.kroune.cumobile.presentation.longread.ui.audio.AudioMaterialCard

/**
 * Simple material component for audio materials.
 */
class AudioMaterialComponent(
    componentContext: ComponentContext,
    val material: LongreadMaterialUi,
) : ComponentContext by componentContext,
    RenderComponent {
    @Composable
    override fun Render() {
        AudioMaterialCard(material = material)
    }
}
