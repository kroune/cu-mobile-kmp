package io.github.kroune.cumobile.data.local

import io.github.kroune.cumobile.data.local.db.FileRenameRuleDao
import io.github.kroune.cumobile.data.local.db.toDomain
import io.github.kroune.cumobile.data.local.db.toEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val logger = KotlinLogging.logger {}

/**
 * Local data source for persisting file renaming templates.
 * Backed by Room database.
 */
internal class FileRenameLocalDataSource(
    private val dao: FileRenameRuleDao,
) {
    /** Flow emitting the list of all configured rename rules. */
    val rulesFlow: Flow<List<FileRenameRule>> = dao.getAllRules().map { entities ->
        entities.map { it.toDomain() }
    }

    /** Replaces all rename rules with the given list. */
    suspend fun saveRules(rules: List<FileRenameRule>) {
        dao.replaceAllRules(rules.map { it.toEntity() })
    }

    /** Adds a single rename rule. */
    suspend fun addRule(rule: FileRenameRule) {
        dao.insertRule(rule.toEntity())
    }

    /** Deletes a single rename rule. */
    suspend fun deleteRule(rule: FileRenameRule) {
        val entity = dao.findMatchingRule(
            courseId = rule.courseId,
            activityName = rule.activityName,
            extension = rule.extension,
        )
        if (entity != null) {
            dao.deleteRule(entity)
        } else {
            logger.warn {
                "No matching rule to delete: courseId=${rule.courseId}, " +
                    "activity=${rule.activityName}, ext=${rule.extension}"
            }
        }
    }

    /** Finds a matching rule by course, activity, and extension. */
    suspend fun getMatchingRule(
        courseId: String,
        activityName: String,
        extension: String,
    ): FileRenameRule? =
        dao.findMatchingRule(courseId, activityName, extension)?.toDomain()
}
