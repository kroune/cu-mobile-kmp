package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Returns an animated [Brush] that produces a shimmer sweep effect.
 *
 * Colors come from [AppTheme.colors] — `shimmerBase` and `shimmerHighlight`.
 */
@Composable
internal fun shimmerBrush(): Brush {
    val colors = AppTheme.colors
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    return Brush.linearGradient(
        colors = listOf(colors.shimmerBase, colors.shimmerHighlight, colors.shimmerBase),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 500f, 0f),
    )
}

/**
 * Rectangular shimmer placeholder with rounded corners.
 *
 * @param height Height of the placeholder.
 * @param cornerRadius Corner radius (default 4dp for text-like shapes).
 */
@Composable
internal fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    cornerRadius: Dp = 4.dp,
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush),
    )
}

/**
 * Circular shimmer placeholder.
 *
 * @param size Diameter of the circle.
 */
@Composable
internal fun ShimmerCircle(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush),
    )
}
