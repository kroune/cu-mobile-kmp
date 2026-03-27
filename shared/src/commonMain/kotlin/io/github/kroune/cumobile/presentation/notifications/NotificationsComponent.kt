package io.github.kroune.cumobile.presentation.notifications

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.NotificationCategory
import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.isLoading
import kotlinx.coroutines.flow.Flow

/**
 * MVI component for the notifications screen.
 *
 * Displays two tabs: "Учеба" (education, [NotificationCategory.Education]) and
 * "Другое" (other, [NotificationCategory.Other]). Supports deep-linking
 * from notification links.
 */
interface NotificationsComponent {
    val state: Value<State>
    val effects: Flow<Effect>

    fun onIntent(intent: Intent)

    sealed interface Effect {
        data class ShowError(val message: String) : Effect
    }

    data class State(
        val educationNotifications: ContentState<List<NotificationItem>> = ContentState.Loading,
        val otherNotifications: ContentState<List<NotificationItem>> = ContentState.Loading,
        /** Currently selected tab index: 0 = Education, 1 = Other. */
        val selectedTab: Int = 0,
        /** URI that should be opened externally (set by OpenLink, consumed by UI). */
        val externalLinkToOpen: String? = null,
    ) {
        /** Whether both tabs are still loading. */
        val isContentLoading: Boolean
            get() = educationNotifications.isLoading && otherNotifications.isLoading

        /** ContentState for the currently selected tab. */
        val currentNotifications: ContentState<List<NotificationItem>>
            get() = if (selectedTab == 0) educationNotifications else otherNotifications
    }

    sealed interface Intent {
        data object Back : Intent

        data object Refresh : Intent

        data class SelectTab(
            val index: Int,
        ) : Intent

        data class OpenLink(
            val uri: String,
        ) : Intent

        /** Acknowledge that the external link has been opened. */
        data object ExternalLinkOpened : Intent
    }
}
