package io.github.kroune.cumobile.presentation.performance

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.CourseExercise
import io.github.kroune.cumobile.data.model.TaskScore
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.componentScope
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.common.isLoading
import io.github.kroune.cumobile.util.AppDispatchers
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Target course identity for [DefaultCoursePerformanceComponent].
 * Bundled to keep the constructor within the detekt LongParameterList threshold.
 */
data class CoursePerformanceParams(
    val courseId: String,
    val courseName: String,
    val totalGrade: Int,
)

/**
 * Default implementation of [CoursePerformanceComponent].
 *
 * Loads course exercises and per-task scores in parallel, then joins
 * them into [ExerciseWithScore] items and computes
 * [ActivitySummary] aggregates.
 */
class DefaultCoursePerformanceComponent(
    componentContext: ComponentContext,
    private val params: CoursePerformanceParams,
    performanceRepository: Lazy<PerformanceRepository>,
    dispatchers: Lazy<AppDispatchers>,
    private val onBack: () -> Unit,
) : CoursePerformanceComponent,
    ComponentContext by componentContext {
    private val performanceRepository by performanceRepository
    private val dispatchers by dispatchers
    private val scope = componentScope()

    private val _state = MutableValue(
        CoursePerformanceComponent.State(
            courseId = params.courseId,
            courseName = params.courseName,
            totalGrade = params.totalGrade,
        ),
    )
    override val state: Value<CoursePerformanceComponent.State> = _state

    private fun updateState(block: CoursePerformanceComponent.State.() -> CoursePerformanceComponent.State) {
        val s = _state.value.block()
        val exercises = s.content.dataOrNull
            ?.exercises
            .orEmpty()
            .toImmutableList()
        val summaries = s.content.dataOrNull
            ?.activitySummaries
            .orEmpty()
            .toImmutableList()
        val filtered = if (s.activityFilter == null) {
            exercises
        } else {
            exercises.filter { it.activityName == s.activityFilter }.toImmutableList()
        }
        _state.value = s.copy(
            exercises = exercises,
            activitySummaries = summaries,
            isContentLoading = s.content.isLoading,
            activityNames = exercises
                .map { it.activityName }
                .distinct()
                .sorted()
                .toImmutableList(),
            filteredExercises = filtered,
            totalContribution = summaries.sumOf { it.totalContribution },
        )
    }

    override fun onIntent(intent: CoursePerformanceComponent.Intent) {
        when (intent) {
            CoursePerformanceComponent.Intent.Back -> onBack()
            CoursePerformanceComponent.Intent.Refresh -> loadData()
            is CoursePerformanceComponent.Intent.SelectTab ->
                updateState { copy(selectedTab = intent.index) }
            is CoursePerformanceComponent.Intent.FilterByActivity ->
                updateState { copy(activityFilter = intent.activityName) }
        }
    }

    init {
        loadData()
    }

    private fun loadData() {
        updateState { copy(content = ContentState.Loading) }

        scope.launch {
            val exercisesDeferred = async {
                performanceRepository.fetchCourseExercises(params.courseId)
            }
            val performanceDeferred = async {
                performanceRepository.fetchCoursePerformance(params.courseId)
            }

            val exercisesResponse = exercisesDeferred.await()
            val performanceResponse = performanceDeferred.await()

            if (exercisesResponse == null && performanceResponse == null) {
                updateState {
                    copy(content = ContentState.Error("Не удалось загрузить успеваемость"))
                }
                return@launch
            }

            val exercises = exercisesResponse?.exercises.orEmpty()
            val tasks = performanceResponse?.tasks.orEmpty()

            val performanceData = withContext(dispatchers.default) {
                PerformanceData(
                    exercises = joinExercisesWithScores(exercises, tasks).toImmutableList(),
                    activitySummaries = buildActivitySummaries(tasks).toImmutableList(),
                )
            }

            updateState { copy(content = ContentState.Success(performanceData)) }
        }
    }
}

/**
 * Joins exercises with their scores by matching
 * [CourseExercise.id] to [TaskScore.exerciseId].
 */
internal fun joinExercisesWithScores(
    exercises: List<CourseExercise>,
    tasks: List<TaskScore>,
): List<ExerciseWithScore> {
    val scoreByExerciseId = tasks.associateBy { it.exerciseId }
    return exercises.map { exercise ->
        ExerciseWithScore(
            exercise = exercise,
            score = scoreByExerciseId[exercise.id],
        )
    }
}

/**
 * Groups tasks by activity and computes per-activity averages.
 *
 * Results are sorted by weight descending.
 */
internal fun buildActivitySummaries(tasks: List<TaskScore>): List<ActivitySummary> {
    val grouped = tasks.groupBy { it.activity.id }
    val summaries = grouped.map { entry ->
        val activityId = entry.key
        val activityTasks = entry.value
        val first = activityTasks.first()
        val totalScore = activityTasks.sumOf { it.score }
        val avgScore = totalScore / activityTasks.size
        ActivitySummary(
            activityId = activityId,
            activityName = first.activity.name,
            count = activityTasks.size,
            averageScore = avgScore,
            weight = first.activity.weight,
        )
    }
    return summaries.sortedByDescending { it.weight }
}
