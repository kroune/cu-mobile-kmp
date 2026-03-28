@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.ShimmerBox
import io.github.kroune.cumobile.presentation.common.ui.ShimmerCircle

/**
 * Skeleton placeholder for [NotificationCard].
 *
 * Matches: 36dp circle icon + title, date, and description shimmer lines.
 */
@Composable
internal fun NotificationCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        ShimmerCircle(size = 36.dp)

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            ShimmerBox(Modifier.fillMaxWidth(0.7f), height = 14.dp)
            Spacer(Modifier.height(4.dp))
            ShimmerBox(Modifier.fillMaxWidth(0.4f), height = 11.dp)
            Spacer(Modifier.height(4.dp))
            ShimmerBox(Modifier.fillMaxWidth(0.9f), height = 12.dp)
            Spacer(Modifier.height(2.dp))
            ShimmerBox(Modifier.fillMaxWidth(0.6f), height = 12.dp)
        }
    }
}
