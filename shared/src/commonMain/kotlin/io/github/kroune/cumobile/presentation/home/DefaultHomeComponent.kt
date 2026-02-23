package io.github.kroune.cumobile.presentation.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.domain.repository.TaskRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [HomeComponent].
 *
 * Loads tasks, courses, and profile data on creation.
 * Delegates navigation intents to [onOpenTask] and [onOpenCourse] callbacks.
 */
class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val taskRepository: TaskRepository,
    private val courseRepository: CourseRepository,
    private val profileRepository: ProfileRepository,
    private val onOpenTask: (StudentTask) -> Unit,
    private val onOpenCourse: (Int) -> Unit,
) : HomeComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(HomeComponent.State())
    override val state: Value<HomeComponent.State> = _state

    init {
        loadData()
    }

    override fun onIntent(intent: HomeComponent.Intent) {
        when (intent) {
            is HomeComponent.Intent.OpenTask -> onOpenTask(intent.task)
            is HomeComponent.Intent.OpenCourse -> onOpenCourse(intent.courseId)
            is HomeComponent.Intent.Refresh -> loadData()
        }
    }

    private fun loadData() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        scope.launch {
            try {
                val tasks = loadTasks()
                val courses = courseRepository.fetchCourses()
                val initials = loadProfileInitials()
                val lmsProfile = profileRepository.fetchLmsProfile()

                _state.value = _state.value.copy(
                    tasks = tasks.orEmpty(),
                    courses = courses.orEmpty(),
                    profileInitials = initials,
                    lateDaysBalance = lmsProfile?.lateDaysBalance,
                    isLoading = false,
                    error = if (tasks == null && courses == null) {
                        "Не удалось загрузить данные"
                    } else {
                        null
                    },
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to load home data" }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ошибка: ${e.message}",
                )
            }
        }
    }

    /**
     * Loads tasks with states relevant for the home screen.
     *
     * Fetches active and review-state tasks to show in deadlines.
     */
    private suspend fun loadTasks(): List<StudentTask>? {
        val states = listOf(
            TaskState.InProgress,
            TaskState.Review,
            TaskState.Backlog,
            TaskState.Failed,
            TaskState.Evaluated,
        )
        return taskRepository.fetchTasks(states)
    }

    /**
     * Computes user initials from the profile (first letters of
     * first name and last name).
     */
    private suspend fun loadProfileInitials(): String {
        val profile = profileRepository.fetchProfile() ?: return ""
        val first = profile.firstName
            .firstOrNull()
            ?.uppercase()
            .orEmpty()
        val last = profile.lastName
            .firstOrNull()
            ?.uppercase()
            .orEmpty()
        return "$first$last"
    }
}
