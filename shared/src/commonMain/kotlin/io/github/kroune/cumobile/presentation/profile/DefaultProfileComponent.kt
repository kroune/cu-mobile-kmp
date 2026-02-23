package io.github.kroune.cumobile.presentation.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import io.github.kroune.cumobile.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Default implementation of [ProfileComponent].
 *
 * Loads profile data and avatar on creation. Supports avatar deletion
 * and logout.
 */
class DefaultProfileComponent(
    componentContext: ComponentContext,
    private val profileRepository: ProfileRepository,
    private val onBack: () -> Unit,
    private val onLogout: () -> Unit,
) : ProfileComponent,
    ComponentContext by componentContext {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _state = MutableValue(ProfileComponent.State(isLoading = true))
    override val state: Value<ProfileComponent.State> = _state

    init {
        lifecycle.subscribe(
            object : Lifecycle.Callbacks {
                override fun onDestroy() {
                    scope.cancel()
                }
            },
        )
        loadProfile()
    }

    override fun onIntent(intent: ProfileComponent.Intent) {
        when (intent) {
            ProfileComponent.Intent.Back -> onBack()
            ProfileComponent.Intent.Refresh -> loadProfile()
            ProfileComponent.Intent.DeleteAvatar -> deleteAvatar()
            ProfileComponent.Intent.Logout -> onLogout()
        }
    }

    private fun loadProfile() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val profile = profileRepository.fetchProfile()
            val lmsProfile = profileRepository.fetchLmsProfile()
            val avatar = profileRepository.fetchAvatar()
            if (profile != null) {
                _state.value = _state.value.copy(
                    profile = profile,
                    lmsProfile = lmsProfile,
                    avatarBytes = avatar,
                    isLoading = false,
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить профиль",
                )
            }
        }
    }

    private fun deleteAvatar() {
        scope.launch {
            _state.value = _state.value.copy(isDeletingAvatar = true)
            val success = profileRepository.deleteAvatar()
            if (success) {
                _state.value = _state.value.copy(
                    avatarBytes = null,
                    isDeletingAvatar = false,
                )
            } else {
                _state.value = _state.value.copy(
                    isDeletingAvatar = false,
                    error = "Не удалось удалить аватар",
                )
            }
        }
    }
}
