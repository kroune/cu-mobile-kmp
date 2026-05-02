package io.github.kroune.cumobile.presentation.common.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import io.github.kroune.cumobile.domain.model.CourseCategory
import io.github.kroune.cumobile.presentation.common.ui.AppTheme

enum class CategoryStyle {
    General,
    Mathematics,
    Development,
    Stem,
    Business,
    SoftSkills,
    Unknown,
}

fun CategoryStyle.label(): String =
    when (this) {
        CategoryStyle.General -> "Общее"
        CategoryStyle.Mathematics -> "Математика"
        CategoryStyle.Development -> "Разработка"
        CategoryStyle.Stem -> "Наука"
        CategoryStyle.Business -> "Бизнес"
        CategoryStyle.SoftSkills -> "Soft Skills"
        CategoryStyle.Unknown -> "Без категории"
    }

@Composable
@ReadOnlyComposable
fun CategoryStyle.color(): Color {
    val colors = AppTheme.colors
    return when (this) {
        CategoryStyle.General -> colors.categoryGeneral
        CategoryStyle.Mathematics -> colors.categoryMathematics
        CategoryStyle.Development -> colors.categoryDevelopment
        CategoryStyle.Stem -> colors.categoryStem
        CategoryStyle.Business -> colors.categoryBusiness
        CategoryStyle.SoftSkills -> colors.categorySoftSkills
        CategoryStyle.Unknown -> colors.categoryDefault
    }
}

fun CourseCategory.toCategoryStyle(): CategoryStyle =
    when (this) {
        CourseCategory.General -> CategoryStyle.General
        CourseCategory.Mathematics -> CategoryStyle.Mathematics
        CourseCategory.Development -> CategoryStyle.Development
        CourseCategory.Stem -> CategoryStyle.Stem
        CourseCategory.Business -> CategoryStyle.Business
        CourseCategory.SoftSkills -> CategoryStyle.SoftSkills
        CourseCategory.Unknown -> CategoryStyle.Unknown
    }
