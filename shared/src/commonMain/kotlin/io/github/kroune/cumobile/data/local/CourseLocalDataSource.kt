package io.github.kroune.cumobile.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local data source for persisting course-related settings,
 * such as manual order of courses.
 */
internal class CourseLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    /**
     * Flow emitting the list of course IDs in the preferred order.
     * Empty if no custom order is set.
     */
    val courseIdOrderFlow: Flow<List<Int>> = dataStore.data.map { preferences ->
        val orderString = preferences[COURSE_ORDER_KEY] ?: ""
        if (orderString.isBlank()) return@map emptyList()
        try {
            orderString.split(",").map { it.trim().toInt() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Saves a new course ID list to represent the manual order. */
    suspend fun saveCourseIdOrder(ids: List<Int>) {
        dataStore.edit { preferences ->
            preferences[COURSE_ORDER_KEY] = ids.joinToString(",")
        }
    }

    private companion object {
        val COURSE_ORDER_KEY = stringPreferencesKey("course_manual_order")
    }
}
