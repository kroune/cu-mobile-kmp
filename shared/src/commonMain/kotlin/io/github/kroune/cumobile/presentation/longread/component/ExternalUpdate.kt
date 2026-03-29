package io.github.kroune.cumobile.presentation.longread.component

/**
 * Events broadcast from the parent [LongreadComponent][io.github.kroune.cumobile.presentation.longread.LongreadComponent]
 * to material child components via a shared flow.
 *
 * Children collect this flow and update their own state accordingly.
 * Only components that care about a particular update type need to handle it.
 */
sealed interface ExternalUpdate {
    data class SearchQuery(
        val query: String,
    ) : ExternalUpdate
}
