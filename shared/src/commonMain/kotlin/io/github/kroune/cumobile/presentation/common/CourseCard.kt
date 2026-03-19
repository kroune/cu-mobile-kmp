package io.github.kroune.cumobile.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroune.cumobile.data.model.Course
import androidx.compose.ui.tooling.preview.Preview

/**
 * Course card for the home screen grid.
 *
 * Shows the course name and category label with a colored chip.
 * Matches the Flutter reference: rounded corners, dark surface,
 * aspect ratio ~1.4 (handled by the grid).
 *
 * @param course The course to display.
 * @param onClick Called when the card is tapped.
 */
@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = courseCategoryColor(course.category)
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.surface, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Course name
        Text(
            text = stripEmojiPrefix(course.name),
            color = AppTheme.colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        // Category chip
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(categoryColor.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = courseCategoryLabel(course.category),
                color = categoryColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewCourseCardDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
            CourseCard(
                course = Course(name = "Линейная алгебра", category = "mathematics"),
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewCourseCardLight() {
    CuMobileTheme(darkTheme = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
            CourseCard(
                course = Course(name = "Линейная алгебра", category = "mathematics"),
                onClick = {},
            )
        }
    }
}
