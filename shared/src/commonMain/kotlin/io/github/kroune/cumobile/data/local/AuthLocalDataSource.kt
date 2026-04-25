package io.github.kroune.cumobile.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.kroune.cumobile.presentation.common.invoke
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local data source for authentication-related storage.
 * Uses DataStore Preferences to persist the BFF cookie.
 */
internal class AuthLocalDataSource(
    private val dataStoreLazy: Lazy<DataStore<Preferences>>,
) {
    /** Flow emitting the current cookie value, or null if not stored. */
    val cookieFlow: Flow<String?> by lazy {
        dataStoreLazy().data.map { preferences -> preferences[COOKIE_KEY] }
    }

    /** Saves the BFF cookie to local storage. */
    suspend fun saveCookie(cookie: String) {
        dataStoreLazy().edit { preferences ->
            preferences[COOKIE_KEY] = cookie
        }
    }

    /** Clears the stored BFF cookie (e.g., on logout or 401). */
    suspend fun clearCookie() {
        dataStoreLazy().edit { preferences ->
            preferences.remove(COOKIE_KEY)
        }
    }

    private companion object {
        val COOKIE_KEY = stringPreferencesKey("bff_cookie")
    }
}
