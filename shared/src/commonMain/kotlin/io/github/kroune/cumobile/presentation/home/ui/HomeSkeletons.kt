@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.ShimmerBox

/**
 * Skeleton placeholder for [DeadlineTaskCard].
 *
 * Matches: 200dp wide, 12dp corner radius, 12dp padding,
 * 4 shimmer lines (title, subtitle, deadline, badge).
 */
@Composable
internal fun DeadlineTaskCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ShimmerBox(Modifier.fillMaxWidth(0.8f), height = 14.dp)
        ShimmerBox(Modifier.fillMaxWidth(0.6f), height = 12.dp)
        ShimmerBox(Modifier.fillMaxWidth(0.5f), height = 12.dp)
        ShimmerBox(Modifier.width(80.dp), height = 20.dp, cornerRadius = 8.dp)
    }
}

/**
 * Skeleton placeholder for [CourseCard].
 *
 * Matches: fillMaxWidth, 12dp corner radius, 12dp padding,
 * name line + category chip.
 */
@Composable
internal fun CourseCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        ShimmerBox(Modifier.fillMaxWidth(0.7f), height = 14.dp)
        Spacer(Modifier.height(8.dp))
        ShimmerBox(
            Modifier.width(70.dp),
            height = 22.dp,
            cornerRadius = 8.dp,
        )
    }
}

/**
 * Skeleton placeholder for ClassCard on the Home screen.
 *
 * Matches: fullWidth row with time column, vertical separator, and content column.
 */
@Composable
internal fun ClassCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Time column
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ShimmerBox(Modifier.width(40.dp), height = 14.dp)
            Spacer(Modifier.height(4.dp))
            ShimmerBox(Modifier.width(40.dp), height = 12.dp)
        }

        Spacer(Modifier.width(16.dp))

        // Separator
        ShimmerBox(
            Modifier.width(2.dp).height(40.dp),
            height = 40.dp,
        )

        Spacer(Modifier.width(16.dp))

        // Content column
        Column(Modifier.weight(1f)) {
            ShimmerBox(Modifier.fillMaxWidth(0.7f), height = 14.dp)
            Spacer(Modifier.height(4.dp))
            ShimmerBox(Modifier.fillMaxWidth(0.5f), height = 12.dp)
        }
    }
}
