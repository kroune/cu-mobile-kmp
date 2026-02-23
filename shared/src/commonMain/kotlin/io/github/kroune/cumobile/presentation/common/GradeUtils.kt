@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.common

import androidx.compose.ui.graphics.Color

/**
 * Returns the color for a grade value on a 0-10 scale.
 *
 * Matches the Flutter reference app's color coding:
 * green (>= 8), yellow (>= 6), orange (>= 4), red (< 4).
 */
internal fun gradeColor(grade: Int): Color =
    when {
        grade >= 8 -> AppColors.GradeExcellent
        grade >= 6 -> AppColors.GradeGood
        grade >= 4 -> AppColors.GradeSatisfactory
        else -> AppColors.GradeFail
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
