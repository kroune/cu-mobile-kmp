package io.github.kroune.cumobile.presentation.files

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.domain.repository.FileRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
    private val fileRepository: FileRepository,
    private val onOpenFile: (path: String) -> Unit = {},
) : FilesComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private val _state = MutableValue(FilesComponent.State())
    override val state: Value<FilesComponent.State> = _state

    override fun onIntent(intent: FilesComponent.Intent) {
        when (intent) {
            FilesComponent.Intent.Refresh -> loadFiles()
            is FilesComponent.Intent.DeleteFile -> deleteFile(intent.name)
            FilesComponent.Intent.DeleteAll -> deleteAllFiles()
            is FilesComponent.Intent.ToggleSelect -> toggleSelect(intent.name)
            FilesComponent.Intent.DeleteSelected -> deleteSelected()
            FilesComponent.Intent.ClearSelection -> clearSelection()
            is FilesComponent.Intent.OpenFile -> onOpenFile(intent.path)
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
            try {
                val files = fileRepository.listDownloadedFiles()
                _state.value = _state.value.copy(
                    files = files,
                    isLoading = false,
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to load downloaded files" }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить список файлов",
                )
            }
        }
    }

    private fun deleteFile(name: String) {
        scope.launch {
            fileRepository.deleteFile(name)
            removeFileFromState(name)
        }
    }

    private fun deleteAllFiles() {
        scope.launch {
            fileRepository.deleteAllFiles()
            _state.value = _state.value.copy(
                files = emptyList(),
                selectedFiles = emptySet(),
            )
        }
    }

    private fun deleteSelected() {
        scope.launch {
            val toDelete = _state.value.selectedFiles.toList()
            for (name in toDelete) {
                fileRepository.deleteFile(name)
            }
            val remaining = _state.value.files.filter {
                it.name !in toDelete
            }
            _state.value = _state.value.copy(
                files = remaining,
                selectedFiles = emptySet(),
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
}
