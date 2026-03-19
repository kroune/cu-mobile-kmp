package io.github.kroune.cumobile.presentation.home

import io.github.kroune.cumobile.domain.repository.CalendarRepository
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository

/**
 * Groups repository dependencies for [DefaultHomeComponent]
 * to keep the constructor parameter count within bounds.
 */
class HomeDependencies(
    val taskRepository: TaskRepository,
    val courseRepository: CourseRepository,
    val profileRepository: ProfileRepository,
    val calendarRepository: CalendarRepository,
)
