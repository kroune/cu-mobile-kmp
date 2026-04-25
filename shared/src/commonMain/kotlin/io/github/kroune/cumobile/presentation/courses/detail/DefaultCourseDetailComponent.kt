package io.github.kroune.cumobile.presentation.courses.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.presentation.common.componentScope
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
    courseRepository: Lazy<CourseRepository>,
    private val onOpenLongread: (longreadId: String, courseId: String, themeId: String) -> Unit,
    private val onBack: () -> Unit,
) : CourseDetailComponent,
    ComponentContext by componentContext {
    private val courseRepository by courseRepository

    private val scope = componentScope()

    private val _state = MutableValue(
        CourseDetailComponent.State(courseId = courseId),
    )
    override val state: Value<CourseDetailComponent.State> = _state

    private fun updateState(block: CourseDetailComponent.State.() -> CourseDetailComponent.State) {
        val s = _state.value.block()
        _state.value = s.copy(
            filteredThemes = filteredThemes(
                s.overview?.themes.orEmpty(),
                s.searchQuery,
            ),
        )
    }

    override fun onIntent(intent: CourseDetailComponent.Intent) {
        when (intent) {
            is CourseDetailComponent.Intent.Search ->
                updateState { copy(searchQuery = intent.query) }
            is CourseDetailComponent.Intent.ToggleTheme -> {
                val current = _state.value.expandedThemeIds
                val updated = if (intent.themeId in current) {
                    current - intent.themeId
                } else {
                    current + intent.themeId
                }
                updateState { copy(expandedThemeIds = updated) }
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
            updateState { copy(isLoading = true, error = null, overview = null) }

            val overview = courseRepository.fetchCourseOverview(courseId)
            if (overview != null) {
                updateState { copy(overview = overview, isLoading = false) }
            } else {
                updateState { copy(isLoading = false, error = "Не удалось загрузить курс") }
            }
        }
    }
}
