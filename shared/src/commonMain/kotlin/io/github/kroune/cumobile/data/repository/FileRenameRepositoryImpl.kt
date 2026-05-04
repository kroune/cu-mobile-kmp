package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.FileRenameLocalDataSource
import io.github.kroune.cumobile.data.model.mappers.toDataLocal
import io.github.kroune.cumobile.data.model.mappers.toDomain
import io.github.kroune.cumobile.domain.model.FileRenameRuleDomain
import io.github.kroune.cumobile.domain.repository.FileRenameRepository
import io.github.kroune.cumobile.util.invoke
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of [FileRenameRepository] using [FileRenameLocalDataSource].
 */
internal class FileRenameRepositoryImpl(
    private val localDataSourceLazy: Lazy<FileRenameLocalDataSource>,
) : FileRenameRepository {
    override val rules: Flow<List<FileRenameRuleDomain>> by lazy {
        localDataSourceLazy().rulesFlow.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveRules(rules: List<FileRenameRuleDomain>) {
        localDataSourceLazy().saveRules(rules.map { it.toDataLocal() })
    }

    override suspend fun addRule(rule: FileRenameRuleDomain) {
        localDataSourceLazy().addRule(rule.toDataLocal())
    }

    override suspend fun deleteRule(rule: FileRenameRuleDomain) {
        localDataSourceLazy().deleteRule(rule.toDataLocal())
    }

    override suspend fun getMatchingRule(
        courseId: String,
        activityName: String,
        extension: String,
    ): FileRenameRuleDomain? =
        localDataSourceLazy().getMatchingRule(courseId, activityName, extension)?.toDomain()
}
