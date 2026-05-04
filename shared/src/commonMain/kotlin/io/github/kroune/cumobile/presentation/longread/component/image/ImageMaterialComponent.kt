package io.github.kroune.cumobile.presentation.longread.component.image

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import io.github.kroune.cumobile.presentation.common.RenderComponent
import io.github.kroune.cumobile.presentation.common.model.LongreadMaterialUi
import io.github.kroune.cumobile.presentation.longread.ui.image.ImageMaterialCard

/**
 * Simple material component for standalone image materials.
 */
class ImageMaterialComponent(
    componentContext: ComponentContext,
    val material: LongreadMaterialUi,
) : ComponentContext by componentContext,
    RenderComponent {
    @Composable
    override fun Render() {
        ImageMaterialCard(material = material)
    }
}
