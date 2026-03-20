package io.github.kroune.cumobile.presentation.main

import io.github.kroune.cumobile.data.local.FileOpener
import io.github.kroune.cumobile.data.local.FileStorage
import io.github.kroune.cumobile.data.local.PdfGenerator
import io.github.kroune.cumobile.data.network.UpdateChecker
import io.github.kroune.cumobile.domain.repository.CalendarRepository
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.FileRenameRepository
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
    val fileRenameRepository: FileRenameRepository,
    val calendarRepository: CalendarRepository,
    val fileOpener: FileOpener,
    val updateChecker: UpdateChecker,
    val pdfGenerator: PdfGenerator,
    val fileStorage: FileStorage,
)
