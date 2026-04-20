package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal fun Modifier.shimmerNode(
    base: Color,
    highlight: Color,
): Modifier = this.then(
    ShimmerElement(
        base = base,
        highlight = highlight,
    ),
)

private data class ShimmerElement(
    val base: Color,
    val highlight: Color,
) : ModifierNodeElement<ShimmerNode>() {

    override fun create(): ShimmerNode = ShimmerNode(
        base = base,
        highlight = highlight,
    )

    override fun update(node: ShimmerNode) {
        node.update(base, highlight)
    }
}

private class ShimmerNode(
    base: Color,
    highlight: Color,
) : Modifier.Node(), DrawModifierNode {

    override val shouldAutoInvalidate: Boolean = false

    private var base: Color = base
    private var highlight: Color = highlight

    private val translate = Animatable(0f)
    private var animationJob: Job? = null

    override fun onAttach() {
        startIfNeeded()
    }

    override fun onDetach() {
        animationJob?.cancel()
        animationJob = null
    }

    fun update(
        base: Color,
        highlight: Color,
    ) {
        var needsRedraw = false

        if (this.base != base) {
            this.base = base
            needsRedraw = true
        }
        if (this.highlight != highlight) {
            this.highlight = highlight
            needsRedraw = true
        }

        if (needsRedraw) {
            invalidateDraw()
        }

        startIfNeeded()
    }

    private fun startIfNeeded() {
        if (animationJob?.isActive == true) return

        animationJob = coroutineScope.launch {
            translate.snapTo(0f)
            translate.animateTo(
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1200,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Restart,
                ),
            ) {
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(base, highlight, base),
                start = Offset(translate.value, 0f),
                end = Offset(translate.value + 500f, 0f),
            ),
        )
    }
}
