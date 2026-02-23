@file:Suppress("MatchingDeclarationName", "MagicNumber")

package io.github.kroune.cumobile.presentation.common

import androidx.compose.ui.graphics.Color
import io.github.kroune.cumobile.data.model.TaskState

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

    // Task state colors (matching Flutter reference exactly)
    val TaskBacklog = Color(0xFF9E9E9E)
    val TaskInProgress = Color(0xFF42A5F5)
    val TaskRevision = Color(0xFFEF5350)
    val TaskRework = Color(0xFFEF5350)
    val TaskReview = Color(0xFFFFA726)
    val TaskHasSolution = Color(0xFF00E676)
    val TaskFailed = Color(0xFFEF5350)
    val TaskEvaluated = Color(0xFF00E676)

    // Grade colors
    val GradeExcellent = Color(0xFF00E676)
    val GradeGood = Color(0xFFFFCA28)
    val GradeSatisfactory = Color(0xFFFF9800)
    val GradeFail = Color(0xFFEF5350)

    // Level score colors (longread)
    val LevelBasic = Color(0xFF3044FF)
    val LevelMedium = Color(0xFFE63F07)

    // Additional surface colors
    val CodeBlockBackground = Color(0xFF2A2A2A)
    val BlockquoteBackground = Color(0xFF1A1A2E)

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
        TaskState.Backlog -> "Не начато"
        TaskState.InProgress -> "В работе"
        TaskState.Revision, TaskState.Rework -> "Доработка"
        TaskState.Review -> "На проверке"
        TaskState.HasSolution -> "Есть решение"
        TaskState.Failed, TaskState.Rejected -> "Не сдано"
        TaskState.Evaluated -> "Проверено"
        else -> state
    }

/**
 * Returns the color for a task state indicator.
 */
fun taskStateColor(state: String): Color =
    when (state) {
        TaskState.Backlog -> AppColors.TaskBacklog
        TaskState.InProgress -> AppColors.TaskInProgress
        TaskState.Revision -> AppColors.TaskRevision
        TaskState.Rework -> AppColors.TaskRework
        TaskState.Review -> AppColors.TaskReview
        TaskState.HasSolution -> AppColors.TaskHasSolution
        TaskState.Failed, TaskState.Rejected -> AppColors.TaskFailed
        TaskState.Evaluated -> AppColors.TaskEvaluated
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
