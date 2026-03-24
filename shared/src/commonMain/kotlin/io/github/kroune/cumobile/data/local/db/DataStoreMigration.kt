package io.github.kroune.cumobile.data.local.db

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.kroune.cumobile.data.local.FileRenameRule
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

/**
 * One-time migration of file rename rules from DataStore to Room.
 * Reads the old JSON key, inserts into the DAO, then removes the key.
 */
suspend fun migrateFileRenameRulesToRoom(
    dataStore: DataStore<Preferences>,
    dao: FileRenameRuleDao,
) {
    val key = stringPreferencesKey("file_rename_rules")
    val json = dataStore.data.first()[key] ?: return
    if (json.isBlank()) return

    val rules = try {
        Json.decodeFromString<List<FileRenameRule>>(json)
    } catch (e: SerializationException) {
        logger.warn(e) { "Failed to parse old rename rules during migration" }
        return
    }

    dao.insertRules(rules.map { it.toEntity() })
    dataStore.edit { it.remove(key) }
    logger.info { "Migrated ${rules.size} rename rules from DataStore to Room" }
}
