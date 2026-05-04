package io.github.kroune.cumobile.presentation.home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnStart
import io.github.kroune.cumobile.data.model.mappers.toApiValue
import io.github.kroune.cumobile.domain.model.ClassDataDomain
import io.github.kroune.cumobile.domain.model.TaskDomain
import io.github.kroune.cumobile.domain.model.TaskStatus
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.DateTimeProvider
import io.github.kroune.cumobile.presentation.common.componentScope
import io.github.kroune.cumobile.presentation.common.formatWeekRange
import io.github.kroune.cumobile.presentation.common.model.ClassDataUi
import io.github.kroune.cumobile.presentation.common.model.mappers.toUi
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Clock
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [HomeComponent].
 *
 * Loads tasks, courses, and schedule data in parallel on creation.
 * Delegates navigation intents to [onOpenTask] and [onOpenCourse] callbacks.
 */
class DefaultHomeComponent(
    componentContext: ComponentContext,
    deps: HomeDependencies,
    private val onOpenTask: (taskId: String, courseId: String, themeId: String, longreadId: String) -> Unit,
    private val onOpenCourse: (String) -> Unit,
) : HomeComponent,
    ComponentContext by componentContext {
    private val taskRepository by deps.taskRepository
    private val courseRepository by deps.courseRepository
    private val calendarRepository by deps.calendarRepository
    private val dispatchers by deps.dispatchers
    private val scope = componentScope()

    private val dateTimeProvider = DateTimeProvider()
    private val today = dateTimeProvider.today()
    private val initialWeekStart = computeWeekStart(today)
    private val _state = MutableValue(
        HomeComponent.State(
            selectedDate = today,
            weekStart = initialWeekStart,
            weekRangeLabel = formatWeekRange(initialWeekStart),
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
            is HomeComponent.Intent.OpenTask -> onOpenTask(
                intent.taskId,
                intent.courseId,
                intent.themeId,
                intent.longreadId,
            )
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
            weekRangeLabel = formatWeekRange(newWeekStart),
        )
        loadSchedule()
    }

    private fun selectDate(date: LocalDate) {
        val newWeekStart = computeWeekStart(date)
        _state.value = _state.value.copy(
            selectedDate = date,
            weekStart = newWeekStart,
            weekRangeLabel = formatWeekRange(newWeekStart),
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
                        schedule = ContentState.Success(
                            classes.map { it.toUi() }.toImmutableList(),
                        ),
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
                if (tasks != null) {
                    val now = Clock.System.now()
                    val (allTasksUi, deadlineTasks) = withContext(dispatchers.default) {
                        val all = tasks.map { it.toUi(now) }.toImmutableList()
                        val deadlines = tasks
                            .filter { !it.courseIsArchived && it.status.isActive }
                            .sortedBy { it.exerciseDeadline ?: it.deadline ?: NO_DEADLINE_SENTINEL }
                            .map { it.toUi(now) }
                            .toImmutableList()
                        all to deadlines
                    }
                    _state.value = _state.value.copy(
                        tasks = ContentState.Success(allTasksUi),
                        deadlineTasks = deadlineTasks,
                    )
                } else {
                    _state.value = _state.value.copy(
                        tasks = ContentState.Error("Не удалось загрузить задания"),
                    )
                }
            }

            launch {
                val courses = courseRepository.fetchCourses()
                if (courses != null) {
                    val (allCoursesUi, activeCourses) = withContext(dispatchers.default) {
                        val all = courses.map { it.toUi() }.toImmutableList()
                        val active = courses
                            .filter { !it.isArchived }
                            .map { it.toUi() }
                            .toImmutableList()
                        all to active
                    }
                    _state.value = _state.value.copy(
                        courses = ContentState.Success(allCoursesUi),
                        activeCourses = activeCourses,
                    )
                } else {
                    _state.value = _state.value.copy(
                        courses = ContentState.Error("Не удалось загрузить курсы"),
                    )
                }
            }
        }
    }

    /**
     * Loads tasks with states relevant for the home screen.
     *
     * Fetches active and review-state tasks to show in deadlines.
     */
    private suspend fun loadTasks(): List<TaskDomain>? {
        val states = listOf(
            TaskStatus.InProgress,
            TaskStatus.Review,
            TaskStatus.Backlog,
            TaskStatus.Failed,
            TaskStatus.Evaluated,
        ).map { it.toApiValue() }
        return taskRepository.fetchTasks(states)
    }

    companion object {
        private const val DAYS_IN_WEEK = 7

        /** Sentinel date used to sort tasks without a deadline to the end of the list. */
        private val NO_DEADLINE_SENTINEL = Instant.fromEpochMilliseconds(Long.MAX_VALUE)

        /**
         * Computes the Monday of the week containing [date].
         */
        fun computeWeekStart(date: LocalDate): LocalDate {
            val daysFromMonday = date.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal
            return date.minus(DatePeriod(days = daysFromMonday))
        }
    }
}

private fun ClassDataDomain.toUi() =
    ClassDataUi(
        startTime = startTime,
        endTime = endTime,
        room = room,
        type = type,
        title = title,
        professor = professor,
        link = link,
        badge = badge,
    )
