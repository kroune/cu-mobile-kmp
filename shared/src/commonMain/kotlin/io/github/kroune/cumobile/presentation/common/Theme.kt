@file:Suppress("MatchingDeclarationName", "MagicNumber")

package io.github.kroune.cumobile.presentation.common

import androidx.compose.ui.graphics.Color

/**
 * App color palette matching the Flutter reference app.
 *
 * Dark theme with green accent, based on the CU LMS design.
 */
object AppColors {
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val Accent = Color(0xFF00E676)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9E9E9E)
    val Error = Color(0xFFEF5350)

    // Task state colors (matching Flutter reference)
    val TaskBacklog = Color(0xFF9E9E9E)
    val TaskInProgress = Color(0xFF42A5F5)
    val TaskRevision = Color(0xFFFFA726)
    val TaskRework = Color(0xFFFFA726)
    val TaskReview = Color(0xFF7E57C2)
    val TaskHasSolution = Color(0xFF26A69A)
    val TaskFailed = Color(0xFFEF5350)
    val TaskEvaluated = Color(0xFF66BB6A)

    // Course category colors
    val CategoryMathematics = Color(0xFF42A5F5)
    val CategoryDevelopment = Color(0xFF66BB6A)
    val CategoryStem = Color(0xFF7E57C2)
    val CategoryGeneral = Color(0xFF9E9E9E)
    val CategoryBusiness = Color(0xFFFFA726)
    val CategorySoftSkills = Color(0xFFEC407A)
    val CategoryDefault = Color(0xFF9E9E9E)
}

/**
 * Returns the display label for a task state.
 */
fun taskStateLabel(state: String): String =
    when (state) {
        "backlog" -> "Не начато"
        "inProgress" -> "В работе"
        "revision", "rework" -> "Доработка"
        "review" -> "На проверке"
        "hasSolution" -> "Есть решение"
        "failed", "rejected" -> "Не сдано"
        "evaluated" -> "Проверено"
        else -> state
    }

/**
 * Returns the color for a task state indicator.
 */
fun taskStateColor(state: String): Color =
    when (state) {
        "backlog" -> AppColors.TaskBacklog
        "inProgress" -> AppColors.TaskInProgress
        "revision" -> AppColors.TaskRevision
        "rework" -> AppColors.TaskRework
        "review" -> AppColors.TaskReview
        "hasSolution" -> AppColors.TaskHasSolution
        "failed", "rejected" -> AppColors.TaskFailed
        "evaluated" -> AppColors.TaskEvaluated
        else -> AppColors.TaskBacklog
    }

/**
 * Returns the display label for a course category.
 */
fun courseCategoryLabel(category: String): String =
    when (category) {
        "mathematics" -> "Математика"
        "development" -> "Разработка"
        "stem" -> "Наука"
        "general" -> "Общее"
        "business" -> "Бизнес"
        "softSkills" -> "Soft Skills"
        else -> "Без категории"
    }

/**
 * Returns the color for a course category.
 */
fun courseCategoryColor(category: String): Color =
    when (category) {
        "mathematics" -> AppColors.CategoryMathematics
        "development" -> AppColors.CategoryDevelopment
        "stem" -> AppColors.CategoryStem
        "general" -> AppColors.CategoryGeneral
        "business" -> AppColors.CategoryBusiness
        "softSkills" -> AppColors.CategorySoftSkills
        else -> AppColors.CategoryDefault
    }

/**
 * Strips common emoji prefixes from course names.
 *
 * The API sometimes prepends emoji icons like "📐 " or "💻 " to names.
 */
fun stripEmojiPrefix(name: String): String {
    if (name.isEmpty()) return name
    val firstCodePoint = name.codePointAt(0)
    // Skip if first character is in emoji ranges (above basic latin/cyrillic)
    return if (firstCodePoint > 0x2600) {
        name.dropWhile { it.code > 0x2600 || it == ' ' }.ifEmpty { name }
    } else {
        name
    }
}
