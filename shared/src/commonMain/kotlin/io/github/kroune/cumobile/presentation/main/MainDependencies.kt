package io.github.kroune.cumobile.presentation.main

import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.FileRepository
import io.github.kroune.cumobile.domain.repository.NotificationRepository
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository

/**
 * Groups all main-flow repository dependencies into a single container.
 *
 * Reduces constructor parameter counts in [DefaultMainComponent]
 * and [io.github.kroune.cumobile.presentation.root.DefaultRootComponent].
 */
data class MainDependencies(
    val taskRepository: TaskRepository,
    val courseRepository: CourseRepository,
    val profileRepository: ProfileRepository,
    val performanceRepository: PerformanceRepository,
    val contentRepository: ContentRepository,
    val notificationRepository: NotificationRepository,
    val fileRepository: FileRepository,
)
