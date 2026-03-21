package io.github.kroune.cumobile.presentation.main

import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.presentation.files.FilesComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Coordinates file download tracking and navigation between
 * detail screens and the Files tab.
 */
internal class FilesCoordinator(
    private val tabPages: Value<ChildPages<*, MainComponent.TabChild>>,
    private val selectTab: (Int) -> Unit,
    private val detailNavigation: StackNavigation<DefaultMainComponent.DetailConfig>,
    private val scope: CoroutineScope,
) {
    private var pendingHighlightFile: String? = null

    fun refreshFiles() {
        sendToFilesComponent(FilesComponent.Intent.Refresh)
    }

    fun notifyDownloadStart(filename: String) {
        sendToFilesComponent(FilesComponent.Intent.AddDownloading(filename))
    }

    fun notifyDownloadComplete(filename: String) {
        pendingHighlightFile = filename
        sendToFilesComponent(FilesComponent.Intent.RemoveDownloading(filename))
    }

    fun navigateToFilesWithHighlight() {
        clearDetailStack()
        selectTab(FILES_TAB_INDEX)
        val filename = pendingHighlightFile ?: return
        pendingHighlightFile = null
        scope.launch {
            // Yield to let Decompose instantiate the Files tab component
            kotlinx.coroutines.yield()
            sendToFilesComponent(FilesComponent.Intent.HighlightFile(filename))
        }
    }

    private fun clearDetailStack() {
        detailNavigation.navigate { listOf(it.first()) }
    }

    private fun sendToFilesComponent(intent: FilesComponent.Intent) {
        val filesItem = tabPages.value.items.getOrNull(FILES_TAB_INDEX)
        val filesChild = filesItem?.instance
        if (filesChild is MainComponent.TabChild.FilesChild) {
            filesChild.component.onIntent(intent)
        }
    }

    companion object {
        private const val FILES_TAB_INDEX = 3
    }
}
