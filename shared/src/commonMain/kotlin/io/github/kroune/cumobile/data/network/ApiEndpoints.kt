package io.github.kroune.cumobile.data.network

/**
 * Central registry of all API endpoint paths.
 *
 * Keeping paths here makes it easy to update them if the backend changes,
 * and prepares for a future remote-config approach.
 */
internal object ApiEndpoints {
    // Profile / Auth
    const val PROFILE_ME = "student-hub/students/me"
    const val AVATAR_ME = "hub/avatars/me"
    const val LMS_PROFILE_ME = "micro-lms/students/me"

    // Tasks
    const val TASKS_STUDENT = "micro-lms/tasks/student"
    const val COMMENTS = "micro-lms/comments"

    object Tasks {
        fun byId(id: String): String =
            "micro-lms/tasks/$id"

        fun events(id: String): String =
            "micro-lms/tasks/$id/events"

        fun comments(id: String): String =
            "micro-lms/tasks/$id/comments"

        fun start(id: String): String =
            "micro-lms/tasks/$id/start"

        fun submit(id: String): String =
            "micro-lms/tasks/$id/submit"

        fun lateDaysProlong(id: String): String =
            "micro-lms/tasks/$id/late-days-prolong"

        fun lateDaysCancel(id: String): String =
            "micro-lms/tasks/$id/late-days-cancel"
    }

    // Courses
    const val COURSES_STUDENT = "micro-lms/courses/student"

    fun courseOverview(id: String): String =
        "micro-lms/courses/$id/overview"

    // Content
    fun longreadMaterials(longreadId: String): String =
        "micro-lms/longreads/$longreadId/materials"

    fun material(materialId: String): String =
        "micro-lms/materials/$materialId"

    const val CONTENT_DOWNLOAD_LINK = "micro-lms/content/download-link"
    const val CONTENT_UPLOAD_LINK = "micro-lms/content/upload-link"

    // Notifications
    const val NOTIFICATIONS_IN_APP = "notification-hub/notifications/in-app"

    // Performance
    const val PERFORMANCE_STUDENT = "micro-lms/performance/student"

    fun courseExercises(courseId: String): String =
        "micro-lms/courses/$courseId/exercises"

    fun coursePerformance(courseId: String): String =
        "micro-lms/courses/$courseId/student-performance"

    const val GRADEBOOK = "micro-lms/gradebook"

    // Timetable
    const val TIMETABLES_ME = "micro-lms/students/me/timetables"
}
