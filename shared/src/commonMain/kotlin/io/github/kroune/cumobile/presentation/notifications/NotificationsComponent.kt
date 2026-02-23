package io.github.kroune.cumobile.presentation.notifications

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.NotificationItem

/**
 * MVI component for the notifications screen.
 *
 * Displays two tabs: "Учеба" (education, category=1) and
 * "Другое" (other, category=2). Supports deep-linking
 * from notification links.
 */
interface NotificationsComponent {
    val state: Value<State>

    fun onIntent(intent: Intent)

    data class State(
        val educationNotifications: List<NotificationItem> = emptyList(),
        val otherNotifications: List<NotificationItem> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        /** Currently selected tab index: 0 = Education, 1 = Other. */
        val selectedTab: Int = 0,
    ) {
        /** Notifications for the currently selected tab. */
        val currentNotifications: List<NotificationItem>
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
    }
}
