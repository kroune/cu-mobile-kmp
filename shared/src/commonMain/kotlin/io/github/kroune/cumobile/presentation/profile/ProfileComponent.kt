package io.github.kroune.cumobile.presentation.profile

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.PickedFile
import io.github.kroune.cumobile.data.model.StudentLmsProfile
import io.github.kroune.cumobile.data.model.StudentProfile
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.common.isLoading
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
        val profile: ContentState<StudentProfile> = ContentState.Loading,
        val lmsProfile: ContentState<StudentLmsProfile?> = ContentState.Loading,
        val avatarUrl: String = "",
        val isDeletingAvatar: Boolean = false,
        val isUploadingAvatar: Boolean = false,
    ) {
        /** Whether the important content (profile) is still loading. */
        val isContentLoading: Boolean
            get() = profile.isLoading

        /** Translated education level label. */
        val educationLevelLabel: String
            get() = when (profile.dataOrNull?.educationLevel?.lowercase()) {
                "bachelor" -> "Бакалавриат"
                "master" -> "Магистратура"
                "specialist" -> "Специалитет"
                else -> profile.dataOrNull?.educationLevel.orEmpty()
            }

        /**
         * Non-university emails (all emails that are not the primary
         * university email).
         */
        val otherEmails: List<io.github.kroune.cumobile.data.model.EmailInfo>
            get() {
                val p = profile.dataOrNull ?: return emptyList()
                val uni = p.universityEmail ?: return p.emails
                return p.emails.filter { it.value != uni }
            }
    }

    sealed interface Intent {
        data object Back : Intent

        data object Refresh : Intent

        data class UploadAvatar(
            val file: PickedFile,
        ) : Intent

        data object DeleteAvatar : Intent

        data object Logout : Intent
    }
}
