package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.data.local.FileRenameRule
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing file renaming rules and templates.
 */
interface FileRenameRepository {
    /** Flow emitting the list of all configured rename rules. */
    val rules: Flow<List<FileRenameRule>>

    /** Saves a new list of rename rules. */
    suspend fun saveRules(rules: List<FileRenameRule>)

    /** Adds a single rename rule. */
    suspend fun addRule(rule: FileRenameRule)

    /** Deletes a single rename rule. */
    suspend fun deleteRule(rule: FileRenameRule)

    /**
     * Returns a rule that matches the given criteria, or null if none found.
     */
    suspend fun getMatchingRule(
        courseId: Int,
        activityName: String,
        extension: String,
    ): FileRenameRule?
}
