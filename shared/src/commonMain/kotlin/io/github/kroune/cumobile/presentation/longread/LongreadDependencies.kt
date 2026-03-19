package io.github.kroune.cumobile.presentation.longread

import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.FileRenameRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository

/**
 * Groups repository dependencies for [DefaultLongreadComponent]
 * to keep the constructor parameter count within bounds.
 */
class LongreadDependencies(
    val contentRepository: ContentRepository,
    val taskRepository: TaskRepository,
    val renameRepository: FileRenameRepository,
)
