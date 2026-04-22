package io.github.kroune.cumobile.presentation.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.PickedFile
import io.github.kroune.cumobile.data.network.ApiEndpoints
import io.github.kroune.cumobile.data.network.BaseUrl
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.componentScope
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock.System

private val logger = KotlinLogging.logger {}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    private val profileRepository: Lazy<ProfileRepository>,
    private val onBack: () -> Unit,
    private val onLogout: () -> Unit,
    private val onAvatarChanged: () -> Unit = {},
) : ProfileComponent,
    ComponentContext by componentContext {
    private val scope = componentScope()

    private var avatarVersion = System.now().toEpochMilliseconds()

    private val _state = MutableValue(
        ProfileComponent.State(avatarUrl = buildAvatarUrl()),
    )
    override val state: Value<ProfileComponent.State> = _state

    private val _effects = Channel<ProfileComponent.Effect>(Channel.BUFFERED)
    override val effects: Flow<ProfileComponent.Effect> = _effects.receiveAsFlow()

    private var currentLoadJob: Job? = null

    init {
        loadProfile()
    }

    override fun onIntent(intent: ProfileComponent.Intent) {
        when (intent) {
            ProfileComponent.Intent.Back -> onBack()
            ProfileComponent.Intent.Refresh -> loadProfile()
            is ProfileComponent.Intent.UploadAvatar -> uploadAvatar(intent.file)
            ProfileComponent.Intent.DeleteAvatar -> deleteAvatar()
            ProfileComponent.Intent.Logout -> onLogout()
        }
    }

    private fun loadProfile() {
        currentLoadJob?.cancel()

        _state.value = _state.value.copy(
            profile = ContentState.Loading,
            lmsProfile = ContentState.Loading,
        )

        currentLoadJob = scope.launch {
            launch {
                val profile = profileRepository().fetchProfile()
                _state.value = _state.value.copy(
                    profile = if (profile != null) {
                        ContentState.Success(profile)
                    } else {
                        ContentState.Error("Не удалось загрузить профиль")
                    },
                )
            }

            launch {
                val lmsProfile = profileRepository().fetchLmsProfile()
                _state.value = _state.value.copy(
                    lmsProfile = ContentState.Success(lmsProfile),
                )
            }
        }
    }

    private fun bumpAvatarVersion() {
        avatarVersion = System.now().toEpochMilliseconds()
        _state.value = _state.value.copy(avatarUrl = buildAvatarUrl())
        onAvatarChanged()
    }

    private fun buildAvatarUrl(): String =
        "${BaseUrl}${ApiEndpoints.Profile.AVATAR_ME}?v=$avatarVersion"

    private fun uploadAvatar(file: PickedFile) {
        scope.launch {
            _state.value = _state.value.copy(isUploadingAvatar = true)
            val success = profileRepository().uploadAvatar(file.bytes, file.contentType)
            if (success) {
                _state.value = _state.value.copy(isUploadingAvatar = false)
                bumpAvatarVersion()
            } else {
                logger.warn { "Failed to upload avatar" }
                _state.value = _state.value.copy(isUploadingAvatar = false)
                _effects.trySend(
                    ProfileComponent.Effect.ShowError("Не удалось загрузить аватар"),
                )
            }
        }
    }

    private fun deleteAvatar() {
        scope.launch {
            _state.value = _state.value.copy(isDeletingAvatar = true)
            val success = profileRepository().deleteAvatar()
            if (success) {
                _state.value = _state.value.copy(isDeletingAvatar = false)
                bumpAvatarVersion()
            } else {
                logger.warn { "Failed to delete avatar" }
                _state.value = _state.value.copy(isDeletingAvatar = false)
                _effects.trySend(
                    ProfileComponent.Effect.ShowError("Не удалось удалить аватар"),
                )
            }
        }
    }
}
