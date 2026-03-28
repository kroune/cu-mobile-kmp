package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.runtime.Composable
import io.github.kroune.cumobile.data.model.PickedFile

/** Handle returned by [rememberFilePicker] to trigger the file picker dialog. */
interface FilePicker {
    fun launch()
}

/**
 * Remembers a platform file-picker launcher.
 *
 * Call [FilePicker.launch] to open the system file picker.
 * When the user selects a file, [onFilePicked] is invoked with the file metadata and bytes.
 */
@Composable
expect fun rememberFilePicker(onFilePicked: (PickedFile) -> Unit): FilePicker
