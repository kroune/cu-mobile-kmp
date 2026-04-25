package io.github.kroune.cumobile.presentation.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnStart
import io.github.kroune.cumobile.data.model.StudentTask
import io.github.kroune.cumobile.data.model.TaskState
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.DateTimeProvider
import io.github.kroune.cumobile.presentation.common.componentScope
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private val logger = KotlinLogging.logger {}

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
) : HomeComponent,
    ComponentContext by componentContext {
    private val taskRepository by deps.taskRepository
    private val courseRepository by deps.courseRepository
    private val calendarRepository by deps.calendarRepository
    private val scope = componentScope()

    private val dateTimeProvider = DateTimeProvider()
    private val today = dateTimeProvider.today()
    private val _state = MutableValue(
        HomeComponent.State(
            selectedDate = today,
            weekStart = computeWeekStart(today),
        ),
    )
    override val state: Value<HomeComponent.State> = _state

    private val _effects = Channel<HomeComponent.Effect>(Channel.BUFFERED)
    override val effects: Flow<HomeComponent.Effect> = _effects.receiveAsFlow()

    private var scheduleJob: Job? = null
    private var currentLoadJob: Job? = null

    init {
        lifecycle.doOnStart(isOneTime = true) {
            loadData()
            loadSchedule()
        }
    }

    override fun onIntent(intent: HomeComponent.Intent) {
        when (intent) {
            is HomeComponent.Intent.OpenTask -> onOpenTask(intent.task)
            is HomeComponent.Intent.OpenCourse -> onOpenCourse(intent.courseId)
            is HomeComponent.Intent.Refresh -> {
                loadData()
                loadSchedule()
            }
            HomeComponent.Intent.PreviousWeek -> changeWeek(-1)
            HomeComponent.Intent.NextWeek -> changeWeek(1)
            is HomeComponent.Intent.SelectDate -> selectDate(intent.date)
        }
    }

    private fun changeWeek(weeks: Int) {
        val currentWeekStart = _state.value.weekStart
        val newWeekStart = currentWeekStart.plus(DatePeriod(days = weeks * DAYS_IN_WEEK))
        val dayOffset = _state.value.selectedDate.toEpochDays() - currentWeekStart.toEpochDays()
        val newSelectedDate = newWeekStart.plus(DatePeriod(days = dayOffset.toInt()))
        _state.value = _state.value.copy(
            selectedDate = newSelectedDate,
            weekStart = newWeekStart,
        )
        loadSchedule()
    }

    private fun selectDate(date: LocalDate) {
        _state.value = _state.value.copy(
            selectedDate = date,
            weekStart = computeWeekStart(date),
        )
        loadSchedule()
    }

    private fun loadSchedule() {
        scheduleJob?.cancel()
        _state.value = _state.value.copy(schedule = ContentState.Loading)
        scheduleJob = scope.launch {
            val dateMillis = _state.value.selectedDate
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
            runCatchingCancellable {
                calendarRepository.getClassesForDate(dateMillis)
            }.fold(
                onSuccess = { classes ->
                    _state.value = _state.value.copy(
                        schedule = ContentState.Success(classes.toImmutableList()),
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
        )

        currentLoadJob = scope.launch {
            launch {
                val tasks = loadTasks()
                _state.value = _state.value.copy(
                    tasks = if (tasks != null) {
                        ContentState.Success(tasks.toImmutableList())
                    } else {
                        ContentState.Error("Не удалось загрузить задания")
                    },
                )
            }

            launch {
                val courses = courseRepository.fetchCourses()
                _state.value = _state.value.copy(
                    courses = if (courses != null) {
                        ContentState.Success(courses.toImmutableList())
                    } else {
                        ContentState.Error("Не удалось загрузить курсы")
                    },
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

    companion object {
        private const val DAYS_IN_WEEK = 7

        /**
         * Computes the Monday of the week containing [date].
         */
        fun computeWeekStart(date: LocalDate): LocalDate {
            val daysFromMonday = date.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal
            return date.minus(DatePeriod(days = daysFromMonday))
        }
    }
}
