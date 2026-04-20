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
import io.github.kroune.cumobile.util.AppDispatchers

/**
 * Groups all main-flow repository dependencies into a single container.
 *
 * Fields are [Lazy] so downstream consumers that only ever touch a
 * subset (e.g. the Home tab never opens the Scanner) don't pay the
 * instantiation cost for the ones they skip. Hand out via [TabChildFactory]
 * / [DetailChildFactory] which forward the specific [Lazy] fields each
 * child needs.
 */
data class MainDependencies(
    val taskRepository: Lazy<TaskRepository>,
    val courseRepository: Lazy<CourseRepository>,
    val profileRepository: Lazy<ProfileRepository>,
    val performanceRepository: Lazy<PerformanceRepository>,
    val contentRepository: Lazy<ContentRepository>,
    val notificationRepository: Lazy<NotificationRepository>,
    val fileRepository: Lazy<FileRepository>,
    val fileRenameRepository: Lazy<FileRenameRepository>,
    val calendarRepository: Lazy<CalendarRepository>,
    val fileOpener: Lazy<FileOpener>,
    val updateChecker: Lazy<UpdateChecker>,
    val pdfGenerator: Lazy<PdfGenerator>,
    val fileStorage: Lazy<FileStorage>,
    val dispatchers: Lazy<AppDispatchers>,
)
