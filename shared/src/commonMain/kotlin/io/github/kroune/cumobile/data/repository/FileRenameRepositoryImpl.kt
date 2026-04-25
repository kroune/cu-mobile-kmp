package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.FileRenameLocalDataSource
import io.github.kroune.cumobile.data.local.FileRenameRule
import io.github.kroune.cumobile.domain.repository.FileRenameRepository
import io.github.kroune.cumobile.presentation.common.invoke
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of [FileRenameRepository] using [FileRenameLocalDataSource].
 */
internal class FileRenameRepositoryImpl(
    private val localDataSourceLazy: Lazy<FileRenameLocalDataSource>,
) : FileRenameRepository {
    override val rules: Flow<List<FileRenameRule>> by lazy { localDataSourceLazy().rulesFlow }

    override suspend fun saveRules(rules: List<FileRenameRule>) {
        localDataSourceLazy().saveRules(rules)
    }

    override suspend fun addRule(rule: FileRenameRule) {
        localDataSourceLazy().addRule(rule)
    }

    override suspend fun deleteRule(rule: FileRenameRule) {
        localDataSourceLazy().deleteRule(rule)
    }

    override suspend fun getMatchingRule(
        courseId: String,
        activityName: String,
        extension: String,
    ): FileRenameRule? =
        localDataSourceLazy().getMatchingRule(courseId, activityName, extension)
}
