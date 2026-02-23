package io.github.kroune.cumobile.presentation.notifications

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.NotificationCategory
import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.domain.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Default implementation of [NotificationsComponent].
 *
 * Loads education ([NotificationCategory.Education]) and other ([NotificationCategory.Other])
 * notifications in parallel on creation. Supports tab switching and link opening.
 */
class DefaultNotificationsComponent(
    componentContext: ComponentContext,
    private val notificationRepository: NotificationRepository,
    private val onBack: () -> Unit,
    private val onOpenLongread: ((longreadId: Int) -> Unit)? = null,
) : NotificationsComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _state = MutableValue(NotificationsComponent.State(isLoading = true))
    override val state: Value<NotificationsComponent.State> = _state

    init {
        loadNotifications()
    }

    override fun onIntent(intent: NotificationsComponent.Intent) {
        when (intent) {
            NotificationsComponent.Intent.Back -> onBack()
            NotificationsComponent.Intent.Refresh -> loadNotifications()
            is NotificationsComponent.Intent.SelectTab -> {
                _state.value = _state.value.copy(selectedTab = intent.index)
            }
            is NotificationsComponent.Intent.OpenLink -> handleOpenLink(intent.uri)
        }
    }

    private fun loadNotifications() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val education = notificationRepository.fetchNotifications(category = NotificationCategory.Education)
            val other = notificationRepository.fetchNotifications(category = NotificationCategory.Other)
            if (education != null || other != null) {
                _state.value = _state.value.copy(
                    educationNotifications = sortByDate(education.orEmpty()),
                    otherNotifications = sortByDate(other.orEmpty()),
                    isLoading = false,
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить уведомления",
                )
            }
        }
    }

    /**
     * Handles tapping a notification link.
     *
     * If the URI matches the longread pattern, navigates in-app.
     * Otherwise the link should be opened externally (deferred to UI layer).
     */
    private fun handleOpenLink(uri: String) {
        val longreadRegex = Regex(
            """my\.centraluniversity\.ru/learn/courses/view/actual/\d+/themes/\d+/longreads/(\d+)""",
        )
        val match = longreadRegex.find(uri)
        if (match != null) {
            val longreadId = match.groupValues[1].toIntOrNull()
            if (longreadId != null) {
                onOpenLongread?.invoke(longreadId)
            }
        }
        // External links are handled by the UI layer via platform URL launcher
    }

    private fun sortByDate(items: List<NotificationItem>) = items.sortedByDescending { it.createdAt }
}
