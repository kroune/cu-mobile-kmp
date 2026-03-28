package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.FileRenameLocalDataSource
import io.github.kroune.cumobile.data.local.FileRenameRule
import io.github.kroune.cumobile.domain.repository.FileRenameRepository
import kotlinx.coroutines.flow.Flow

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
        localDataSource.addRule(rule)
    }

    override suspend fun deleteRule(rule: FileRenameRule) {
        localDataSource.deleteRule(rule)
    }

    override suspend fun getMatchingRule(
        courseId: String,
        activityName: String,
        extension: String,
    ): FileRenameRule? =
        localDataSource.getMatchingRule(courseId, activityName, extension)
}
