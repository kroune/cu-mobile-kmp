package io.github.kroune.cumobile.presentation.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.model.EmailInfo
import io.github.kroune.cumobile.data.model.PhoneInfo
import io.github.kroune.cumobile.data.model.StudentProfile
import io.github.kroune.cumobile.presentation.common.ContentState
import io.github.kroune.cumobile.presentation.common.ui.AppTheme
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.common.ui.DetailTopBar
import io.github.kroune.cumobile.presentation.profile.AvatarData
import io.github.kroune.cumobile.presentation.profile.ProfileComponent

private val previewProfileState = ProfileComponent.State(
    profile = ContentState.Success(
        StudentProfile(
            firstName = "Иван",
            lastName = "Петров",
            educationLevel = "bachelor",
            course = 2,
            telegram = "@ipetrov",
        ),
    ),
    lmsProfile = ContentState.Success(null),
    avatar = ContentState.Success(null),
)

private val previewProfileFullState = ProfileComponent.State(
    profile = ContentState.Success(
        StudentProfile(
            firstName = "Иван",
            lastName = "Петров",
            middleName = "Сергеевич",
            educationLevel = "bachelor",
            course = 2,
            telegram = "@ipetrov",
            timeLogin = "ipetrov",
            emails = listOf(
                EmailInfo(
                    value = "ipetrov@edu.centraluniversity.ru",
                    type = "university",
                ),
                EmailInfo(value = "ivan.petrov@gmail.com", type = "personal"),
            ),
            phones = listOf(
                PhoneInfo(value = "+79001234567", type = "mobile"),
            ),
        ),
    ),
    lmsProfile = ContentState.Success(null),
    avatar = ContentState.Success(null),
)

@Preview
@Composable
private fun PreviewProfileScreenSkeletonDark() {
    CuMobileTheme(darkTheme = true) {
        Column(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            DetailTopBar(title = "Профиль", onBack = {})
            ProfileScreenSkeleton()
        }
    }
}

@Preview
@Composable
private fun PreviewProfileScreenSkeletonLight() {
    CuMobileTheme(darkTheme = false) {
        Column(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            DetailTopBar(title = "Профиль", onBack = {})
            ProfileScreenSkeleton()
        }
    }
}

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

@Preview
@Composable
private fun PreviewProfileLoadErrorDark() {
    CuMobileTheme(darkTheme = true) {
        ProfileScreenContent(
            state = ProfileComponent.State(
                profile = ContentState.Error("Не удалось загрузить профиль"),
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfileLoadErrorLight() {
    CuMobileTheme(darkTheme = false) {
        ProfileScreenContent(
            state = ProfileComponent.State(
                profile = ContentState.Error("Не удалось загрузить профиль"),
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfileActionErrorDark() {
    CuMobileTheme(darkTheme = true) {
        ProfileScreenContent(
            state = previewProfileState,
            actionError = "Не удалось загрузить аватар",
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfileActionErrorLight() {
    CuMobileTheme(darkTheme = false) {
        ProfileScreenContent(
            state = previewProfileState,
            actionError = "Не удалось загрузить аватар",
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfileLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        ProfileScreenContent(
            state = ProfileComponent.State(),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfileLoadingLight() {
    CuMobileTheme(darkTheme = false) {
        ProfileScreenContent(
            state = ProfileComponent.State(),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfileUploadingAvatarDark() {
    CuMobileTheme(darkTheme = true) {
        ProfileScreenContent(
            state = previewProfileState.copy(isUploadingAvatar = true),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfileDeletingAvatarDark() {
    CuMobileTheme(darkTheme = true) {
        ProfileScreenContent(
            state = previewProfileState.copy(
                avatar = ContentState.Success(AvatarData(bytes = ByteArray(0), bitmap = null)),
                isDeletingAvatar = true,
            ),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfileFullDataDark() {
    CuMobileTheme(darkTheme = true) {
        ProfileScreenContent(
            state = previewProfileFullState,
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfileFullDataLight() {
    CuMobileTheme(darkTheme = false) {
        ProfileScreenContent(
            state = previewProfileFullState,
            onIntent = {},
            onBack = {},
        )
    }
}
