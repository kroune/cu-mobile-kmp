package io.github.kroune.cumobile.presentation.notifications.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.formatDateTimeFull
import io.github.kroune.cumobile.presentation.common.model.NotificationUi
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.notifications.NotificationsComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private val previewLongNotification = NotificationUi(
    id = "long",
    title = "Длинное уведомление",
    description = "Это длинное описание уведомления, которое должно занимать " +
        "более трёх строк текста для демонстрации функции сворачивания и " +
        "разворачивания. Здесь может быть дополнительная информация о " +
        "задании, оценке или событии, которая не помещается в краткий " +
        "предпросмотр карточки уведомления.",
    icon = "education",
    category = "1",
    createdAtFormatted = formatDateTimeFull("2026-03-15T12:00:00"),
    linkUri = null,
    linkLabel = null,
)

private val previewNotificationsState = NotificationsComponent.State(
    educationNotifications = ContentState.Success(
        persistentListOf(
            NotificationUi(
                id = "1",
                title = "Новое задание",
                description = "Преподаватель назначил задание по курсу Алгоритмы",
                icon = "education",
                category = "1",
                createdAtFormatted = formatDateTimeFull("2026-03-18T10:00:00"),
                linkUri = null,
                linkLabel = null,
            ),
            NotificationUi(
                id = "2",
                title = "Оценка выставлена",
                description = "Получена оценка 8 за ДЗ: Деревья",
                icon = "education",
                category = "1",
                createdAtFormatted = formatDateTimeFull("2026-03-17T14:30:00"),
                linkUri = null,
                linkLabel = null,
            ),
            previewLongNotification,
        ),
    ),
    otherNotifications = ContentState.Success(persistentListOf()),
)

private val previewOtherTabState = NotificationsComponent.State(
    selectedTab = 1,
    educationNotifications = ContentState.Success(persistentListOf()),
    otherNotifications = ContentState.Success(
        persistentListOf(
            NotificationUi(
                id = "10",
                title = "Обновление системы",
                description = "Плановое техническое обслуживание 25 марта с 02:00 до 06:00",
                icon = "news",
                category = "2",
                createdAtFormatted = formatDateTimeFull("2026-03-20T09:00:00"),
                linkUri = null,
                linkLabel = null,
            ),
            NotificationUi(
                id = "11",
                title = "Новый опрос",
                description = "Пожалуйста, заполните опрос удовлетворённости обучением",
                icon = "servicedesk",
                category = "2",
                createdAtFormatted = formatDateTimeFull("2026-03-19T16:00:00"),
                linkUri = "https://example.com/survey",
                linkLabel = "Пройти опрос",
            ),
        ),
    ),
)

private val previewLongWithLink = NotificationUi(
    id = "long-link",
    title = "Очень длинный заголовок уведомления, который не помещается в две строки " +
        "и должен быть обрезан при сворачивании карточки",
    description = "Это длинное описание уведомления, которое должно занимать " +
        "более трёх строк текста для демонстрации функции сворачивания и " +
        "разворачивания. Здесь может быть дополнительная информация о " +
        "задании, оценке или событии, которая не помещается в краткий " +
        "предпросмотр карточки уведомления.",
    icon = "education",
    category = "1",
    createdAtFormatted = formatDateTimeFull("2026-03-16T08:00:00"),
    linkUri = "/learn/courses/view/actual/123/themes/456/longreads/789",
    linkLabel = "Открыть лонгрид «Динамическое программирование: основы и продвинутые техники»",
)

@Preview
@Composable
private fun PreviewNotificationsScreenSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsScreenSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsScreenDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = previewNotificationsState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsScreenLight() {
    CuMobileTheme(darkTheme = false) {
        NotificationsScreenContent(
            state = previewNotificationsState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsExpandedDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = previewNotificationsState.copy(
                expandedNotificationIds = setOf("long"),
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsErrorDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(
                educationNotifications = ContentState.Error("Не удалось загрузить уведомления"),
                otherNotifications = ContentState.Error("Не удалось загрузить уведомления"),
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsErrorLight() {
    CuMobileTheme(darkTheme = false) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(
                educationNotifications = ContentState.Error("Не удалось загрузить уведомления"),
                otherNotifications = ContentState.Error("Не удалось загрузить уведомления"),
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsEmptyDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(
                educationNotifications = ContentState.Success(persistentListOf()),
                otherNotifications = ContentState.Success(persistentListOf()),
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsOtherTabDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = previewOtherTabState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewNotificationsWithLinkDark() {
    val eduData = (previewNotificationsState.educationNotifications as ContentState.Success).data
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = previewNotificationsState.copy(
                educationNotifications = ContentState.Success(
                    (
                        eduData + NotificationUi(
                            id = "3",
                            title = "Новый лонгрид по Алгоритмам",
                            description = "Добавлен материал «Динамическое программирование»",
                            icon = "education",
                            category = "1",
                            createdAtFormatted = formatDateTimeFull("2026-03-16T08:00:00"),
                            linkUri = "/longread/123",
                            linkLabel = "Открыть лонгрид",
                        )
                    ).toImmutableList(),
                ),
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongWithLinkCollapsedDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(
                educationNotifications = ContentState.Success(persistentListOf(previewLongWithLink)),
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLongWithLinkExpandedDark() {
    CuMobileTheme(darkTheme = true) {
        NotificationsScreenContent(
            state = NotificationsComponent.State(
                educationNotifications = ContentState.Success(persistentListOf(previewLongWithLink)),
                expandedNotificationIds = setOf("long-link"),
            ),
            onIntent = {},
            onBack = {},
        )
    }
}
