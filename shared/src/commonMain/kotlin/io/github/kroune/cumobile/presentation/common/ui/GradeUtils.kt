@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Returns the color for a grade value on a 0-10 scale.
 *
 * Matches the Flutter reference app's color coding:
 * green (>= 8), yellow (>= 6), orange (>= 4), red (< 4).
 */
@Composable
@ReadOnlyComposable
internal fun gradeColor(grade: Int): Color {
    val colors = AppTheme.colors
    return when {
        grade >= 8 -> colors.gradeExcellent
        grade >= 6 -> colors.gradeGood
        grade >= 4 -> colors.gradeSatisfactory
        else -> colors.gradeFail
    }
}

/**
 * Returns a human-readable description for a grade value on a 0-10 scale.
 */
internal fun gradeDescription(grade: Int): String =
    when {
        grade >= 8 -> "Отлично"
        grade >= 6 -> "Хорошо"
        grade >= 4 -> "Удовлетворительно"
        else -> "Неудовлетворительно"
    }
