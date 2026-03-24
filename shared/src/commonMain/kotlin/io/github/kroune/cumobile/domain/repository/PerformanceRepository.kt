package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.CourseExercisesResponse
import io.github.kroune.cumobile.data.model.CourseStudentPerformanceResponse
import io.github.kroune.cumobile.data.model.GradebookResponse
import io.github.kroune.cumobile.data.model.StudentPerformanceResponse

/** Repository for performance data and gradebook. */
interface PerformanceRepository {
    /** Fetches overall student performance across all courses. */
    suspend fun fetchPerformance(): StudentPerformanceResponse?

    /** Fetches exercises for a specific course. */
    suspend fun fetchCourseExercises(courseId: String): CourseExercisesResponse?

    /** Fetches per-task performance scores for a course. */
    suspend fun fetchCoursePerformance(courseId: String): CourseStudentPerformanceResponse?

    /** Fetches the student's gradebook. */
    suspend fun fetchGradebook(): GradebookResponse?
}
