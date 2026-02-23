package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.model.Course
import io.github.kroune.cumobile.data.model.CourseOverview

/** Repository for course-related operations. */
interface CourseRepository {
    /** Fetches all courses for the current student. */
    suspend fun fetchCourses(): List<Course>?

    /** Fetches the overview (themes + longreads) for a course. */
    suspend fun fetchCourseOverview(courseId: Int): CourseOverview?
}
