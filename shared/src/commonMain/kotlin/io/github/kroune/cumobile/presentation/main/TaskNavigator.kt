package io.github.kroune.cumobile.presentation.main

import io.github.kroune.cumobile.data.model.CourseOverview
import io.github.kroune.cumobile.data.model.Longread
import io.github.kroune.cumobile.domain.repository.CourseRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * Resolves a task's exercise id to its containing longread (via course overview)
 * and navigates there. Falls back to the course detail screen when the exercise
 * cannot be located.
 */
internal class TaskNavigator(
    private val scope: CoroutineScope,
    private val courseRepository: CourseRepository,
    private val navigateToLongread: (longreadId: String, courseId: String, themeId: String) -> Unit,
    private val navigateToCourseDetail: (courseId: String) -> Unit,
) {
    fun navigate(
        courseId: String,
        exerciseId: String,
    ) {
        scope.launch {
            val overview = courseRepository.fetchCourseOverview(courseId)
            val location = overview?.findExerciseLocation(exerciseId)
            if (location == null) {
                logger.warn {
                    "Exercise $exerciseId not found in course $courseId; falling back to course detail"
                }
                navigateToCourseDetail(courseId)
                return@launch
            }
            navigateToLongread(location.longreadId, courseId, location.themeId)
        }
    }
}

private data class ExerciseLocation(
    val themeId: String,
    val longreadId: String,
)

private fun CourseOverview.findExerciseLocation(exerciseId: String): ExerciseLocation? {
    for (theme in themes) {
        val longread = theme.longreads.find { it.containsExercise(exerciseId) } ?: continue
        return ExerciseLocation(themeId = theme.id, longreadId = longread.id)
    }
    return null
}

private fun Longread.containsExercise(exerciseId: String): Boolean =
    exercises.any { it.id == exerciseId }
