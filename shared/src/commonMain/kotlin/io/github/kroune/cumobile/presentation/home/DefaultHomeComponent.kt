package io.github.kroune.cumobile.presentation.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.DateTimeProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

private const val MillisPerDay = 86_400_000L

/**
 * Default implementation of [HomeComponent].
 *
 * Loads tasks, courses, and profile data on creation.
 * Delegates navigation intents to [onOpenTask] and [onOpenCourse] callbacks.
 */
class DefaultHomeComponent(
    componentContext: ComponentContext,
    deps: HomeDependencies,
    private val onOpenTask: (StudentTask) -> Unit,
    private val onOpenCourse: (Int) -> Unit,
    private val onOpenProfile: () -> Unit = {},
) : HomeComponent,
    ComponentContext by componentContext {
    private val taskRepository = deps.taskRepository
    private val courseRepository = deps.courseRepository
    private val profileRepository = deps.profileRepository
    private val calendarRepository = deps.calendarRepository
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val dateTimeProvider = DateTimeProvider()
    private val _state = MutableValue(
        HomeComponent.State(
            selectedDateMillis = dateTimeProvider.todayMillis(),
        ),
    )
    override val state: Value<HomeComponent.State> = _state

    init {
        loadData()
        observeCalendarStatus()
    }

    override fun onIntent(intent: HomeComponent.Intent) {
        when (intent) {
            is HomeComponent.Intent.OpenTask -> onOpenTask(intent.task)
            is HomeComponent.Intent.OpenCourse -> onOpenCourse(intent.courseId)
            is HomeComponent.Intent.Refresh -> loadData()
            HomeComponent.Intent.PreviousDay -> changeDate(-1)
            HomeComponent.Intent.NextDay -> changeDate(1)
            HomeComponent.Intent.Today -> setToday()
            is HomeComponent.Intent.ConnectCalendar -> connectCalendar(intent.url)
            HomeComponent.Intent.DisconnectCalendar -> disconnectCalendar()
            HomeComponent.Intent.OpenProfile -> onOpenProfile()
        }
    }

    private fun observeCalendarStatus() {
        calendarRepository.calendarUrlFlow
            .onEach { url ->
                val isConnected = !url.isNullOrBlank()
                _state.value = _state.value.copy(isCalendarConnected = isConnected)
                if (isConnected) {
                    loadSchedule()
                } else {
                    _state.value = _state.value.copy(classes = emptyList())
                }
            }.launchIn(scope)
    }

    private fun changeDate(days: Int) {
        val current = _state.value.selectedDateMillis
        val updated = current + (days * MillisPerDay)
        _state.value = _state.value.copy(selectedDateMillis = updated)
        loadSchedule()
    }

    private fun setToday() {
        _state.value = _state.value.copy(selectedDateMillis = dateTimeProvider.todayMillis())
        loadSchedule()
    }

    private fun connectCalendar(url: String) {
        scope.launch {
            calendarRepository.saveCalendarUrl(url)
        }
    }

    private fun disconnectCalendar() {
        scope.launch {
            calendarRepository.saveCalendarUrl(null)
        }
    }

    private fun loadSchedule() {
        if (!_state.value.isCalendarConnected) return
        scope.launch {
            try {
                val dateMillis = _state.value.selectedDateMillis
                val classes = calendarRepository.getClassesForDate(dateMillis)
                _state.value = _state.value.copy(classes = classes)
            } catch (e: Exception) {
                logger.error(e) { "Failed to load schedule" }
            }
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
