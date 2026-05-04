package io.github.kroune.cumobile.presentation.notifications

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.model.NotificationUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

/**
 * MVI component for the notifications screen.
 *
 * Displays two tabs: "Учеба" (education, category 1) and
 * "Другое" (other, category 2). Supports deep-linking
 * from notification links.
 */
interface NotificationsComponent {
    val state: Value<State>
    val effects: Flow<Effect>

    fun onIntent(intent: Intent)

    sealed interface Effect {
        data class ShowError(
            val message: String,
        ) : Effect
    }

    data class State(
        val educationNotifications: ContentState<ImmutableList<NotificationUi>> = ContentState.Loading,
        val otherNotifications: ContentState<ImmutableList<NotificationUi>> = ContentState.Loading,
        /** Currently selected tab index: 0 = Education, 1 = Other. */
        val selectedTab: Int = 0,
        /** URI that should be opened externally (set by OpenLink, consumed by UI). */
        val externalLinkToOpen: String? = null,
        /** IDs of notifications whose descriptions are fully expanded. */
        val expandedNotificationIds: Set<String> = emptySet(),
    )

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

        /** Toggle expand/collapse for a notification's description. */
        data class ToggleExpand(
            val notificationId: String,
        ) : Intent
    }
}
