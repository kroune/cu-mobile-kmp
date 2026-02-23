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
        grade >= 8 -> Color(0xFF66BB6A)
        grade >= 6 -> Color(0xFFFFEE58)
        grade >= 4 -> Color(0xFFFFA726)
        else -> Color(0xFFEF5350)
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
