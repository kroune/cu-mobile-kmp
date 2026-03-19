@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.StudentProfile
import io.github.kroune.cumobile.presentation.common.AppTheme
import io.github.kroune.cumobile.presentation.common.CuMobileTheme
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.presentation.common.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent
import io.github.kroune.cumobile.presentation.common.rememberFilePicker

/**
 * Profile screen displaying student info, avatar, and logout button.
 *
 * Layout:
 * 1. Back button + logout button
 * 2. Avatar circle with initials and delete action
 * 3. Name + course + education level
 * 4. Info card: login, telegram, university email, other emails, phones
 */
@Composable
fun ProfileScreen(
    component: ProfileComponent,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()

    ProfileScreenContent(
        state = state,
        onIntent = component::onIntent,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun ProfileScreenContent(
    state: ProfileComponent.State,
    onIntent: (ProfileComponent.Intent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        DetailTopBar(
            title = "Профиль",
            onBack = onBack,
            trailingContent = {
                TextButton(
                    onClick = { onIntent(ProfileComponent.Intent.Logout) },
                ) {
                    Text(
                        text = "Выйти",
                        color = AppTheme.colors.error,
                        fontSize = 14.sp,
                    )
                }
            },
        )

        when {
            state.isLoading -> LoadingContent()
            state.error != null && state.profile == null -> ErrorContent(
                error = state.error.orEmpty(),
                onRetry = { onIntent(ProfileComponent.Intent.Refresh) },
            )
            state.profile != null -> ProfileContent(
                state = state,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileComponent.State,
    onIntent: (ProfileComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile ?: return
    val avatarPicker = rememberFilePicker { file ->
        onIntent(ProfileComponent.Intent.UploadAvatar(file))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Avatar section
        AvatarSection(
            initials = state.initials,
            hasAvatar = state.avatarBytes != null,
            isDeleting = state.isDeletingAvatar,
            isUploading = state.isUploadingAvatar,
            onDelete = { onIntent(ProfileComponent.Intent.DeleteAvatar) },
            onUpload = { avatarPicker.launch() },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = profile.fullName,
            color = AppTheme.colors.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        // Course + education level
        val courseNumber = profile.course
        if ((courseNumber != null && courseNumber > 0) || state.educationLevelLabel.isNotEmpty()) {
            val courseText = buildString {
                if (courseNumber != null && courseNumber > 0) append("$courseNumber курс")
                if (state.educationLevelLabel.isNotEmpty()) {
                    if (isNotEmpty()) append(" — ")
                    append(state.educationLevelLabel)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = courseText,
                color = AppTheme.colors.textSecondary,
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info card
        InfoCard(state)

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar section
        CalendarSection(
            state = state,
            onUpdateUrl = { url ->
                onIntent(ProfileComponent.Intent.UpdateCalendarUrl(url))
            },
            onSave = { onIntent(ProfileComponent.Intent.SaveCalendarUrl) },
            onDisconnect = {
                onIntent(ProfileComponent.Intent.DisconnectCalendar)
            },
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AvatarSection(
    initials: String,
    hasAvatar: Boolean,
    isDeleting: Boolean,
    isUploading: Boolean,
    onDelete: () -> Unit,
    onUpload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isBusy = isDeleting || isUploading
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Main avatar circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, AppTheme.colors.accent, CircleShape)
                .background(AppTheme.colors.accent.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AppTheme.colors.accent,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = initials.ifEmpty { "?" },
                    color = AppTheme.colors.accent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Delete button (bottom-left of avatar)
        if (hasAvatar && !isBusy) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.error, CircleShape)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "\u2715", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
            }
        }

        // Upload button (bottom-right of avatar)
        if (!isBusy) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.accent, CircleShape)
                    .clickable(onClick = onUpload),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "+", fontSize = 16.sp, color = AppTheme.colors.background)
            }
        }
    }
}

@Composable
private fun InfoCard(
    state: ProfileComponent.State,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        // Login
        InfoRow(label = "Логин", value = profile.timeLogin)

        // Telegram
        if (!profile.telegram.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Telegram", value = "@${profile.telegram}")
        }

        // University email
        val uniEmail = profile.universityEmail
        if (uniEmail != null) {
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Email ЦУ", value = uniEmail)
        }

        // Other emails
        state.otherEmails.forEach { email ->
            Spacer(modifier = Modifier.height(12.dp))
            InfoRowWithBadge(
                label = "Email",
                value = maskEmail(email.value),
                badge = email.type.ifEmpty { null },
            )
        }

        // Phones
        profile.phones.forEach { phone ->
            Spacer(modifier = Modifier.height(12.dp))
            InfoRowWithBadge(
                label = "Телефон",
                value = maskPhone(phone.value),
                badge = phone.type.ifEmpty { null },
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = AppTheme.colors.textPrimary,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun InfoRowWithBadge(
    label: String,
    value: String,
    badge: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = badge,
                    color = AppTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .background(AppTheme.colors.background, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun CalendarSection(
    state: ProfileComponent.State,
    onUpdateUrl: (String) -> Unit,
    onSave: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Расписание (Яндекс Календарь)",
            color = AppTheme.colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )

        if (state.isCalendarConnected) {
            Text(
                text = "Подключено",
                color = AppTheme.colors.accent,
                fontSize = 12.sp,
            )
        }

        OutlinedTextField(
            value = state.calendarUrlInput,
            onValueChange = onUpdateUrl,
            label = { Text("ICS URL календаря") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppTheme.colors.textPrimary,
                unfocusedTextColor = AppTheme.colors.textPrimary,
                focusedBorderColor = AppTheme.colors.accent,
                unfocusedBorderColor = AppTheme.colors.textSecondary,
                focusedLabelColor = AppTheme.colors.accent,
                unfocusedLabelColor = AppTheme.colors.textSecondary,
                cursorColor = AppTheme.colors.accent,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onSave,
                enabled = state.calendarUrlInput.isNotBlank(),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.accent,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(text = "Сохранить", color = AppTheme.colors.background)
            }

            if (state.isCalendarConnected) {
                Button(
                    onClick = onDisconnect,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.error,
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(text = "Отключить", color = AppTheme.colors.textPrimary)
                }
            }
        }
    }
}

/**
 * Masks an email address, showing the first 3 characters and domain.
 * Example: "john.doe@example.com" → "joh***@example.com"
 */
internal fun maskEmail(email: String): String {
    val atIndex = email.indexOf('@')
    if (atIndex <= 3) return email
    val visible = email.substring(0, 3)
    val domain = email.substring(atIndex)
    return "$visible***$domain"
}

/**
 * Masks a phone number, showing first 2 and last 2 digits.
 * Example: "+79001234567" → "+79*******67"
 */
internal fun maskPhone(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    if (digits.length <= 4) return phone
    val prefix = phone.take(3)
    val suffix = phone.takeLast(2)
    val masked = "*".repeat(phone.length - 5)
    return "$prefix$masked$suffix"
}

private val previewProfileState = ProfileComponent.State(
    profile = StudentProfile(
        firstName = "Иван",
        lastName = "Петров",
        educationLevel = "bachelor",
        course = 2,
        telegram = "@ipetrov",
    ),
)

@Preview
@Composable
private fun PreviewProfileScreenDark() {
    CuMobileTheme(darkTheme = true) {
        ProfileScreenContent(state = previewProfileState, onIntent = {}, onBack = {})
    }
}

@Preview
@Composable
private fun PreviewProfileScreenLight() {
    CuMobileTheme(darkTheme = false) {
        ProfileScreenContent(state = previewProfileState, onIntent = {}, onBack = {})
    }
}
