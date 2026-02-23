package io.github.kroune.cumobile.data.model

/**
 * Centralized task state string constants.
 *
 * These values match the API responses from the CU LMS backend.
 * Used for filtering, display, and state normalization throughout
 * the application.
 */
object TaskState {
    const val Backlog = "backlog"
    const val InProgress = "inProgress"
    const val HasSolution = "hasSolution"
    const val Revision = "revision"
    const val Rework = "rework"
    const val Review = "review"
    const val Evaluated = "evaluated"
    const val Failed = "failed"
    const val Rejected = "rejected"
}
