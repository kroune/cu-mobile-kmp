package io.github.kroune.cumobile.presentation.files.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.kroune.cumobile.data.local.DownloadedFileInfo
import io.github.kroune.cumobile.presentation.common.ui.CuMobileTheme
import io.github.kroune.cumobile.presentation.files.FilesComponent

private val previewFilesState = FilesComponent.State(
    files = listOf(
        DownloadedFileInfo(
            name = "homework_1.pdf",
            path = "/files/homework_1.pdf",
            sizeBytes = 1_200_000,
            lastModifiedMillis = 1710806400000L,
        ),
    ),
)

@Preview
@Composable
private fun PreviewFilesLoadErrorDark() {
    CuMobileTheme(darkTheme = true) {
        FilesScreenContent(
            state = FilesComponent.State(error = "Не удалось загрузить список файлов"),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesLoadErrorLight() {
    CuMobileTheme(darkTheme = false) {
        FilesScreenContent(
            state = FilesComponent.State(error = "Не удалось загрузить список файлов"),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesActionErrorDark() {
    CuMobileTheme(darkTheme = true) {
        FilesScreenContent(
            state = previewFilesState,
            actionError = "Не удалось удалить файл",
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesActionErrorLight() {
    CuMobileTheme(darkTheme = false) {
        FilesScreenContent(
            state = previewFilesState,
            actionError = "Не удалось удалить файл",
            onIntent = {},
            onDismissError = {},
        )
    }
}

private val previewFilesSuccessState = FilesComponent.State(
    files = listOf(
        DownloadedFileInfo(
            name = "homework_1.pdf",
            path = "/files/homework_1.pdf",
            sizeBytes = 1_200_000,
            lastModifiedMillis = 1710806400000L,
        ),
        DownloadedFileInfo(
            name = "lecture_notes.docx",
            path = "/files/lecture_notes.docx",
            sizeBytes = 350_000,
            lastModifiedMillis = 1710720000000L,
        ),
        DownloadedFileInfo(
            name = "data_analysis.xlsx",
            path = "/files/data_analysis.xlsx",
            sizeBytes = 89_000,
            lastModifiedMillis = 1710633600000L,
        ),
        DownloadedFileInfo(
            name = "presentation.pptx",
            path = "/files/presentation.pptx",
            sizeBytes = 5_400_000,
            lastModifiedMillis = 1710547200000L,
        ),
        DownloadedFileInfo(
            name = "screenshot.png",
            path = "/files/screenshot.png",
            sizeBytes = 450_000,
            lastModifiedMillis = 1710460800000L,
        ),
    ),
)

@Preview
@Composable
private fun PreviewFilesSuccessDark() {
    CuMobileTheme(darkTheme = true) {
        FilesScreenContent(
            state = previewFilesSuccessState,
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesSuccessLight() {
    CuMobileTheme(darkTheme = false) {
        FilesScreenContent(
            state = previewFilesSuccessState,
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesEmptyDark() {
    CuMobileTheme(darkTheme = true) {
        FilesScreenContent(
            state = FilesComponent.State(),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesEmptyLight() {
    CuMobileTheme(darkTheme = false) {
        FilesScreenContent(
            state = FilesComponent.State(),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesLoadingDark() {
    CuMobileTheme(darkTheme = true) {
        FilesScreenContent(
            state = FilesComponent.State(isLoading = true),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesSelectingDark() {
    CuMobileTheme(darkTheme = true) {
        FilesScreenContent(
            state = previewFilesSuccessState.copy(
                selectedFiles = setOf("homework_1.pdf", "data_analysis.xlsx"),
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesSelectingLight() {
    CuMobileTheme(darkTheme = false) {
        FilesScreenContent(
            state = previewFilesSuccessState.copy(
                selectedFiles = setOf("homework_1.pdf", "data_analysis.xlsx"),
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesDownloadingDark() {
    CuMobileTheme(darkTheme = true) {
        FilesScreenContent(
            state = previewFilesSuccessState.copy(
                downloadingFiles = setOf("new_material.pdf"),
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesDownloadingLight() {
    CuMobileTheme(darkTheme = false) {
        FilesScreenContent(
            state = previewFilesSuccessState.copy(
                downloadingFiles = setOf("new_material.pdf"),
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesHighlightedDark() {
    CuMobileTheme(darkTheme = true) {
        FilesScreenContent(
            state = previewFilesSuccessState.copy(
                highlightedFile = "homework_1.pdf",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}

@Preview
@Composable
private fun PreviewFilesHighlightedLight() {
    CuMobileTheme(darkTheme = false) {
        FilesScreenContent(
            state = previewFilesSuccessState.copy(
                highlightedFile = "homework_1.pdf",
            ),
            actionError = null,
            onIntent = {},
            onDismissError = {},
        )
    }
}
