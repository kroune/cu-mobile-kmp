package io.github.kroune.cumobile.presentation.home

import io.github.kroune.cumobile.domain.repository.CalendarRepository
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository

/**
 * Groups repository dependencies for [DefaultHomeComponent].
 *
 * Fields are [Lazy] so a tab that never runs its `loadData()` (e.g. the
 * user leaves the app before the Home lifecycle starts) doesn't
 * instantiate the underlying repositories.
 */
class HomeDependencies(
    val taskRepository: Lazy<TaskRepository>,
    val courseRepository: Lazy<CourseRepository>,
    val profileRepository: Lazy<ProfileRepository>,
    val calendarRepository: Lazy<CalendarRepository>,
)
