package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kroune.cumobile.domain.model.CourseCategory
import io.github.kroune.cumobile.domain.model.CourseDomain
import io.github.kroune.cumobile.presentation.common.model.mappers.toUi

@Preview
@Composable
private fun PreviewCourseCardDark() {
    CuMobileTheme(darkTheme = true) {
        Box(Modifier.background(AppTheme.colors.background).padding(16.dp)) {
            CourseCard(
                course = CourseDomain(
                    id = "",
                    name = "Линейная алгебра",
                    isArchived = false,
                    category = CourseCategory.Mathematics,
                    categoryCoverUrl = "",
                ).toUi(),
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
                course = CourseDomain(
                    id = "",
                    name = "Линейная алгебра",
                    isArchived = false,
                    category = CourseCategory.Mathematics,
                    categoryCoverUrl = "",
                ).toUi(),
                onClick = {},
            )
        }
    }
}
