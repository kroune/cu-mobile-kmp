package io.github.kroune.cumobile.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Shared dimension constants matching real card layouts
private val CardCornerRadius = 12.dp
private val CardPadding = 12.dp
private val CardSpacing = 6.dp
private val ChipCornerRadius = 8.dp
private val PreviewPadding = 16.dp

// DeadlineTaskCard dimensions
private val TaskCardWidth = 200.dp
private val TaskTitleHeight = 14.dp
private val TaskSubtitleHeight = 12.dp
private val TaskBadgeWidth = 80.dp
private val TaskBadgeHeight = 20.dp

// NotificationCard dimensions
private val NotificationIconSize = 36.dp
private val NotificationIconSpacing = 12.dp
private val NotificationTitleHeight = 14.dp
private val NotificationDateHeight = 11.dp
private val NotificationDescriptionHeight = 12.dp
private val NotificationLineSpacing = 4.dp

// TotalGradeCard dimensions
private val GradeBoxSize = 64.dp
private val GradeBoxCornerRadius = 8.dp
private val GradeBoxSpacing = 16.dp
private val GradeLabelWidth = 100.dp
private val GradeLabelHeight = 12.dp
private val GradeDescriptionWidth = 80.dp
private val GradeDescriptionHeight = 16.dp
private val GradeNameWidth = 140.dp

// ClassCard dimensions
private val ClassTimeColumnWidth = 40.dp
private val ClassTimeHeight = 14.dp
private val ClassEndTimeHeight = 12.dp
private val ClassSeparatorWidth = 2.dp
private val ClassSeparatorHeight = 40.dp
private val ClassTitleHeight = 14.dp
private val ClassSubtitleHeight = 12.dp
private val ClassColumnSpacing = 16.dp

// ExerciseTile dimensions
private val ExerciseThemeHeight = 11.dp
private val ExerciseNameHeight = 14.dp
private val ExerciseActivityHeight = 12.dp
private val ExerciseBadgeWidth = 60.dp
private val ExerciseBadgeHeight = 20.dp
private val ExerciseInnerSpacing = 4.dp

// CourseListTile dimensions
private val CourseTileVerticalPadding = 14.dp
private val CourseTitleHeight = 14.dp
private val CourseCategoryHeight = 12.dp

// CourseCard dimensions
private val CourseNameHeight = 14.dp
private val CourseChipWidth = 70.dp
private val CourseChipHeight = 22.dp
private val CourseChipTopPadding = 8.dp

// LongreadMaterial dimensions
private val LongreadPadding = 16.dp
private val LongreadTitleHeight = 16.dp
private val LongreadTitleSpacing = 8.dp
private val LongreadLineHeight = 12.dp
private val LongreadLineSpacing = 4.dp

// Fraction constants for shimmer line widths
private const val FractionLarge = 0.8f
private const val FractionMedium = 0.7f
private const val FractionSmall = 0.6f
private const val FractionTiny = 0.4f
private const val FractionFull = 0.9f
private const val FractionHalf = 0.5f

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
            .width(TaskCardWidth)
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(AppTheme.colors.surface)
            .padding(CardPadding),
        verticalArrangement = Arrangement.spacedBy(CardSpacing),
    ) {
        ShimmerBox(Modifier.fillMaxWidth(FractionLarge), height = TaskTitleHeight)
        ShimmerBox(Modifier.fillMaxWidth(FractionSmall), height = TaskSubtitleHeight)
        ShimmerBox(Modifier.fillMaxWidth(FractionHalf), height = TaskSubtitleHeight)
        ShimmerBox(Modifier.width(TaskBadgeWidth), height = TaskBadgeHeight, cornerRadius = ChipCornerRadius)
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
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(AppTheme.colors.surface)
            .padding(CardPadding),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        ShimmerBox(Modifier.fillMaxWidth(FractionMedium), height = CourseNameHeight)
        Spacer(Modifier.height(CourseChipTopPadding))
        ShimmerBox(
            Modifier.width(CourseChipWidth),
            height = CourseChipHeight,
            cornerRadius = ChipCornerRadius,
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
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(AppTheme.colors.surface)
            .padding(CardPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Time column
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ShimmerBox(Modifier.width(ClassTimeColumnWidth), height = ClassTimeHeight)
            Spacer(Modifier.height(ExerciseInnerSpacing))
            ShimmerBox(Modifier.width(ClassTimeColumnWidth), height = ClassEndTimeHeight)
        }

        Spacer(Modifier.width(ClassColumnSpacing))

        // Separator
        ShimmerBox(
            Modifier.width(ClassSeparatorWidth).height(ClassSeparatorHeight),
            height = ClassSeparatorHeight,
        )

        Spacer(Modifier.width(ClassColumnSpacing))

        // Content column
        Column(Modifier.weight(1f)) {
            ShimmerBox(Modifier.fillMaxWidth(FractionMedium), height = ClassTitleHeight)
            Spacer(Modifier.height(ExerciseInnerSpacing))
            ShimmerBox(Modifier.fillMaxWidth(FractionHalf), height = ClassSubtitleHeight)
        }
    }
}

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
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(AppTheme.colors.surface)
            .padding(horizontal = CardPadding, vertical = CourseTileVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            ShimmerBox(Modifier.fillMaxWidth(FractionMedium), height = CourseTitleHeight)
            Spacer(Modifier.height(ExerciseInnerSpacing))
            ShimmerBox(Modifier.fillMaxWidth(FractionTiny), height = CourseCategoryHeight)
        }
    }
}

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
            .background(AppTheme.colors.surface, RoundedCornerShape(CardCornerRadius))
            .padding(CardPadding),
    ) {
        ShimmerCircle(size = NotificationIconSize)

        Spacer(Modifier.width(NotificationIconSpacing))

        Column(Modifier.weight(1f)) {
            ShimmerBox(Modifier.fillMaxWidth(FractionMedium), height = NotificationTitleHeight)
            Spacer(Modifier.height(NotificationLineSpacing))
            ShimmerBox(Modifier.fillMaxWidth(FractionTiny), height = NotificationDateHeight)
            Spacer(Modifier.height(NotificationLineSpacing))
            ShimmerBox(Modifier.fillMaxWidth(FractionFull), height = NotificationDescriptionHeight)
            Spacer(Modifier.height(2.dp))
            ShimmerBox(Modifier.fillMaxWidth(FractionSmall), height = NotificationDescriptionHeight)
        }
    }
}

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
            .padding(LongreadPadding)
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(AppTheme.colors.surface)
            .padding(LongreadPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShimmerBox(
            Modifier.size(GradeBoxSize),
            height = GradeBoxSize,
            cornerRadius = GradeBoxCornerRadius,
        )
        Spacer(Modifier.width(GradeBoxSpacing))
        Column {
            ShimmerBox(Modifier.width(GradeLabelWidth), height = GradeLabelHeight)
            Spacer(Modifier.height(ExerciseInnerSpacing))
            ShimmerBox(Modifier.width(GradeDescriptionWidth), height = GradeDescriptionHeight)
            Spacer(Modifier.height(ExerciseInnerSpacing))
            ShimmerBox(Modifier.width(GradeNameWidth), height = GradeLabelHeight)
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
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(AppTheme.colors.surface)
            .padding(CardPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBox(Modifier.weight(1f).fillMaxWidth(FractionHalf), height = ExerciseThemeHeight)
            Spacer(Modifier.width(CardPadding))
            ShimmerBox(
                Modifier.width(ExerciseBadgeWidth),
                height = ExerciseBadgeHeight,
                cornerRadius = ChipCornerRadius,
            )
        }
        Spacer(Modifier.height(ExerciseInnerSpacing))
        ShimmerBox(Modifier.fillMaxWidth(FractionLarge), height = ExerciseNameHeight)
        Spacer(Modifier.height(ExerciseInnerSpacing))
        ShimmerBox(Modifier.fillMaxWidth(FractionTiny), height = ExerciseActivityHeight)
    }
}

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
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(AppTheme.colors.surface)
            .padding(LongreadPadding),
    ) {
        ShimmerBox(Modifier.fillMaxWidth(FractionSmall), height = LongreadTitleHeight)
        Spacer(Modifier.height(LongreadTitleSpacing))
        ShimmerBox(Modifier.fillMaxWidth(), height = LongreadLineHeight)
        Spacer(Modifier.height(LongreadLineSpacing))
        ShimmerBox(Modifier.fillMaxWidth(), height = LongreadLineHeight)
        Spacer(Modifier.height(LongreadLineSpacing))
        ShimmerBox(Modifier.fillMaxWidth(FractionMedium), height = LongreadLineHeight)
    }
}

// region Previews

@Preview
@Composable
private fun PreviewDeadlineTaskCardSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            DeadlineTaskCardSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewDeadlineTaskCardSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            DeadlineTaskCardSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewCourseCardSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            CourseCardSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewCourseCardSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            CourseCardSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewClassCardSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            ClassCardSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewClassCardSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            ClassCardSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewCourseListTileSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            CourseListTileSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewCourseListTileSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            CourseListTileSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewNotificationCardSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            NotificationCardSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewNotificationCardSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            NotificationCardSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewTotalGradeCardSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            TotalGradeCardSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewTotalGradeCardSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            TotalGradeCardSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewExerciseTileSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            ExerciseTileSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewExerciseTileSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            ExerciseTileSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewLongreadMaterialSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            LongreadMaterialSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewLongreadMaterialSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(PreviewPadding)) {
            LongreadMaterialSkeleton()
        }
    }
}

// endregion
