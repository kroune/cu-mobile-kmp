package io.github.kroune.cumobile.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Creates a [DataStore] instance for app preferences.
 *
 * @param producePath platform-specific function that returns the file path
 *        for the DataStore file (e.g., using `context.filesDir` on Android).
 */
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() },
    )

/** Common DataStore file name used across platforms. */
internal const val DataStoreFileName = "cumobile_prefs.preferences_pb"
