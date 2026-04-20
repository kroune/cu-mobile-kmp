package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .shimmerNode(
                base = colors.shimmerBase,
                highlight = colors.shimmerHighlight,
            ),
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
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .shimmerNode(
                base = colors.shimmerBase,
                highlight = colors.shimmerHighlight,
            ),
    )
}
