package io.github.kroune.cumobile.presentation.longread

import com.arkivanov.decompose.value.MutableValue

/**
 * Mutates the longread component's search-related state fields.
 *
 * Kept free of the actual match-counting logic — the owner feeds in the
 * pre-computed count via [applyQuery]. This lets the owner run the expensive
 * text scan on a background dispatcher against a cached plain-text index,
 * without having to know anything about the component's [State] shape.
 */
internal class LongreadSearchHandler(
    private val state: MutableValue<LongreadComponent.State>,
) {
    fun toggleSearch() {
        val current = state.value
        state.value = if (current.isSearchVisible) {
            current.copy(
                isSearchVisible = false,
                searchQuery = "",
                searchMatchCount = 0,
                currentMatchIndex = 0,
            )
        } else {
            current.copy(isSearchVisible = true)
        }
    }

    /**
     * Writes the user-visible query immediately so the search field stays
     * responsive; match-count update is applied later via [applyMatchCount]
     * after the background computation completes.
     */
    fun updateQueryText(query: String) {
        state.value = state.value.copy(searchQuery = query, currentMatchIndex = 0)
    }

    /**
     * Stores the match count computed for [forQuery]. Ignores stale
     * results whose query no longer matches the current one.
     */
    fun applyMatchCount(
        forQuery: String,
        matchCount: Int,
    ) {
        val current = state.value
        if (current.searchQuery != forQuery) return
        state.value = current.copy(searchMatchCount = matchCount, currentMatchIndex = 0)
    }

    fun navigateMatch(forward: Boolean) {
        val current = state.value
        if (current.searchMatchCount <= 0) return
        val newIndex = if (forward) {
            (current.currentMatchIndex + 1) % current.searchMatchCount
        } else {
            (current.currentMatchIndex - 1 + current.searchMatchCount) %
                current.searchMatchCount
        }
        state.value = current.copy(currentMatchIndex = newIndex)
    }
}

/**
 * Counts case-insensitive occurrences of [query] across all values in
 * [plainTextByMaterialId]. Pure function — safe to call on
 * [kotlinx.coroutines.Dispatchers.Default].
 */
internal fun countMatches(
    plainTextByMaterialId: Map<String, String>,
    query: String,
): Int {
    if (query.isBlank()) return 0
    val lowerQuery = query.lowercase()
    var total = 0
    for (text in plainTextByMaterialId.values) {
        if (text.isEmpty()) continue
        val lowerText = text.lowercase()
        var startIndex = 0
        while (true) {
            val index = lowerText.indexOf(lowerQuery, startIndex)
            if (index < 0) break
            total++
            startIndex = index + 1
        }
    }
    return total
}
