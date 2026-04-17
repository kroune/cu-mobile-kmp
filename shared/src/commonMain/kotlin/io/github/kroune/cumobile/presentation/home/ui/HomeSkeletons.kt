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
 * Matches the N3 layout: 240dp wide, 12dp corner radius, 8dp padding,
 * two columns — left (chip + course + title) and right (time + date).
 */
@Composable
internal fun DeadlineTaskCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .width(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ShimmerBox(Modifier.width(56.dp), height = 14.dp, cornerRadius = 6.dp)
            ShimmerBox(Modifier.fillMaxWidth(0.8f), height = 15.dp)
            ShimmerBox(Modifier.fillMaxWidth(0.6f), height = 13.dp)
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ShimmerBox(Modifier.width(52.dp), height = 22.dp)
            ShimmerBox(Modifier.width(44.dp), height = 15.dp)
        }
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
 * Skeleton placeholder for the week picker navigation.
 *
 * Matches: navigation header shimmer + 7 day pill shimmers.
 */
@Composable
internal fun WeekPickerSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        // Navigation header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            ShimmerBox(Modifier.width(140.dp), height = 14.dp)
        }

        Spacer(Modifier.height(6.dp))

        // Day pills row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(WeekDaysCount) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppTheme.colors.surface)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ShimmerBox(Modifier.width(20.dp), height = 11.dp)
                    ShimmerBox(Modifier.width(16.dp), height = 13.dp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

/**
 * Skeleton placeholder for [ScheduleCard].
 *
 * Matches: row with info column on left and time badge on right.
 */
@Composable
internal fun FloatingBadgeCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AppTheme.colors.surface)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Info column
        Column(
            modifier = Modifier.weight(1f).padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ShimmerBox(Modifier.fillMaxWidth(0.7f), height = 13.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ShimmerBox(Modifier.width(40.dp), height = 11.dp)
                ShimmerBox(Modifier.width(50.dp), height = 11.dp)
            }
        }

        // Time badge
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AppTheme.colors.background.copy(alpha = 0.8f))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShimmerBox(Modifier.width(36.dp), height = 14.dp)
            Spacer(Modifier.height(2.dp))
            ShimmerBox(Modifier.width(36.dp), height = 12.dp)
        }
    }
}

private const val WeekDaysCount = 7
