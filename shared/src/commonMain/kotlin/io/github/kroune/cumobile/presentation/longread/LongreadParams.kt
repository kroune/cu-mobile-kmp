package io.github.kroune.cumobile.presentation.longread

/**
 * Groups longread identification parameters into a single container.
 *
 * Reduces constructor parameter count in [DefaultLongreadComponent].
 */
data class LongreadParams(
    val longreadId: Int,
    val courseId: Int,
    val themeId: Int,
)
