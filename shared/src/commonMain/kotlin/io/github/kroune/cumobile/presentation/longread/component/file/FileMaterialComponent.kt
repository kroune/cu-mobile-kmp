package io.github.kroune.cumobile.presentation.longread.component.file

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.model.LongreadMaterial
import io.github.kroune.cumobile.domain.repository.ContentRepository
import io.github.kroune.cumobile.presentation.common.RenderComponent
import io.github.kroune.cumobile.presentation.longread.ui.file.FileMaterialCard
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

/**
 * Material component for downloadable file cards.
 *
 * Owns the full download flow: get link → resolve filename → save → report result.
 * [material] is immutable data fixed at creation time (not in State).
 */
class FileMaterialComponent(
    componentContext: ComponentContext,
    val material: LongreadMaterial,
    private val contentRepository: ContentRepository,
    private val resolveFilename: suspend () -> String,
    private val saveFile: suspend (url: String, filename: String) -> Boolean,
    private val onDownloadResult: (FileDownloadResult) -> Unit,
) : ComponentContext by componentContext,
    RenderComponent {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private fun download() {
        val filename = material.filename ?: return
        val version = material.version ?: "1"
        scope.launch {
            onDownloadResult(FileDownloadResult.Started)
            val url = contentRepository.getDownloadLink(filename, version)
            if (url != null) {
                val localFilename = resolveFilename()
                val saved = saveFile(url, localFilename)
                if (saved) {
                    onDownloadResult(FileDownloadResult.Success)
                } else {
                    onDownloadResult(
                        FileDownloadResult.Failed("Не удалось сохранить файл"),
                    )
                }
            } else {
                logger.warn { "Failed to get download link for $filename" }
                onDownloadResult(
                    FileDownloadResult.Failed("Не удалось получить ссылку для скачивания"),
                )
            }
        }
    }

    @Composable
    override fun Render() {
        FileMaterialCard(
            material = material,
            onDownload = { download() },
        )
    }
}

/** Result of a file download attempt, reported to the parent via callback. */
sealed interface FileDownloadResult {
    data object Started : FileDownloadResult

    data object Success : FileDownloadResult

    data class Failed(
        val message: String,
    ) : FileDownloadResult
}
