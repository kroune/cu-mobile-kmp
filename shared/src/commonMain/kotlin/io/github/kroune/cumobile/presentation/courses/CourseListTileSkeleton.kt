@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.ShimmerBox

/**
 * Skeleton placeholder for course list tiles (DraggableCourseItem / CourseListTile).
 *
 * Matches: fullWidth, 12dp corner radius, name + category lines.
 */
@Composable
internal fun CourseListTileSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            ShimmerBox(Modifier.fillMaxWidth(0.7f), height = 14.dp)
            Spacer(Modifier.height(4.dp))
            ShimmerBox(Modifier.fillMaxWidth(0.4f), height = 12.dp)
        }
    }
}
