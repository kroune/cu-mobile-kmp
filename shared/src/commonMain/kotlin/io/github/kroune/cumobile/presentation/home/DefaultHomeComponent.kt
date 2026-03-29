package io.github.kroune.cumobile.presentation.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.DateTimeProvider
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

private const val MillisPerDay = 86_400_000L

/**
 * Default implementation of [HomeComponent].
 *
 * Loads tasks, courses, profile, and schedule data in parallel on creation.
 * Delegates navigation intents to [onOpenTask] and [onOpenCourse] callbacks.
 */
class DefaultHomeComponent(
    componentContext: ComponentContext,
    deps: HomeDependencies,
    private val onOpenTask: (StudentTask) -> Unit,
    private val onOpenCourse: (String) -> Unit,
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

    private val _effects = Channel<HomeComponent.Effect>(Channel.BUFFERED)
    override val effects: Flow<HomeComponent.Effect> = _effects.receiveAsFlow()

    private var scheduleJob: Job? = null
    private var currentLoadJob: Job? = null

    init {
        loadData()
        loadSchedule()
    }

    override fun onIntent(intent: HomeComponent.Intent) {
        when (intent) {
            is HomeComponent.Intent.OpenTask -> onOpenTask(intent.task)
            is HomeComponent.Intent.OpenCourse -> onOpenCourse(intent.courseId)
            is HomeComponent.Intent.Refresh -> {
                loadData()
                loadSchedule()
            }
            HomeComponent.Intent.PreviousDay -> changeDate(-1)
            HomeComponent.Intent.NextDay -> changeDate(1)
            HomeComponent.Intent.Today -> setToday()
            HomeComponent.Intent.OpenProfile -> onOpenProfile()
        }
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

    private fun loadSchedule() {
        scheduleJob?.cancel()
        _state.value = _state.value.copy(schedule = ContentState.Loading)
        scheduleJob = scope.launch {
            val dateMillis = _state.value.selectedDateMillis
            runCatchingCancellable {
                calendarRepository.getClassesForDate(dateMillis)
            }.fold(
                onSuccess = { classes ->
                    _state.value = _state.value.copy(
                        schedule = ContentState.Success(classes),
                    )
                },
                onFailure = { e ->
                    logger.error(e) { "Failed to load schedule" }
                    _state.value = _state.value.copy(
                        schedule = ContentState.Error("Не удалось загрузить расписание"),
                    )
                },
            )
        }
    }

    private fun loadData() {
        currentLoadJob?.cancel()

        _state.value = _state.value.copy(
            tasks = ContentState.Loading,
            courses = ContentState.Loading,
            profileInitials = ContentState.Loading,
            avatarBytes = ContentState.Loading,
            lateDaysBalance = ContentState.Loading,
        )

        currentLoadJob = scope.launch {
            launch {
                val tasks = loadTasks()
                _state.value = _state.value.copy(
                    tasks = if (tasks != null) {
                        ContentState.Success(tasks)
                    } else {
                        ContentState.Error("Не удалось загрузить задания")
                    },
                )
            }

            launch {
                val courses = courseRepository.fetchCourses()
                _state.value = _state.value.copy(
                    courses = if (courses != null) {
                        ContentState.Success(courses)
                    } else {
                        ContentState.Error("Не удалось загрузить курсы")
                    },
                )
            }

            launch {
                val initials = loadProfileInitials()
                _state.value = _state.value.copy(
                    profileInitials = ContentState.Success(initials),
                )
            }

            launch { loadLateDaysBalance() }
            launch { loadAvatar() }
        }
    }

    private suspend fun loadLateDaysBalance() {
        runCatchingCancellable {
            profileRepository.fetchLmsProfile()
        }.fold(
            onSuccess = { lmsProfile ->
                _state.value = _state.value.copy(
                    lateDaysBalance = ContentState.Success(lmsProfile?.lateDaysBalance),
                )
            },
            onFailure = { e ->
                logger.error(e) { "Failed to load late days balance" }
                _state.value = _state.value.copy(
                    lateDaysBalance = ContentState.Error("Не удалось загрузить баланс"),
                )
                _effects.trySend(
                    HomeComponent.Effect.ShowError("Не удалось загрузить баланс поздних дней"),
                )
            },
        )
    }

    private suspend fun loadAvatar() {
        runCatchingCancellable {
            profileRepository.fetchAvatar()
        }.fold(
            onSuccess = { bytes ->
                _state.value = _state.value.copy(
                    avatarBytes = ContentState.Success(bytes),
                )
            },
            onFailure = { e ->
                logger.error(e) { "Failed to load avatar" }
                _state.value = _state.value.copy(
                    avatarBytes = ContentState.Success(null),
                )
            },
        )
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
