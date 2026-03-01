package io.github.kroune.cumobile.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local data source for calendar-related storage.
 */
internal class CalendarLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val calendarUrlFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[CALENDAR_URL_KEY]
    }

    suspend fun saveCalendarUrl(url: String?) {
        dataStore.edit { preferences ->
            if (url == null) {
                preferences.remove(CALENDAR_URL_KEY)
            } else {
                preferences[CALENDAR_URL_KEY] = url
            }
        }
    }

    private companion object {
        val CALENDAR_URL_KEY = stringPreferencesKey("ics_url")
    }
}
