package io.github.kroune.cumobile.presentation.longread

/**
 * Groups longread identification parameters into a single container.
 *
 * Reduces constructor parameter count in [DefaultLongreadComponent].
 */
data class LongreadParams(
    val longreadId: String,
    val courseId: String,
    val themeId: String,
    val focusTaskId: String? = null,
)
