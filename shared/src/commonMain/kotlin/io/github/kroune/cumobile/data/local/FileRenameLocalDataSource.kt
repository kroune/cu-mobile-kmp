package io.github.kroune.cumobile.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

/**
 * Local data source for persisting file renaming templates.
 */
internal class FileRenameLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    /** Flow emitting the list of all configured rename rules. */
    val rulesFlow: Flow<List<FileRenameRule>> = dataStore.data.map { preferences ->
        val json = preferences[RULES_KEY] ?: ""
        if (json.isBlank()) return@map emptyList()
        try {
            Json.decodeFromString<List<FileRenameRule>>(json)
        } catch (e: kotlinx.serialization.SerializationException) {
            logger.warn(e) { "Failed to parse rename rules" }
            emptyList()
        }
    }

    /** Saves a new list of rename rules to persistent storage. */
    suspend fun saveRules(rules: List<FileRenameRule>) {
        dataStore.edit { preferences ->
            preferences[RULES_KEY] = Json.encodeToString(rules)
        }
    }

    private companion object {
        val RULES_KEY = stringPreferencesKey("file_rename_rules")
    }
}
