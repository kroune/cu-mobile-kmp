package io.github.kroune.cumobile.presentation.profile

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.model.LmsProfileUi
import io.github.kroune.cumobile.presentation.common.model.PickedFileUi
import io.github.kroune.cumobile.presentation.common.model.ProfileUi
import kotlinx.coroutines.flow.Flow

/**
 * MVI component for the profile screen.
 *
 * Displays the student's profile data (name, course, education level,
 * contact info) and avatar management (view/delete).
 */
interface ProfileComponent {
    val state: Value<State>
    val effects: Flow<Effect>

    fun onIntent(intent: Intent)

    sealed interface Effect {
        data class ShowError(
            val message: String,
        ) : Effect
    }

    data class State(
        val profile: ContentState<ProfileUi> = ContentState.Loading,
        val lmsProfile: ContentState<LmsProfileUi?> = ContentState.Loading,
        val avatarUrl: String = "",
        val isDeletingAvatar: Boolean = false,
        val isUploadingAvatar: Boolean = false,
    )

    sealed interface Intent {
        data object Back : Intent

        data object Refresh : Intent

        data class UploadAvatar(
            val file: PickedFileUi,
        ) : Intent

        data object DeleteAvatar : Intent

        data object Logout : Intent
    }
}
