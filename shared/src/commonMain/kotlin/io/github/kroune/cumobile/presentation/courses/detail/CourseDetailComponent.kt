package io.github.kroune.cumobile.presentation.courses.detail

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.CourseOverview
import io.github.kroune.cumobile.data.model.CourseTheme

/**
 * MVI component for the Course Detail screen.
 *
 * Displays course themes with expandable longreads and exercises.
 * Supports search filtering within themes and longreads.
 */
interface CourseDetailComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    data class State(
        val courseId: Int = 0,
        val overview: CourseOverview? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        /** Search query filtering themes and longreads by name. */
        val searchQuery: String = "",
        /** IDs of themes that are currently expanded. */
        val expandedThemeIds: Set<Int> = emptySet(),
    )

    sealed interface Intent {
        /** Update the search query. */
        data class Search(
            val query: String,
        ) : Intent

        /** Toggle expand/collapse for a theme. */
        data class ToggleTheme(
            val themeId: Int,
        ) : Intent

        /** Navigate to a longread. */
        data class OpenLongread(
            val longreadId: Int,
            val courseId: Int,
            val themeId: Int,
        ) : Intent

        /** Navigate back. */
        data object Back : Intent

        /** Refresh course data. */
        data object Refresh : Intent
    }
}

/**
 * Filters themes by search query.
 *
 * A theme matches if its name contains the query, or any of its
 * longreads' names match, or any of its exercises' names match.
 * For non-matching themes, only matching longreads are retained.
 */
internal fun filteredThemes(
    themes: List<CourseTheme>,
    query: String,
): List<CourseTheme> {
    if (query.isBlank()) return themes

    return themes.mapNotNull { theme ->
        val themeNameMatches = theme.name.contains(query, ignoreCase = true)
        if (themeNameMatches) {
            return@mapNotNull theme
        }

        val matchingLongreads = theme.longreads.filter { longread ->
            longread.name.contains(query, ignoreCase = true) ||
                longread.exercises.any { ex ->
                    ex.name.contains(query, ignoreCase = true)
                }
        }

        if (matchingLongreads.isNotEmpty()) {
            theme.copy(longreads = matchingLongreads)
        } else {
            null
        }
    }
}
