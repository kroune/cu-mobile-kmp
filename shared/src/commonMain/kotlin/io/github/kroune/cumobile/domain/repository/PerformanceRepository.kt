package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.domain.model.CourseExerciseDomain
import io.github.kroune.cumobile.domain.model.CourseGradeDomain
import io.github.kroune.cumobile.domain.model.ExerciseScoreDomain
import io.github.kroune.cumobile.domain.model.GradebookResponseDomain

/** Repository for performance data and gradebook. */
interface PerformanceRepository {
    /** Fetches overall student performance across all courses. */
    suspend fun fetchPerformance(): List<CourseGradeDomain>?

    /** Fetches exercises for a specific course. */
    suspend fun fetchCourseExercises(courseId: String): List<CourseExerciseDomain>?

    /** Fetches per-task performance scores for a course. */
    suspend fun fetchCoursePerformance(courseId: String): List<ExerciseScoreDomain>?

    /** Fetches the student's gradebook. */
    suspend fun fetchGradebook(): GradebookResponseDomain?
}
