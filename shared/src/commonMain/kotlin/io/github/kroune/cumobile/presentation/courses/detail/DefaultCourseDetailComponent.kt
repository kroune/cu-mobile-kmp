package io.github.kroune.cumobile.presentation.courses.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.domain.repository.CourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Default implementation of [CourseDetailComponent].
 *
 * Loads course overview from the repository and handles
 * theme expansion, search, and longread navigation.
 */
class DefaultCourseDetailComponent(
    componentContext: ComponentContext,
    private val courseId: String,
    private val courseRepository: CourseRepository,
    private val onOpenLongread: (longreadId: String, courseId: String, themeId: String) -> Unit,
    private val onBack: () -> Unit,
) : CourseDetailComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(
        CourseDetailComponent.State(courseId = courseId),
    )
    override val state: Value<CourseDetailComponent.State> = _state

    override fun onIntent(intent: CourseDetailComponent.Intent) {
        when (intent) {
            is CourseDetailComponent.Intent.Search ->
                _state.value = _state.value.copy(searchQuery = intent.query)
            is CourseDetailComponent.Intent.ToggleTheme -> {
                val current = _state.value.expandedThemeIds
                val updated = if (intent.themeId in current) {
                    current - intent.themeId
                } else {
                    current + intent.themeId
                }
                _state.value = _state.value.copy(expandedThemeIds = updated)
            }
            is CourseDetailComponent.Intent.OpenLongread ->
                onOpenLongread(intent.longreadId, intent.courseId, intent.themeId)
            CourseDetailComponent.Intent.Back -> onBack()
            CourseDetailComponent.Intent.Refresh -> loadOverview()
        }
    }

    init {
        loadOverview()
    }

    private fun loadOverview() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val overview = courseRepository.fetchCourseOverview(courseId)
            if (overview != null) {
                _state.value = _state.value.copy(
                    overview = overview,
                    isLoading = false,
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить курс",
                )
            }
        }
    }
}
