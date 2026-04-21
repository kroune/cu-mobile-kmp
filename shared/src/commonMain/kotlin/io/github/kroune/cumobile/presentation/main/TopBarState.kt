package io.github.kroune.cumobile.presentation.main

import io.github.kroune.cumobile.presentation.common.ContentState

data class TopBarState(
    val avatarUrl: String = "",
    val lateDaysBalance: ContentState<Int?> = ContentState.Loading,
)
