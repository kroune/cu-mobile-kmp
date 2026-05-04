package io.github.kroune.cumobile.domain.repository

import io.github.kroune.cumobile.domain.model.FileRenameRuleDomain
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing file renaming rules and templates.
 */
interface FileRenameRepository {
    /** Flow emitting the list of all configured rename rules. */
    val rules: Flow<List<FileRenameRuleDomain>>

    /** Saves a new list of rename rules. */
    suspend fun saveRules(rules: List<FileRenameRuleDomain>)

    /** Adds a single rename rule. */
    suspend fun addRule(rule: FileRenameRuleDomain)

    /** Deletes a single rename rule. */
    suspend fun deleteRule(rule: FileRenameRuleDomain)

    /**
     * Returns a rule that matches the given criteria, or null if none found.
     */
    suspend fun getMatchingRule(
        courseId: String,
        activityName: String,
        extension: String,
    ): FileRenameRuleDomain?
}
