package io.github.kroune.cumobile.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val ShimmerDurationMs = 1200
private const val ShimmerTargetValue = 1000f
private val DefaultBoxHeight = 14.dp
private val DefaultBoxCornerRadius = 4.dp
private val DefaultCircleSize = 36.dp
private val PreviewPadding = 16.dp

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
        targetValue = ShimmerTargetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ShimmerDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    return Brush.linearGradient(
        colors = listOf(colors.shimmerBase, colors.shimmerHighlight, colors.shimmerBase),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + ShimmerTargetValue / 2f, 0f),
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
    height: Dp = DefaultBoxHeight,
    cornerRadius: Dp = DefaultBoxCornerRadius,
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
    size: Dp = DefaultCircleSize,
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush),
    )
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewShimmerBoxDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 16.dp)
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewShimmerBoxLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 16.dp)
        }
    }
}

@Preview
@Composable
private fun PreviewShimmerCircleDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            ShimmerCircle()
        }
    }
}

@Preview
@Composable
private fun PreviewShimmerCircleLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            ShimmerCircle()
        }
    }
}
