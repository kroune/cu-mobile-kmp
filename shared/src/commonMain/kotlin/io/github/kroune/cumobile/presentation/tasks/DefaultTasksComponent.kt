package io.github.kroune.cumobile.presentation.tasks

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.domain.repository.TaskRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Default implementation of [TasksComponent].
 *
 * Loads tasks from the API, applies segment/status/course/search
 * filters locally, and sorts by deadline. Delegates task-open
 * navigation to the parent component via [onOpenTask].
 */
class DefaultTasksComponent(
    componentContext: ComponentContext,
    private val taskRepository: TaskRepository,
    private val onOpenTask: (StudentTask) -> Unit,
) : TasksComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(TasksComponent.State())
    override val state: Value<TasksComponent.State> = _state

    override fun onIntent(intent: TasksComponent.Intent) {
        when (intent) {
            is TasksComponent.Intent.SelectSegment ->
                _state.value = _state.value.copy(
                    segment = intent.index,
                    statusFilter = null,
                )
            is TasksComponent.Intent.FilterByStatus ->
                _state.value = _state.value.copy(
                    statusFilter = intent.status,
                )
            is TasksComponent.Intent.FilterByCourse ->
                _state.value = _state.value.copy(
                    courseFilter = intent.courseId,
                )
            is TasksComponent.Intent.Search ->
                _state.value = _state.value.copy(
                    searchQuery = intent.query,
                )
            is TasksComponent.Intent.OpenTask ->
                onOpenTask(intent.task)
            TasksComponent.Intent.Refresh ->
                loadTasks()
        }
    }

    init {
        loadTasks()
    }

    private fun loadTasks() {
        scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
            )
            val result = taskRepository.fetchTasks(AllApiStates)
            if (result != null) {
                _state.value = _state.value.copy(
                    allTasks = result.toImmutableList(),
                    isLoading = false,
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить задания",
                )
            }
        }
    }
}
