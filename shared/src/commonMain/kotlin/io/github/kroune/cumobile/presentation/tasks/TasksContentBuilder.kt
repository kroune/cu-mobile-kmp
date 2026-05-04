package io.github.kroune.cumobile.presentation.tasks

import io.github.kroune.cumobile.data.model.mappers.toApiValue
import io.github.kroune.cumobile.domain.model.TaskDomain
import io.github.kroune.cumobile.domain.model.TaskStatus
import io.github.kroune.cumobile.presentation.common.model.TaskUi
import io.github.kroune.cumobile.presentation.common.model.label
import io.github.kroune.cumobile.presentation.common.model.mappers.toUi
import io.github.kroune.cumobile.presentation.common.model.toStatusStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Instant

/**
 * Pairs a [TaskDomain] with its effective [TaskStatus] so we
 * don't recompute the status for the same task during filtering and sorting.
 */
private data class IndexedTask(
    val task: TaskDomain,
    val status: TaskStatus,
)

private val BottomStatuses = setOf(
    TaskStatus.Evaluated,
    TaskStatus.Failed,
    TaskStatus.Rejected,
    TaskStatus.Review,
)

/**
 * Builds [TasksComponent.Content] from the raw task list and current filters.
 *
 * Runs as a single pass over [allTasks]:
 * - Segregates tasks into active/archive buckets in one sweep.
 * - Filters each bucket by status/course/search and sorts by bottom-state +
 *   deadline.
 *
 * Pure function — safe to call on [kotlinx.coroutines.Dispatchers.Default].
 */
internal fun buildTasksContent(
    allTasks: List<TaskDomain>,
    segment: Int,
    statusFilter: String?,
    courseFilter: String?,
    searchQuery: String,
    now: Instant,
): TasksComponent.Content {
    if (allTasks.isEmpty()) return TasksComponent.Content()

    val active = ArrayList<IndexedTask>(allTasks.size)
    val archive = ArrayList<IndexedTask>(allTasks.size)
    val coursePairs = LinkedHashMap<String, String>()

    for (task in allTasks) {
        val indexed = IndexedTask(task, task.status)
        if (task.status.isActive) {
            active.add(indexed)
        } else {
            archive.add(indexed)
        }
        if (task.courseId !in coursePairs) {
            coursePairs[task.courseId] = task.courseName
        }
    }

    val availableStatuses = collectAvailableStatuses(segment, active, archive)
    val availableCourses = coursePairs.entries
        .map { it.key to it.value }
        .sortedBy { it.second }
        .toImmutableList()

    return TasksComponent.Content(
        activeFilteredTasks = active.filterAndSort(statusFilter, courseFilter, searchQuery, now),
        archiveFilteredTasks = archive.filterAndSort(statusFilter, courseFilter, searchQuery, now),
        activeCount = active.size,
        archiveCount = archive.size,
        availableCourses = availableCourses,
        availableStatuses = availableStatuses,
    )
}

private fun collectAvailableStatuses(
    segment: Int,
    active: List<IndexedTask>,
    archive: List<IndexedTask>,
): ImmutableList<Pair<String, String>> {
    val bucket = if (segment == 0) active else archive
    val present = bucket.mapTo(mutableSetOf()) { it.status.toApiValue() }
    val segmentStatuses = if (segment == 0) {
        TaskStatus.ACTIVE_STATUSES
    } else {
        TaskStatus.ARCHIVE_STATUSES
    }
    return segmentStatuses
        .filter { it.toApiValue() in present }
        .map { status ->
            val apiValue = status.toApiValue()
            apiValue to status.toStatusStyle().label()
        }.toImmutableList()
}

private fun List<IndexedTask>.filterAndSort(
    statusFilter: String?,
    courseFilter: String?,
    searchQuery: String,
    now: Instant,
): ImmutableList<TaskUi> {
    if (isEmpty()) return persistentListOf()
    val query = searchQuery.takeIf { it.isNotEmpty() }
    val filtered = filter { item ->
        (statusFilter == null || item.status.toApiValue() == statusFilter) &&
            (courseFilter == null || item.task.courseId == courseFilter) &&
            (
                query == null ||
                    item.task.exerciseName
                        .contains(query, ignoreCase = true)
            )
    }
    if (filtered.isEmpty()) return persistentListOf()
    return filtered
        .sortedWith(indexedTaskComparator())
        .map { it.task.toUi(now) }
        .toImmutableList()
}

private fun indexedTaskComparator(): Comparator<IndexedTask> =
    Comparator { a, b ->
        val aBottom = a.status in BottomStatuses
        val bBottom = b.status in BottomStatuses
        if (aBottom != bBottom) {
            return@Comparator if (aBottom) 1 else -1
        }
        val deadlineA = a.task.deadline ?: a.task.exerciseDeadline
        val deadlineB = b.task.deadline ?: b.task.exerciseDeadline
        when {
            deadlineA == null && deadlineB == null -> 0
            deadlineA == null -> 1
            deadlineB == null -> -1
            else -> deadlineA.compareTo(deadlineB)
        }
    }
