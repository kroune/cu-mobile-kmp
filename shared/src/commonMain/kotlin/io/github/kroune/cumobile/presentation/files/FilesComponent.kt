package io.github.kroune.cumobile.presentation.files

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.local.DownloadedFileInfo
import kotlinx.coroutines.flow.Flow

/**
 * MVI component for the Files tab ("Файлы").
 *
 * Manages the list of locally downloaded files with selection,
 * deletion, and refresh actions.
 */
interface FilesComponent {
    val state: Value<State>
    val effects: Flow<Effect>

    fun onIntent(intent: Intent)

    sealed interface Effect {
        data class ShowError(
            val message: String,
        ) : Effect
    }

    data class State(
        val files: List<DownloadedFileInfo> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        /** Names of currently selected files (for batch delete). */
        val selectedFiles: Set<String> = emptySet(),
        /** Filenames currently being downloaded (shown as loading rows). */
        val downloadingFiles: Set<String> = emptySet(),
        /** Filename to highlight briefly after navigation (auto-clears). */
        val highlightedFile: String? = null,
    ) {
        /** Whether selection mode is active. */
        val isSelecting: Boolean
            get() = selectedFiles.isNotEmpty()

        /** Total size of all downloaded files in bytes. */
        val totalSizeBytes: Long
            get() = files.sumOf { it.sizeBytes }
    }

    sealed interface Intent {
        /** Reload the file list. */
        data object Refresh : Intent

        /** Delete a single file by name. */
        data class DeleteFile(
            val name: String,
        ) : Intent

        /** Delete all downloaded files. */
        data object DeleteAll : Intent

        /** Toggle selection state of a file. */
        data class ToggleSelect(
            val name: String,
        ) : Intent

        /** Delete all currently selected files. */
        data object DeleteSelected : Intent

        /** Clear selection. */
        data object ClearSelection : Intent

        /** Open a file (platform-handled). */
        data class OpenFile(
            val path: String,
        ) : Intent

        /** Open file rename settings. */
        data object OpenRenameSettings : Intent

        /** Open document scanner. */
        data object OpenScanner : Intent

        /** Mark a file as being downloaded (shows loading indicator). */
        data class AddDownloading(val name: String) : Intent

        /** Remove a file from downloading set. */
        data class RemoveDownloading(val name: String) : Intent

        /** Briefly highlight a file after navigation. */
        data class HighlightFile(val name: String) : Intent
    }
}
