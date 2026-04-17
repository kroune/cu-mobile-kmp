package io.github.kroune.cumobile.presentation.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
 * Fixed height chosen to match a typical filled card (chip + 2-line course + task)
 * so the deadlines row doesn't jump when data replaces the skeleton.
 */
@Composable
internal fun DeadlineTaskCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .width(240.dp)
            .height(96.dp)
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
            ShimmerBox(Modifier.fillMaxWidth(0.6f), height = 15.dp)
            ShimmerBox(Modifier.fillMaxWidth(0.5f), height = 13.dp)
        }
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            ShimmerBox(Modifier.width(52.dp), height = 22.dp)
            ShimmerBox(Modifier.width(44.dp), height = 15.dp)
        }
    }
}

/**
 * Skeleton placeholder for [CourseCard].
 *
 * Real card puts course name at top and category chip at bottom via SpaceBetween
 * inside an aspect-ratio 1.4 box. Two name lines mirror the 2-line maxLines in the
 * real card, and chip dimensions match the real chip (11sp label + 4dp vertical padding).
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
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ShimmerBox(Modifier.fillMaxWidth(0.9f), height = 14.dp)
            ShimmerBox(Modifier.fillMaxWidth(0.55f), height = 14.dp)
        }
        ShimmerBox(
            Modifier.width(70.dp),
            height = 19.dp,
            cornerRadius = 8.dp,
        )
    }
}

/**
 * Skeleton placeholder for the week picker navigation.
 *
 * Navigation header height matches the real row's 48dp (set by Material3 IconButton's
 * minimum interactive size) so the schedule doesn't shift down when controls render.
 */
@Composable
internal fun WeekPickerSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            ShimmerBox(Modifier.width(140.dp), height = 14.dp)
        }

        Spacer(Modifier.height(6.dp))

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
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
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
