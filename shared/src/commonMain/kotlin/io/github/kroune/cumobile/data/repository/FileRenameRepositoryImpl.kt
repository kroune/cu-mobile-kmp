package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.FileRenameLocalDataSource
import io.github.kroune.cumobile.data.local.FileRenameRule
import io.github.kroune.cumobile.domain.repository.FileRenameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Implementation of [FileRenameRepository] using [FileRenameLocalDataSource].
 */
internal class FileRenameRepositoryImpl(
    private val localDataSource: FileRenameLocalDataSource,
) : FileRenameRepository {
    override val rules: Flow<List<FileRenameRule>> = localDataSource.rulesFlow

    override suspend fun saveRules(rules: List<FileRenameRule>) {
        localDataSource.saveRules(rules)
    }

    override suspend fun addRule(rule: FileRenameRule) {
        val current = rules.first()
        saveRules(current + rule)
    }

    override suspend fun deleteRule(rule: FileRenameRule) {
        val current = rules.first()
        saveRules(current.filter { it != rule })
    }

    override suspend fun getMatchingRule(
        courseId: Int,
        activityName: String,
        extension: String,
    ): FileRenameRule? {
        val current = rules.first()
        return current.find {
            it.courseId == courseId &&
                it.activityName.equals(activityName, ignoreCase = true) &&
                it.extension.equals(extension, ignoreCase = true)
        }
    }
}
