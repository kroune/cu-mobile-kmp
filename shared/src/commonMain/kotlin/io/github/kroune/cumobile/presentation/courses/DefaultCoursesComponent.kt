package io.github.kroune.cumobile.presentation.courses

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Default implementation of [CoursesComponent].
 *
 * Loads courses, performance data, and gradebook from repositories.
 * Delegates course navigation to the parent via callbacks.
 */
class DefaultCoursesComponent(
    componentContext: ComponentContext,
    private val courseRepository: CourseRepository,
    private val performanceRepository: PerformanceRepository,
    private val onOpenCourse: (courseId: Int) -> Unit,
    private val onOpenCoursePerformance: (courseId: Int, courseName: String, totalGrade: Int) -> Unit,
) : CoursesComponent,
    ComponentContext by componentContext {
    private val scope = CoroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(CoursesComponent.State())
    override val state: Value<CoursesComponent.State> = _state

    override fun onIntent(intent: CoursesComponent.Intent) {
        when (intent) {
            is CoursesComponent.Intent.SelectSegment ->
                _state.value = _state.value.copy(segment = intent.index)
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
        }
    }

    init {
        lifecycle.subscribe(
            object : Lifecycle.Callbacks {
                override fun onDestroy() {
                    scope.cancel()
                }
            },
        )
        loadAllData()
    }

    private fun loadAllData() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val courses = courseRepository.fetchCourses()
            val performance = performanceRepository.fetchPerformance()
            val gradebook = performanceRepository.fetchGradebook()

            if (courses != null) {
                _state.value = _state.value.copy(
                    courses = courses,
                    performanceCourses = performance?.courses ?: emptyList(),
                    gradebook = gradebook,
                    isLoading = false,
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить курсы",
                )
            }
        }
    }
}
