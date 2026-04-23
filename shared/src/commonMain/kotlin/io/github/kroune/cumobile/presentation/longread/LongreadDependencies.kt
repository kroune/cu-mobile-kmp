package io.github.kroune.cumobile.presentation.longread

import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.domain.repository.FileRenameRepository
import io.github.kroune.cumobile.domain.repository.QuizRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.kroune.cumobile.util.AppDispatchers

/**
 * Groups repository dependencies for [DefaultLongreadComponent].
 *
 * Fields are [Lazy] so resolving this bundle at the [DetailChildFactory]
 * level doesn't eagerly instantiate three repositories for a longread
 * the user might back out of immediately.
 */
class LongreadDependencies(
    val contentRepository: Lazy<ContentRepository>,
    val taskRepository: Lazy<TaskRepository>,
    val quizRepository: Lazy<QuizRepository>,
    val renameRepository: Lazy<FileRenameRepository>,
    val dispatchers: Lazy<AppDispatchers>,
)
