package io.github.kroune.cumobile.data.network

/**
 * Central registry of all API endpoint paths.
 *
 * Keeping paths here makes it easy to update them if the backend changes,
 * and prepares for a future remote-config approach.
 */
internal object ApiEndpoints {
    object Profile {
        const val ME = "student-hub/students/me"
        const val AVATAR_ME = "hub/avatars/me"
        const val LMS_ME = "micro-lms/students/me"
    }

    object Tasks {
        const val STUDENT = "micro-lms/tasks/student"
        const val COMMENTS = "micro-lms/comments"

        fun commentById(id: String): String =
            "micro-lms/comments/$id"

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

    object Courses {
        const val STUDENT = "micro-lms/courses/student"

        fun overview(id: String): String =
            "micro-lms/courses/$id/overview"

        fun exercises(courseId: String): String =
            "micro-lms/courses/$courseId/exercises"
    }

    object Content {
        const val DOWNLOAD_LINK = "micro-lms/content/download-link"
        const val UPLOAD_LINK = "micro-lms/content/upload-link"

        fun longreadMaterials(longreadId: String): String =
            "micro-lms/longreads/$longreadId/materials"

        fun material(materialId: String): String =
            "micro-lms/materials/$materialId"
    }

    object Notifications {
        const val IN_APP = "notification-hub/notifications/in-app"
    }

    object Performance {
        const val STUDENT = "micro-lms/performance/student"
        const val GRADEBOOK = "micro-lms/gradebook"

        fun coursePerformance(courseId: String): String =
            "micro-lms/courses/$courseId/student-performance"
    }

    object Quizzes {
        const val ATTEMPTS = "micro-lms/quizzes/attempts"

        fun attemptById(id: String): String =
            "micro-lms/quizzes/attempts/$id"

        fun completeAttempt(id: String): String =
            "micro-lms/quizzes/attempts/$id/complete"

        fun questions(quizId: String): String =
            "micro-lms/quizzes/$quizId/questions"

        fun sessionAttempts(sessionId: String): String =
            "micro-lms/quizzes/sessions/$sessionId/attempts"
    }

    object Timetable {
        const val ME = "micro-lms/students/me/timetables"
    }
}
