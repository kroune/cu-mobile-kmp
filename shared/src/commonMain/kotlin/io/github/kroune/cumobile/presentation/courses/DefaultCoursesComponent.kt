package io.github.kroune.cumobile.presentation.courses

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [CoursesComponent].
 *
 * Loads courses, performance data, and gradebook from repositories in parallel.
 * Delegates course navigation to the parent via callbacks.
 */
class DefaultCoursesComponent(
    componentContext: ComponentContext,
    private val courseRepository: CourseRepository,
    private val performanceRepository: PerformanceRepository,
    private val onOpenCourse: (courseId: String) -> Unit,
    private val onOpenCoursePerformance: (courseId: String, courseName: String, totalGrade: Int) -> Unit,
) : CoursesComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(CoursesComponent.State())
    override val state: Value<CoursesComponent.State> = _state

    private val _effects = Channel<CoursesComponent.Effect>(Channel.BUFFERED)
    override val effects: Flow<CoursesComponent.Effect> = _effects.receiveAsFlow()

    private var currentLoadJob: Job? = null

    override fun onIntent(intent: CoursesComponent.Intent) {
        when (intent) {
            is CoursesComponent.Intent.SelectSegment ->
                _state.value = _state.value.copy(segment = intent.index)
            CoursesComponent.Intent.ToggleActive ->
                _state.value = _state.value.copy(
                    showActive = !_state.value.showActive,
                )
            CoursesComponent.Intent.ToggleArchived ->
                _state.value = _state.value.copy(
                    showArchived = !_state.value.showArchived,
                )
            is CoursesComponent.Intent.OpenCourse ->
                onOpenCourse(intent.courseId)
            is CoursesComponent.Intent.OpenCoursePerformance ->
                onOpenCoursePerformance(
                    intent.courseId,
                    intent.courseName,
                    intent.totalGrade,
                )
            CoursesComponent.Intent.Refresh ->
                loadAllData()
            is CoursesComponent.Intent.ReorderCourses ->
                reorderCourses(intent.ids)
        }
    }

    init {
        loadAllData()
        observeOrder()
    }

    private fun observeOrder() {
        courseRepository.courseIdOrderFlow
            .onEach { order ->
                _state.value = _state.value.copy(courseOrder = order)
            }.launchIn(scope)
    }

    private fun reorderCourses(ids: List<String>) {
        scope.launch {
            courseRepository.saveCourseIdOrder(ids)
        }
    }

    private fun loadAllData() {
        currentLoadJob?.cancel()

        _state.value = _state.value.copy(
            courses = ContentState.Loading,
            performanceCourses = ContentState.Loading,
            gradebook = ContentState.Loading,
        )

        currentLoadJob = scope.launch {
            launch {
                val courses = courseRepository.fetchCourses()
                if (courses != null) {
                    _state.value = _state.value.copy(
                        courses = ContentState.Success(courses),
                    )
                } else {
                    logger.warn { "Failed to load courses" }
                    _state.value = _state.value.copy(
                        courses = ContentState.Error("Не удалось загрузить курсы"),
                    )
                }
            }

            launch {
                val performance = performanceRepository.fetchPerformance()
                if (performance != null) {
                    _state.value = _state.value.copy(
                        performanceCourses = ContentState.Success(performance.courses),
                    )
                } else {
                    logger.warn { "Failed to load performance data" }
                    _state.value = _state.value.copy(
                        performanceCourses = ContentState.Error("Не удалось загрузить статистику"),
                    )
                }
            }

            launch {
                runCatchingCancellable {
                    performanceRepository.fetchGradebook()
                }.onSuccess { gradebook ->
                    _state.value = _state.value.copy(
                        gradebook = ContentState.Success(gradebook),
                    )
                }.onFailure { e ->
                    logger.error(e) { "Failed to load gradebook" }
                    _state.value = _state.value.copy(
                        gradebook = ContentState.Error("Не удалось загрузить зачётку"),
                    )
                    _effects.trySend(
                        CoursesComponent.Effect.ShowError("Не удалось загрузить зачётку"),
                    )
                }
            }
        }
    }
}
