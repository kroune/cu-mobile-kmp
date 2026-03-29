package io.github.kroune.cumobile.presentation.longread.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.ShimmerBox

/**
 * Skeleton placeholder for a longread material (markdown) card.
 *
 * Matches: fullWidth card with title + 3 content lines.
 */
@Composable
internal fun LongreadMaterialSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(16.dp),
    ) {
        ShimmerBox(Modifier.fillMaxWidth(0.6f), height = 16.dp)
        Spacer(Modifier.height(8.dp))
        ShimmerBox(Modifier.fillMaxWidth(), height = 12.dp)
        Spacer(Modifier.height(4.dp))
        ShimmerBox(Modifier.fillMaxWidth(), height = 12.dp)
        Spacer(Modifier.height(4.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.7f), height = 12.dp)
    }
}
