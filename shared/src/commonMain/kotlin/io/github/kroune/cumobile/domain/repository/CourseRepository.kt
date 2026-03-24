package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.CourseOverview
import kotlinx.coroutines.flow.Flow

/** Repository for course-related operations. */
interface CourseRepository {
    /** Fetches all courses for the current student. */
    suspend fun fetchCourses(): List<Course>?

    /** Fetches the overview (themes + longreads) for a course. */
    suspend fun fetchCourseOverview(courseId: String): CourseOverview?

    /** Flow emitting the list of course IDs in the preferred order. */
    val courseIdOrderFlow: Flow<List<String>>

    /** Saves a new course ID list to represent the manual order. */
    suspend fun saveCourseIdOrder(ids: List<String>)
}
