package io.github.kroune.cumobile.presentation.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.kroune.cumobile.data.model.StudentProfile
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.dataOrNull
import io.github.kroune.cumobile.presentation.common.ui.ActionErrorBar
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.DetailTopBar
import io.github.kroune.cumobile.presentation.common.ui.ErrorContent
import io.github.kroune.cumobile.presentation.common.ui.ShimmerBox
import io.github.kroune.cumobile.presentation.common.ui.ShimmerCircle
import io.github.kroune.cumobile.presentation.common.ui.rememberFilePicker
import io.github.kroune.cumobile.presentation.profile.ProfileComponent
import kotlinx.coroutines.launch

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
    var actionError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        component.effects.collect { effect ->
            when (effect) {
                is ProfileComponent.Effect.ShowError -> {
                    actionError = effect.message
                }
            }
        }
    }

    ProfileScreenContent(
        state = state,
        actionError = actionError,
        onIntent = component::onIntent,
        onBack = onBack,
        onDismissError = { actionError = null },
        modifier = modifier,
    )
}

@Composable
internal fun ProfileScreenContent(
    state: ProfileComponent.State,
    actionError: String? = null,
    onIntent: (ProfileComponent.Intent) -> Unit,
    onBack: () -> Unit,
    onDismissError: () -> Unit = {},
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

        ActionErrorBar(error = actionError, onDismiss = onDismissError)

        when (val profileState = state.profile) {
            is ContentState.Loading -> ProfileScreenSkeleton()
            is ContentState.Error -> ErrorContent(
                error = profileState.message,
                onRetry = { onIntent(ProfileComponent.Intent.Refresh) },
            )
            is ContentState.Success -> ProfileContent(
                state = state,
                profile = profileState.data,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileComponent.State,
    profile: StudentProfile,
    onIntent: (ProfileComponent.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
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
        var showAvatarDialog by remember { mutableStateOf(false) }
        AvatarSection(
            initials = state.initials,
            avatarBytes = state.avatarBytes.dataOrNull,
            isBusy = state.isDeletingAvatar || state.isUploadingAvatar,
            onDelete = { onIntent(ProfileComponent.Intent.DeleteAvatar) },
            onUpload = { avatarPicker.launch() },
            onAvatarClick = { showAvatarDialog = true },
        )

        if (showAvatarDialog && state.hasAvatar) {
            AvatarFullScreenDialog(
                avatarBytes = state.avatarBytes.dataOrNull,
                onDismiss = { showAvatarDialog = false },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = profile.fullName,
            color = AppTheme.colors.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AvatarSection(
    initials: String,
    avatarBytes: ByteArray?,
    isBusy: Boolean,
    onDelete: () -> Unit,
    onUpload: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasAvatar = avatarBytes != null
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, AppTheme.colors.accent, CircleShape)
                .background(AppTheme.colors.accent.copy(alpha = 0.2f), CircleShape)
                .clickable(enabled = hasAvatar && !isBusy, onClick = onAvatarClick),
            contentAlignment = Alignment.Center,
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AppTheme.colors.accent,
                    strokeWidth = 2.dp,
                )
            } else if (avatarBytes != null) {
                AsyncImage(
                    model = avatarBytes,
                    contentDescription = "Аватар",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
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
                Icon(Icons.Filled.Close, "Удалить аватар", Modifier.size(16.dp), AppTheme.colors.textPrimary)
            }
        }

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
                Icon(Icons.Filled.Add, "Загрузить аватар", Modifier.size(18.dp), AppTheme.colors.background)
            }
        }
    }
}

@Composable
private fun AvatarFullScreenDialog(
    avatarBytes: ByteArray?,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarBytes != null) {
                AsyncImage(
                    model = avatarBytes,
                    contentDescription = "Аватар",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    state: ProfileComponent.State,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile.dataOrNull ?: return

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
            key(email.value) {
                Spacer(modifier = Modifier.height(12.dp))
                InfoRowWithBadge(
                    label = "Email",
                    value = maskEmail(email.value),
                    copyValue = email.value,
                    badge = email.type.ifEmpty { null },
                )
            }
        }

        // Phones
        profile.phones.forEach { phone ->
            key(phone.value) {
                Spacer(modifier = Modifier.height(12.dp))
                InfoRowWithBadge(
                    label = "Телефон",
                    value = maskPhone(phone.value),
                    copyValue = phone.value,
                    badge = phone.type.ifEmpty { null },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoRow(
    label: String,
    value: String,
    copyValue: String = value,
    modifier: Modifier = Modifier,
) {
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(copyValue))
                    scope.launch { tooltipState.show() }
                },
            ),
    ) {
        Text(
            text = label,
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = {
                PlainTooltip(
                    containerColor = AppTheme.colors.textSecondary,
                    contentColor = AppTheme.colors.background,
                ) { Text("Скопировано") }
            },
            state = tooltipState,
        ) {
            Text(
                text = value,
                color = AppTheme.colors.textPrimary,
                fontSize = 14.sp,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoRowWithBadge(
    label: String,
    value: String,
    badge: String?,
    copyValue: String = value,
    modifier: Modifier = Modifier,
) {
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(copyValue))
                    scope.launch { tooltipState.show() }
                },
            ),
    ) {
        Text(
            text = label,
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = {
                PlainTooltip(
                    containerColor = AppTheme.colors.textSecondary,
                    contentColor = AppTheme.colors.background,
                ) { Text("Скопировано") }
            },
            state = tooltipState,
        ) {
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
}

/**
 * Masks an email address, showing the first 3 characters and domain.
 * Example: "john.doe@example.com" -> "joh***@example.com"
 */
internal fun maskEmail(email: String): String {
    val atIndex = email.indexOf('@')
    if (atIndex <= EmailVisibleChars) return email
    val visible = email.substring(0, EmailVisibleChars)
    val domain = email.substring(atIndex)
    return "$visible***$domain"
}

/**
 * Masks a phone number, showing first 2 and last 2 digits.
 * Example: "+79001234567" -> "+79*******67"
 */
internal fun maskPhone(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    if (digits.length <= MinPhoneDigits) return phone
    val prefix = phone.take(PhonePrefixLength)
    val suffix = phone.takeLast(PhoneSuffixLength)
    val masked = "*".repeat(phone.length - PhonePrefixLength - PhoneSuffixLength)
    return "$prefix$masked$suffix"
}

private const val EmailVisibleChars = 3
private const val MinPhoneDigits = 4
private const val PhonePrefixLength = 3
private const val PhoneSuffixLength = 2

private const val ProfileInfoRowCount = 4

/**
 * Skeleton loading state for the Profile screen.
 *
 * Shows shimmer placeholders for avatar circle, name, course info,
 * and info card with label/value rows.
 */
@Composable
internal fun ProfileScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))

        ShimmerCircle(size = 80.dp)

        Spacer(Modifier.height(16.dp))

        ShimmerBox(Modifier.width(160.dp), height = 20.dp)
        Spacer(Modifier.height(4.dp))
        ShimmerBox(Modifier.width(120.dp), height = 14.dp)

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface, RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            repeat(ProfileInfoRowCount) { index ->
                if (index > 0) Spacer(Modifier.height(12.dp))
                ShimmerBox(Modifier.width(60.dp), height = 12.dp)
                Spacer(Modifier.height(2.dp))
                ShimmerBox(
                    Modifier.fillMaxWidth(0.7f),
                    height = 14.dp,
                )
            }
        }
    }
}
