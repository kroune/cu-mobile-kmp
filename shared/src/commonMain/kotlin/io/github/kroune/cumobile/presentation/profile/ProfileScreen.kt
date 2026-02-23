@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
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
import io.github.kroune.cumobile.presentation.common.AppColors
import io.github.kroune.cumobile.presentation.common.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ErrorContent
import io.github.kroune.cumobile.presentation.common.LoadingContent

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        DetailTopBar(
            title = "Профиль",
            onBack = onBack,
            trailingContent = {
                TextButton(
                    onClick = { component.onIntent(ProfileComponent.Intent.Logout) },
                ) {
                    Text(
                        text = "Выйти",
                        color = AppColors.Error,
                        fontSize = 14.sp,
                    )
                }
            },
        )

        when {
            state.isLoading -> LoadingContent()
            state.error != null && state.profile == null -> ErrorContent(
                error = state.error.orEmpty(),
                onRetry = { component.onIntent(ProfileComponent.Intent.Refresh) },
            )
            state.profile != null -> ProfileContent(
                state = state,
                onDeleteAvatar = { component.onIntent(ProfileComponent.Intent.DeleteAvatar) },
            )
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileComponent.State,
    onDeleteAvatar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile ?: return

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
            onDelete = onDeleteAvatar,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = profile.fullName,
            color = AppColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        // Course + education level
        if (profile.course > 0 || state.educationLevelLabel.isNotEmpty()) {
            val courseText = buildString {
                if (profile.course > 0) append("${profile.course} курс")
                if (state.educationLevelLabel.isNotEmpty()) {
                    if (isNotEmpty()) append(" — ")
                    append(state.educationLevelLabel)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = courseText,
                color = AppColors.TextSecondary,
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info card
        InfoCard(state)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AvatarSection(
    initials: String,
    hasAvatar: Boolean,
    isDeleting: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Main avatar circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, AppColors.Accent, CircleShape)
                .background(AppColors.Accent.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AppColors.Accent,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = initials.ifEmpty { "?" },
                    color = AppColors.Accent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Delete button (bottom-left of avatar)
        if (hasAvatar && !isDeleting) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AppColors.Error, CircleShape)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "\u2715", fontSize = 14.sp, color = AppColors.TextPrimary)
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
            .background(AppColors.Surface, RoundedCornerShape(12.dp))
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
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = AppColors.TextPrimary,
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
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = badge,
                    color = AppColors.TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .background(AppColors.Background, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
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
