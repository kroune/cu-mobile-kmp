package io.github.kroune.cumobile.presentation.tasks

import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Pairs a [StudentTask] with its normalized effective state so we
 * don't recompute [effectiveTaskState] / [normalizeTaskState] for
 * the same task during filtering and sorting.
 */
private data class IndexedTask(
    val task: StudentTask,
    val effectiveState: String,
)

private val BottomStates = setOf(
    TaskState.Evaluated,
    TaskState.Failed,
    TaskState.Rejected,
    TaskState.Review,
)

/**
 * Builds [TasksComponent.Content] from the raw task list and current filters.
 *
 * Runs as a single pass over [allTasks]:
 * - Precomputes effective state per task so filtering and sorting reuse it.
 * - Segregates tasks into active/archive buckets in one sweep.
 * - Filters each bucket by status/course/search and sorts by bottom-state +
 *   deadline.
 *
 * Pure function — safe to call on [kotlinx.coroutines.Dispatchers.Default].
 */
internal fun buildTasksContent(
    allTasks: List<StudentTask>,
    segment: Int,
    statusFilter: String?,
    courseFilter: String?,
    searchQuery: String,
): TasksComponent.Content {
    if (allTasks.isEmpty()) return TasksComponent.Content()

    val active = ArrayList<IndexedTask>(allTasks.size)
    val archive = ArrayList<IndexedTask>(allTasks.size)
    val coursePairs = LinkedHashMap<String, String>()

    for (task in allTasks) {
        val effective = normalizeTaskState(effectiveTaskState(task))
        val indexed = IndexedTask(task, effective)
        when (effective) {
            in ActiveStates -> active.add(indexed)
            in ArchiveStates -> archive.add(indexed)
            else -> Unit
        }
        if (task.course.id !in coursePairs) {
            coursePairs[task.course.id] = task.course.name
        }
    }

    val availableStatuses = collectAvailableStatuses(segment, active, archive)
    val availableCourses = coursePairs.entries
        .map { it.key to it.value }
        .sortedBy { it.second }
        .toImmutableList()

    return TasksComponent.Content(
        activeFilteredTasks = active.filterAndSort(statusFilter, courseFilter, searchQuery),
        archiveFilteredTasks = archive.filterAndSort(statusFilter, courseFilter, searchQuery),
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
): ImmutableList<String> {
    val segmentStates = if (segment == 0) ActiveStates else ArchiveStates
    val bucket = if (segment == 0) active else archive
    val present = bucket.mapTo(mutableSetOf()) { it.effectiveState }
    return segmentStates
        .filter { it in present }
        .toImmutableList()
}

private fun List<IndexedTask>.filterAndSort(
    statusFilter: String?,
    courseFilter: String?,
    searchQuery: String,
): ImmutableList<StudentTask> {
    if (isEmpty()) return persistentListOf()
    val query = searchQuery.takeIf { it.isNotEmpty() }
    val filtered = filter { item ->
        (statusFilter == null || item.effectiveState == statusFilter) &&
            (courseFilter == null || item.task.course.id == courseFilter) &&
            (
                query == null ||
                    item.task.exercise.name
                        .contains(query, ignoreCase = true)
            )
    }
    if (filtered.isEmpty()) return persistentListOf()
    return filtered
        .sortedWith(indexedTaskComparator())
        .map { it.task }
        .toImmutableList()
}

private fun indexedTaskComparator(): Comparator<IndexedTask> =
    Comparator { a, b ->
        val aBottom = a.effectiveState in BottomStates
        val bBottom = b.effectiveState in BottomStates
        if (aBottom != bBottom) {
            return@Comparator if (aBottom) 1 else -1
        }
        val deadlineA = a.task.deadline ?: a.task.exercise.deadline
        val deadlineB = b.task.deadline ?: b.task.exercise.deadline
        when {
            deadlineA == null && deadlineB == null -> 0
            deadlineA == null -> 1
            deadlineB == null -> -1
            else -> deadlineA.compareTo(deadlineB)
        }
    }
