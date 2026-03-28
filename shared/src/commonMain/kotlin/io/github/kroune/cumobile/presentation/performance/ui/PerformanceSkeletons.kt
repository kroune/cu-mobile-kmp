@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.performance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * Skeleton placeholder for TotalGradeCard on the performance screen.
 *
 * Matches: 64dp grade box + label/description/name text lines.
 */
@Composable
internal fun TotalGradeCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShimmerBox(
            Modifier.size(64.dp),
            height = 64.dp,
            cornerRadius = 8.dp,
        )
        Spacer(Modifier.width(16.dp))
        Column {
            ShimmerBox(Modifier.width(100.dp), height = 12.dp)
            Spacer(Modifier.height(4.dp))
            ShimmerBox(Modifier.width(80.dp), height = 16.dp)
            Spacer(Modifier.height(4.dp))
            ShimmerBox(Modifier.width(140.dp), height = 12.dp)
        }
    }
}

/**
 * Skeleton placeholder for ExerciseTile on the scores tab.
 *
 * Matches: fullWidth card with theme name, exercise name, activity name, and score badge.
 */
@Composable
internal fun ExerciseTileSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBox(Modifier.weight(1f).fillMaxWidth(0.5f), height = 11.dp)
            Spacer(Modifier.width(12.dp))
            ShimmerBox(
                Modifier.width(60.dp),
                height = 20.dp,
                cornerRadius = 8.dp,
            )
        }
        Spacer(Modifier.height(4.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.8f), height = 14.dp)
        Spacer(Modifier.height(4.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.4f), height = 12.dp)
    }
}
