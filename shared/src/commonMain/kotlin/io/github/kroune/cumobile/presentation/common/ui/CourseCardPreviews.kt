@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kroune.cumobile.data.model.Course

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
