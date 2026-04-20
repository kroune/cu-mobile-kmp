package io.github.kroune.cumobile.presentation.files

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import io.github.kroune.cumobile.domain.repository.FileRepository
import io.github.kroune.cumobile.presentation.common.componentScope
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [FilesComponent].
 *
 * Manages downloaded files: listing, deleting (single / selected / all),
 * and toggling selection mode. File opening is delegated to the
 * platform via [onOpenFile].
 */
class DefaultFilesComponent(
    componentContext: ComponentContext,
    private val fileRepository: Lazy<FileRepository>,
    private val onOpenFile: (path: String) -> Unit = {},
    private val onOpenRenameSettings: () -> Unit = {},
    private val onOpenScanner: () -> Unit = {},
) : FilesComponent,
    ComponentContext by componentContext {
    private val scope = componentScope()

    private val _state = MutableValue(FilesComponent.State())
    override val state: Value<FilesComponent.State> = _state

    private val _effects = Channel<FilesComponent.Effect>(Channel.BUFFERED)
    override val effects: Flow<FilesComponent.Effect> = _effects.receiveAsFlow()

    override fun onIntent(intent: FilesComponent.Intent) {
        when (intent) {
            FilesComponent.Intent.Refresh -> loadFiles()
            is FilesComponent.Intent.DeleteFile -> deleteFile(intent.name)
            FilesComponent.Intent.DeleteAll -> deleteAllFiles()
            is FilesComponent.Intent.ToggleSelect -> toggleSelect(intent.name)
            FilesComponent.Intent.DeleteSelected -> deleteSelected()
            FilesComponent.Intent.ClearSelection -> clearSelection()
            is FilesComponent.Intent.OpenFile -> onOpenFile(intent.path)
            FilesComponent.Intent.OpenRenameSettings -> onOpenRenameSettings()
            FilesComponent.Intent.OpenScanner -> onOpenScanner()
            is FilesComponent.Intent.AddDownloading -> addDownloading(intent.name)
            is FilesComponent.Intent.RemoveDownloading -> removeDownloading(intent.name)
            is FilesComponent.Intent.HighlightFile -> highlightFile(intent.name)
        }
    }

    init {
        lifecycle.subscribe(
            object : Lifecycle.Callbacks {
                override fun onStart() {
                    loadFiles()
                }
            },
        )
    }

    private fun loadFiles() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val start = TimeSource.Monotonic.markNow()
            runCatchingCancellable {
                fileRepository().listDownloadedFiles()
            }.fold(
                onSuccess = { files ->
                    val remaining = MIN_LOADING_DURATION_MS - start.elapsedNow().inWholeMilliseconds
                    if (remaining > 0) delay(remaining)
                    _state.value = _state.value.copy(
                        files = files,
                        isLoading = false,
                    )
                },
                onFailure = { e ->
                    logger.error(e) { "Failed to load downloaded files" }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Не удалось загрузить список файлов",
                    )
                },
            )
        }
    }

    private fun deleteFile(name: String) {
        scope.launch {
            runCatchingCancellable {
                fileRepository().deleteFile(name)
            }.onSuccess {
                removeFileFromState(name)
            }.onFailure { e ->
                logger.error(e) { "Failed to delete file: $name" }
                _effects.trySend(
                    FilesComponent.Effect.ShowError("Не удалось удалить файл"),
                )
            }
        }
    }

    private fun deleteAllFiles() {
        scope.launch {
            runCatchingCancellable {
                fileRepository().deleteAllFiles()
            }.onSuccess {
                _state.value = _state.value.copy(
                    files = emptyList(),
                    selectedFiles = emptySet(),
                )
            }.onFailure { e ->
                logger.error(e) { "Failed to delete all files" }
                _effects.trySend(
                    FilesComponent.Effect.ShowError("Не удалось удалить файлы"),
                )
            }
        }
    }

    private fun deleteSelected() {
        scope.launch {
            runCatchingCancellable {
                val toDelete = _state.value.selectedFiles.toList()
                for (name in toDelete) {
                    fileRepository().deleteFile(name)
                }
                toDelete
            }.fold(
                onSuccess = { toDelete ->
                    val remaining = _state.value.files.filter {
                        it.name !in toDelete
                    }
                    _state.value = _state.value.copy(
                        files = remaining,
                        selectedFiles = emptySet(),
                    )
                },
                onFailure = { e ->
                    logger.error(e) { "Failed to delete selected files" }
                    _effects.trySend(
                        FilesComponent.Effect.ShowError("Не удалось удалить выбранные файлы"),
                    )
                },
            )
        }
    }

    private fun toggleSelect(name: String) {
        val current = _state.value.selectedFiles
        val updated = if (name in current) {
            current - name
        } else {
            current + name
        }
        _state.value = _state.value.copy(selectedFiles = updated)
    }

    private fun clearSelection() {
        _state.value = _state.value.copy(selectedFiles = emptySet())
    }

    private fun removeFileFromState(name: String) {
        _state.value = _state.value.copy(
            files = _state.value.files.filter { it.name != name },
            selectedFiles = _state.value.selectedFiles - name,
        )
    }

    private fun addDownloading(name: String) {
        _state.value = _state.value.copy(
            downloadingFiles = _state.value.downloadingFiles + name,
        )
    }

    private fun removeDownloading(name: String) {
        _state.value = _state.value.copy(
            downloadingFiles = _state.value.downloadingFiles - name,
        )
    }

    private fun highlightFile(name: String) {
        _state.value = _state.value.copy(highlightedFile = name)
        scope.launch {
            delay(HIGHLIGHT_DURATION_MS)
            if (_state.value.highlightedFile == name) {
                _state.value = _state.value.copy(highlightedFile = null)
            }
        }
    }

    companion object {
        private const val HIGHLIGHT_DURATION_MS = 2_000L
        private const val MIN_LOADING_DURATION_MS = 400L
    }
}
