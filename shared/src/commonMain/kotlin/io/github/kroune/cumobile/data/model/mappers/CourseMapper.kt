package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.CourseApi
import io.github.kroune.cumobile.domain.model.CourseDomain

fun CourseApi.toDomain(): CourseDomain =
    CourseDomain(
        id = id,
        name = name,
        isArchived = isArchived,
        category = category.toCourseCategory(),
        categoryCoverUrl = categoryCover,
    )
