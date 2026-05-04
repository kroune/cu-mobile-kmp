package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.CourseDomain
import io.github.kroune.cumobile.presentation.common.model.CourseUi
import io.github.kroune.cumobile.presentation.common.model.label
import io.github.kroune.cumobile.presentation.common.model.toCategoryStyle
import io.github.kroune.cumobile.presentation.common.ui.stripEmojiPrefix

fun CourseDomain.toUi(): CourseUi {
    val style = category.toCategoryStyle()
    return CourseUi(
        id = id,
        name = stripEmojiPrefix(name),
        isArchived = isArchived,
        categoryLabel = style.label(),
        categoryStyle = style,
    )
}
