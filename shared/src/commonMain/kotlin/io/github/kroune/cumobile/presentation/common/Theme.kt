@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.kroune.cumobile.data.model.TaskState

/**
 * Complete color palette for the CuMobile app.
 *
 * Theme-varying colors (background, surface, text, etc.) differ between
 * dark and light themes. Semantic colors (task states, grades, categories)
 * stay the same in both themes.
 */
data class AppColorScheme(
    // Theme-varying
    val background: Color,
    val surface: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val error: Color,
    val codeBlockBackground: Color,
    val blockquoteBackground: Color,
    // Task state (semantic)
    val taskBacklog: Color,
    val taskInProgress: Color,
    val taskRevision: Color,
    val taskRework: Color,
    val taskReview: Color,
    val taskHasSolution: Color,
    val taskFailed: Color,
    val taskEvaluated: Color,
    // Grade (semantic)
    val gradeExcellent: Color,
    val gradeGood: Color,
    val gradeSatisfactory: Color,
    val gradeFail: Color,
    // Level score (semantic)
    val levelBasic: Color,
    val levelMedium: Color,
    // Course category (semantic)
    val categoryMathematics: Color,
    val categoryDevelopment: Color,
    val categoryStem: Color,
    val categoryGeneral: Color,
    val categoryBusiness: Color,
    val categorySoftSkills: Color,
    val categoryDefault: Color,
)

// Dark theme task colors (bright, for dark backgrounds)
private val darkTaskBacklog = Color(0xFF9E9E9E)
private val darkTaskInProgress = Color(0xFF42A5F5)
private val darkTaskRevision = Color(0xFFEF5350)
private val darkTaskRework = Color(0xFFEF5350)
private val darkTaskReview = Color(0xFFFFA726)
private val darkTaskHasSolution = Color(0xFF00E676)
private val darkTaskFailed = Color(0xFFEF5350)
private val darkTaskEvaluated = Color(0xFF00E676)

// Light theme task colors (darker shades for contrast on light backgrounds)
private val lightTaskBacklog = Color(0xFF757575)
private val lightTaskInProgress = Color(0xFF1976D2)
private val lightTaskRevision = Color(0xFFD32F2F)
private val lightTaskRework = Color(0xFFD32F2F)
private val lightTaskReview = Color(0xFFEF6C00)
private val lightTaskHasSolution = Color(0xFF2E7D32)
private val lightTaskFailed = Color(0xFFD32F2F)
private val lightTaskEvaluated = Color(0xFF2E7D32)

private val gradeExcellent = Color(0xFF00E676)
private val gradeGood = Color(0xFFFFCA28)
private val gradeSatisfactory = Color(0xFFFF9800)
private val gradeFail = Color(0xFFEF5350)

private val levelBasic = Color(0xFF3044FF)
private val levelMedium = Color(0xFFE63F07)

private val categoryMathematics = Color(0xFF42A5F5)
private val categoryDevelopment = Color(0xFF66BB6A)
private val categoryStem = Color(0xFF7E57C2)
private val categoryGeneral = Color(0xFF9E9E9E)
private val categoryBusiness = Color(0xFFFFA726)
private val categorySoftSkills = Color(0xFFEC407A)
private val categoryDefault = Color(0xFF9E9E9E)

/** Dark theme palette (matching the CU LMS design). */
val DarkAppColors = AppColorScheme(
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    accent = Color(0xFF00E676),
    textPrimary = Color.White,
    textSecondary = Color(0xFF9E9E9E),
    error = Color(0xFFEF5350),
    codeBlockBackground = Color(0xFF2A2A2A),
    blockquoteBackground = Color(0xFF1A1A2E),
    taskBacklog = darkTaskBacklog,
    taskInProgress = darkTaskInProgress,
    taskRevision = darkTaskRevision,
    taskRework = darkTaskRework,
    taskReview = darkTaskReview,
    taskHasSolution = darkTaskHasSolution,
    taskFailed = darkTaskFailed,
    taskEvaluated = darkTaskEvaluated,
    gradeExcellent = gradeExcellent,
    gradeGood = gradeGood,
    gradeSatisfactory = gradeSatisfactory,
    gradeFail = gradeFail,
    levelBasic = levelBasic,
    levelMedium = levelMedium,
    categoryMathematics = categoryMathematics,
    categoryDevelopment = categoryDevelopment,
    categoryStem = categoryStem,
    categoryGeneral = categoryGeneral,
    categoryBusiness = categoryBusiness,
    categorySoftSkills = categorySoftSkills,
    categoryDefault = categoryDefault,
)

/** Light theme palette. */
val LightAppColors = AppColorScheme(
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    accent = Color(0xFF007B32),
    textPrimary = Color(0xFF1B1B1B),
    textSecondary = Color(0xFF666666),
    error = Color(0xFFD32F2F),
    codeBlockBackground = Color(0xFFEEEEEE),
    blockquoteBackground = Color(0xFFE8EAF6),
    taskBacklog = lightTaskBacklog,
    taskInProgress = lightTaskInProgress,
    taskRevision = lightTaskRevision,
    taskRework = lightTaskRework,
    taskReview = lightTaskReview,
    taskHasSolution = lightTaskHasSolution,
    taskFailed = lightTaskFailed,
    taskEvaluated = lightTaskEvaluated,
    gradeExcellent = gradeExcellent,
    gradeGood = gradeGood,
    gradeSatisfactory = gradeSatisfactory,
    gradeFail = gradeFail,
    levelBasic = levelBasic,
    levelMedium = levelMedium,
    categoryMathematics = categoryMathematics,
    categoryDevelopment = categoryDevelopment,
    categoryStem = categoryStem,
    categoryGeneral = categoryGeneral,
    categoryBusiness = categoryBusiness,
    categorySoftSkills = categorySoftSkills,
    categoryDefault = categoryDefault,
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

/** App-wide theme accessor. Use [AppTheme.colors] inside composables. */
object AppTheme {
    val colors: AppColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

// Material 3 color schemes wired to AppColorScheme
private val DarkMaterialColors = darkColorScheme(
    background = DarkAppColors.background,
    surface = DarkAppColors.surface,
    primary = DarkAppColors.accent,
    onBackground = DarkAppColors.textPrimary,
    onSurface = DarkAppColors.textPrimary,
    error = DarkAppColors.error,
)

private val LightMaterialColors = lightColorScheme(
    background = LightAppColors.background,
    surface = LightAppColors.surface,
    primary = LightAppColors.accent,
    onBackground = LightAppColors.textPrimary,
    onSurface = LightAppColors.textPrimary,
    error = LightAppColors.error,
)

/**
 * Wraps content with both [MaterialTheme] and the custom [AppTheme].
 *
 * Used in [io.github.kroune.cumobile.App] and in `@Preview` functions.
 */
@Composable
fun CuMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val materialColors = if (darkTheme) DarkMaterialColors else LightMaterialColors
    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(colorScheme = materialColors, content = content)
    }
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
@Composable
@ReadOnlyComposable
fun taskStateColor(state: String): Color {
    val colors = AppTheme.colors
    return when (state) {
        TaskState.Backlog -> colors.taskBacklog
        TaskState.InProgress -> colors.taskInProgress
        TaskState.Revision -> colors.taskRevision
        TaskState.Rework -> colors.taskRework
        TaskState.Review -> colors.taskReview
        TaskState.HasSolution -> colors.taskHasSolution
        TaskState.Failed, TaskState.Rejected -> colors.taskFailed
        TaskState.Evaluated -> colors.taskEvaluated
        else -> colors.taskBacklog
    }
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
@Composable
@ReadOnlyComposable
fun courseCategoryColor(category: String): Color {
    val colors = AppTheme.colors
    return when (category) {
        "mathematics" -> colors.categoryMathematics
        "development" -> colors.categoryDevelopment
        "stem" -> colors.categoryStem
        "general" -> colors.categoryGeneral
        "business" -> colors.categoryBusiness
        "softSkills" -> colors.categorySoftSkills
        else -> colors.categoryDefault
    }
}

/**
 * Strips common emoji prefixes from course names.
 *
 * The API sometimes prepends emoji icons like "📐 " or "💻 " to names.
 */
fun stripEmojiPrefix(name: String): String {
    if (name.isEmpty()) return name
    val firstCharCode = name[0].code
    // Skip if first character is in emoji/symbols range (common in the LMS)
    return if (firstCharCode > 0x2000) {
        name.dropWhile { it.code > 0x2000 || it == ' ' }.ifEmpty { name }
    } else {
        name
    }
}
