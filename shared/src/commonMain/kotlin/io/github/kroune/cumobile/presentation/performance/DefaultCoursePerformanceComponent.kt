package io.github.kroune.cumobile.presentation.performance

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.CourseExercise
import io.github.kroune.cumobile.data.model.TaskScore
import io.github.kroune.cumobile.domain.repository.PerformanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Default implementation of [CoursePerformanceComponent].
 *
 * Loads course exercises and per-task scores, then joins
 * them into [ExerciseWithScore] items and computes
 * [ActivitySummary] aggregates.
 */
class DefaultCoursePerformanceComponent(
    componentContext: ComponentContext,
    private val courseId: Int,
    private val courseName: String,
    private val totalGrade: Int,
    private val performanceRepository: PerformanceRepository,
    private val onBack: () -> Unit,
) : CoursePerformanceComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(
        CoursePerformanceComponent.State(
            courseId = courseId,
            courseName = courseName,
            totalGrade = totalGrade,
        ),
    )
    override val state: Value<CoursePerformanceComponent.State> = _state

    override fun onIntent(intent: CoursePerformanceComponent.Intent) {
        when (intent) {
            CoursePerformanceComponent.Intent.Back -> onBack()
            CoursePerformanceComponent.Intent.Refresh -> loadData()
            is CoursePerformanceComponent.Intent.SelectTab ->
                _state.value = _state.value.copy(selectedTab = intent.index)
            is CoursePerformanceComponent.Intent.FilterByActivity ->
                _state.value = _state.value.copy(
                    activityFilter = intent.activityName,
                )
        }
    }

    init {
        loadData()
    }

    private fun loadData() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val exercisesResponse =
                performanceRepository.fetchCourseExercises(courseId)
            val performanceResponse =
                performanceRepository.fetchCoursePerformance(courseId)

            if (exercisesResponse == null && performanceResponse == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить успеваемость",
                )
                return@launch
            }

            val exercises = exercisesResponse?.exercises.orEmpty()
            val tasks = performanceResponse?.tasks.orEmpty()

            val exercisesWithScores = joinExercisesWithScores(exercises, tasks)
            val summaries = buildActivitySummaries(tasks)

            _state.value = _state.value.copy(
                exercises = exercisesWithScores,
                activitySummaries = summaries,
                isLoading = false,
            )
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
