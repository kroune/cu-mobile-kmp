package io.github.kroune.cumobile.presentation.longread

import com.arkivanov.decompose.value.MutableValue

/**
 * Handles search functionality (toggle, query update, match navigation)
 * within the longread component.
 */
internal class LongreadSearchHandler(
    private val state: MutableValue<LongreadComponent.State>,
) {
    fun toggleSearch() {
        val current = state.value
        if (current.isSearchVisible) {
            state.value = current.copy(
                isSearchVisible = false,
                searchQuery = "",
                searchMatchCount = 0,
                currentMatchIndex = 0,
            )
        } else {
            state.value = current.copy(isSearchVisible = true)
        }
    }

    fun updateSearchQuery(query: String) {
        val matchCount = if (query.isBlank()) {
            0
        } else {
            countMatches(query)
        }
        state.value = state.value.copy(
            searchQuery = query,
            searchMatchCount = matchCount,
            currentMatchIndex = 0,
        )
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

    private fun countMatches(query: String): Int {
        val lowerQuery = query.lowercase()
        return state.value.materials.sumOf { material ->
            val text = material.viewContent?.let { stripHtmlTags(it) }.orEmpty()
            if (text.isBlank()) return@sumOf 0
            var count = 0
            var startIndex = 0
            val lowerText = text.lowercase()
            while (true) {
                val index = lowerText.indexOf(lowerQuery, startIndex)
                if (index < 0) break
                count++
                startIndex = index + 1
            }
            count
        }
    }
}
