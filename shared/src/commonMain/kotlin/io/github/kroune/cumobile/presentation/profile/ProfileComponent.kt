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
        val avatarBytes: ContentState<ByteArray?> = ContentState.Loading,
        val isDeletingAvatar: Boolean = false,
        val isUploadingAvatar: Boolean = false,
    ) {
        /** Whether the important content (profile) is still loading. */
        val isContentLoading: Boolean
            get() = profile.isLoading

        /** Whether the user has an avatar. */
        val hasAvatar: Boolean
            get() = avatarBytes.dataOrNull != null

        /** User initials for avatar placeholder (first char of first + last name). */
        val initials: String
            get() {
                val p = profile.dataOrNull ?: return ""
                val first = p.firstName
                    .firstOrNull()
                    ?.uppercase()
                    .orEmpty()
                val last = p.lastName
                    .firstOrNull()
                    ?.uppercase()
                    .orEmpty()
                return "$first$last"
            }

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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is State) return false
            return profile == other.profile &&
                lmsProfile == other.lmsProfile &&
                avatarBytes == other.avatarBytes &&
                isDeletingAvatar == other.isDeletingAvatar &&
                isUploadingAvatar == other.isUploadingAvatar
        }

        override fun hashCode(): Int {
            var result = profile.hashCode()
            result = 31 * result + lmsProfile.hashCode()
            result = 31 * result + avatarBytes.hashCode()
            result = 31 * result + isDeletingAvatar.hashCode()
            result = 31 * result + isUploadingAvatar.hashCode()
            return result
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
