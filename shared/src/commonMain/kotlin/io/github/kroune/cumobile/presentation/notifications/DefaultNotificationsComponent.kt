package io.github.kroune.cumobile.presentation.notifications

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.NotificationCategory
import io.github.kroune.cumobile.data.model.NotificationItem
import io.github.kroune.cumobile.domain.repository.NotificationRepository
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.componentScope
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [NotificationsComponent].
 *
 * Loads education ([NotificationCategory.Education]) and other ([NotificationCategory.Other])
 * notifications in parallel on creation. Supports tab switching and link opening.
 */
class DefaultNotificationsComponent(
    componentContext: ComponentContext,
    private val notificationRepository: Lazy<NotificationRepository>,
    private val dispatchers: Lazy<AppDispatchers>,
    private val onBack: () -> Unit,
    private val onOpenLongread: ((longreadId: String, courseId: String, themeId: String) -> Unit)? = null,
) : NotificationsComponent,
    ComponentContext by componentContext {
    private val scope = componentScope()

    private val _state = MutableValue(NotificationsComponent.State())
    override val state: Value<NotificationsComponent.State> = _state

    private val _effects = Channel<NotificationsComponent.Effect>(Channel.BUFFERED)
    override val effects: Flow<NotificationsComponent.Effect> = _effects.receiveAsFlow()

    private var currentLoadJob: Job? = null

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

            is NotificationsComponent.Intent.OpenLink -> {
                val handledInApp = handleOpenLink(intent.uri)
                if (!handledInApp) {
                    _state.value = _state.value.copy(externalLinkToOpen = intent.uri)
                }
            }

            NotificationsComponent.Intent.ExternalLinkOpened -> {
                _state.value = _state.value.copy(externalLinkToOpen = null)
            }

            is NotificationsComponent.Intent.ToggleExpand -> {
                val current = _state.value.expandedNotificationIds
                val updated = if (intent.notificationId in current) {
                    current - intent.notificationId
                } else {
                    current + intent.notificationId
                }
                _state.value = _state.value.copy(expandedNotificationIds = updated)
            }
        }
    }

    private fun loadNotifications() {
        currentLoadJob?.cancel()

        _state.value = _state.value.copy(
            educationNotifications = ContentState.Loading,
            otherNotifications = ContentState.Loading,
        )

        currentLoadJob = scope.launch {
            launch {
                val education = notificationRepository().fetchNotifications(
                    category = NotificationCategory.Education,
                )
                if (education != null) {
                    _state.value = _state.value.copy(
                        educationNotifications = ContentState.Success(sortByDate(education)),
                    )
                } else {
                    logger.warn { "Failed to load education notifications" }
                    _state.value = _state.value.copy(
                        educationNotifications = ContentState.Error(
                            "Не удалось загрузить уведомления",
                        ),
                    )
                }
            }

            launch {
                val other = notificationRepository().fetchNotifications(
                    category = NotificationCategory.Other,
                )
                if (other != null) {
                    _state.value = _state.value.copy(
                        otherNotifications = ContentState.Success(sortByDate(other)),
                    )
                } else {
                    logger.warn { "Failed to load other notifications" }
                    _state.value = _state.value.copy(
                        otherNotifications = ContentState.Error(
                            "Не удалось загрузить уведомления",
                        ),
                    )
                }
            }
        }
    }

    /**
     * Handles tapping a notification link.
     *
     * If the URI matches the longread pattern, navigates in-app.
     * Otherwise the link should be opened externally (deferred to UI layer).
     *
     * @return `true` if the link was handled in-app, `false` for external handling.
     */
    internal fun handleOpenLink(uri: String): Boolean {
        val longreadRegex = Regex(
            """my\.centraluniversity\.ru/learn/courses/view/actual/(\d+)/themes/(\d+)/longreads/(\d+)""",
        )
        val match = longreadRegex.find(uri)
        if (match != null) {
            val (courseId, themeId, longreadId) = match.destructured
            if (courseId.isNotEmpty() && themeId.isNotEmpty() && longreadId.isNotEmpty()) {
                onOpenLongread?.invoke(longreadId, courseId, themeId)
                return true
            }
        }
        return false
    }

    private suspend fun sortByDate(items: List<NotificationItem>): List<NotificationItem> =
        withContext(dispatchers().default) {
            items.sortedByDescending { it.createdAt }
        }
}
