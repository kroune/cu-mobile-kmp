package io.github.kroune.cumobile.presentation.profile

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.PickedFile
import kotlinx.coroutines.flow.Flow
import io.github.kroune.cumobile.data.model.StudentLmsProfile
import io.github.kroune.cumobile.data.model.StudentProfile

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
        data class ShowError(val message: String) : Effect
    }

    data class State(
        val profile: StudentProfile? = null,
        val lmsProfile: StudentLmsProfile? = null,
        val avatarBytes: ByteArray? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isDeletingAvatar: Boolean = false,
        val isUploadingAvatar: Boolean = false,
        /** Current calendar ICS URL (null if not connected). */
        val calendarUrl: String? = null,
        /** Input text for calendar URL editing. */
        val calendarUrlInput: String = "",
    ) {
        /** User initials for avatar placeholder (first char of first + last name). */
        val initials: String
            get() {
                val p = profile ?: return ""
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
            get() = when (profile?.educationLevel?.lowercase()) {
                "bachelor" -> "Бакалавриат"
                "master" -> "Магистратура"
                "specialist" -> "Специалитет"
                else -> profile?.educationLevel.orEmpty()
            }

        /**
         * Non-university emails (all emails that are not the primary
         * university email).
         */
        val otherEmails: List<io.github.kroune.cumobile.data.model.EmailInfo>
            get() {
                val p = profile ?: return emptyList()
                val uni = p.universityEmail ?: return p.emails
                return p.emails.filter { it.value != uni }
            }

        /** Whether a calendar URL is currently saved. */
        val isCalendarConnected: Boolean
            get() = !calendarUrl.isNullOrBlank()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is State) return false
            return profile == other.profile &&
                lmsProfile == other.lmsProfile &&
                avatarBytes.contentEquals(other.avatarBytes) &&
                isLoading == other.isLoading &&
                error == other.error &&
                isDeletingAvatar == other.isDeletingAvatar &&
                isUploadingAvatar == other.isUploadingAvatar &&
                calendarUrl == other.calendarUrl &&
                calendarUrlInput == other.calendarUrlInput
        }

        override fun hashCode(): Int {
            var result = profile.hashCode()
            result = 31 * result + lmsProfile.hashCode()
            result = 31 * result + (avatarBytes?.contentHashCode() ?: 0)
            result = 31 * result + isLoading.hashCode()
            result = 31 * result + error.hashCode()
            result = 31 * result + isDeletingAvatar.hashCode()
            result = 31 * result + isUploadingAvatar.hashCode()
            result = 31 * result + calendarUrl.hashCode()
            result = 31 * result + calendarUrlInput.hashCode()
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

        /** Update the calendar URL input field. */
        data class UpdateCalendarUrl(
            val url: String,
        ) : Intent

        /** Save the current calendar URL input. */
        data object SaveCalendarUrl : Intent

        /** Disconnect (clear) the calendar. */
        data object DisconnectCalendar : Intent
    }
}
