package io.github.kroune.cumobile.presentation.scanner

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.github.kroune.cumobile.data.local.FileStorage
import io.github.kroune.cumobile.data.local.PdfGenerator
import io.github.kroune.cumobile.data.local.PdfPageInput
import io.github.kroune.cumobile.data.model.PickedFile
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock.System

private val logger = KotlinLogging.logger {}

/**
 * Default implementation of [ScannerComponent].
 *
 * Handles page management, rotation editing, and PDF generation/saving.
 */
class DefaultScannerComponent(
    componentContext: ComponentContext,
    private val pdfGenerator: PdfGenerator,
    private val fileStorage: FileStorage,
    private val onBack: () -> Unit,
) : ScannerComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(
        Dispatchers.Main.immediate + SupervisorJob(),
    )

    private var pageCounter = 0

    private val _state = MutableValue(
        ScannerComponent.State(fileName = generateDefaultFileName()),
    )
    override val state: Value<ScannerComponent.State> = _state

    private val _effects = Channel<ScannerComponent.Effect>(Channel.BUFFERED)
    override val effects: Flow<ScannerComponent.Effect> = _effects.receiveAsFlow()

    override fun onIntent(intent: ScannerComponent.Intent) {
        when (intent) {
            is ScannerComponent.Intent.Page -> handlePageIntent(intent)
            is ScannerComponent.Intent.Editor -> handleEditorIntent(intent)
            is ScannerComponent.Intent.Settings -> handleSettingsIntent(intent)
            ScannerComponent.Intent.SavePdf -> savePdf()
        }
    }

    // region Page management

    private fun handlePageIntent(intent: ScannerComponent.Intent.Page) {
        when (intent) {
            is ScannerComponent.Intent.Page.Add -> addPages(intent.images)
            is ScannerComponent.Intent.Page.Remove -> removePage(intent.index)
            is ScannerComponent.Intent.Page.MoveUp -> movePage(intent.index, -1)
            is ScannerComponent.Intent.Page.MoveDown -> movePage(intent.index, 1)
        }
    }

    private fun addPages(images: List<PickedFile>) {
        val newPages = images.map { file ->
            ScannerComponent.ScanPage(
                id = "page_${pageCounter++}",
                imageBytes = file.bytes,
                originalName = file.name,
            )
        }
        _state.value = _state.value.copy(
            pages = _state.value.pages + newPages,
        )
    }

    private fun removePage(index: Int) {
        val current = _state.value.pages.toMutableList()
        if (index !in current.indices) return
        current.removeAt(index)
        val editing = _state.value.editingPageIndex
        _state.value = _state.value.copy(
            pages = current,
            editingPageIndex = when {
                editing == index -> -1
                editing > index -> editing - 1
                else -> editing
            },
        )
    }

    private fun movePage(
        index: Int,
        direction: Int,
    ) {
        val target = index + direction
        val pages = _state.value.pages.toMutableList()
        if (index !in pages.indices || target !in pages.indices) return
        val temp = pages[index]
        pages[index] = pages[target]
        pages[target] = temp
        _state.value = _state.value.copy(pages = pages)
    }

    // endregion

    // region Editor

    private fun handleEditorIntent(intent: ScannerComponent.Intent.Editor) {
        when (intent) {
            is ScannerComponent.Intent.Editor.Open -> {
                if (intent.index in _state.value.pages.indices) {
                    _state.value = _state.value.copy(editingPageIndex = intent.index)
                }
            }
            is ScannerComponent.Intent.Editor.UpdateRotation -> {
                val index = _state.value.editingPageIndex
                val pages = _state.value.pages.toMutableList()
                if (index !in pages.indices) return
                val page = pages[index]
                pages[index] = ScannerComponent.ScanPage(
                    page.id,
                    page.imageBytes,
                    page.originalName,
                    intent.degrees,
                )
                _state.value = _state.value.copy(pages = pages)
            }
            ScannerComponent.Intent.Editor.Close ->
                _state.value = _state.value.copy(editingPageIndex = -1)
        }
    }

    // endregion

    // region Settings

    private fun handleSettingsIntent(intent: ScannerComponent.Intent.Settings) {
        when (intent) {
            is ScannerComponent.Intent.Settings.UpdateFileName ->
                _state.value = _state.value.copy(fileName = intent.name)
            is ScannerComponent.Intent.Settings.SetCompression ->
                _state.value = _state.value.copy(compressImages = intent.enabled)
        }
    }

    // endregion

    // region Save PDF

    @Suppress("MagicNumber")
    private fun savePdf() {
        val currentState = _state.value
        if (currentState.pages.isEmpty() || currentState.isSaving) return

        _state.value = currentState.copy(isSaving = true)

        scope.launch {
            try {
                val pdfPages = currentState.pages.map { page ->
                    PdfPageInput(
                        imageBytes = page.imageBytes,
                        rotationDegrees = page.rotationDegrees,
                    )
                }

                val pdfBytes = pdfGenerator.generatePdf(
                    pages = pdfPages,
                    compress = currentState.compressImages,
                )

                if (pdfBytes == null) {
                    _state.value = _state.value.copy(isSaving = false)
                    _effects.trySend(
                        ScannerComponent.Effect.ShowError("Не удалось создать PDF"),
                    )
                    return@launch
                }

                val filename = buildUniqueFilename(currentState.fileName)
                val saved = fileStorage.saveFile(pdfBytes, filename)

                _state.value = _state.value.copy(isSaving = false)

                if (saved) {
                    _effects.trySend(ScannerComponent.Effect.SaveSuccess)
                    onBack()
                } else {
                    _effects.trySend(
                        ScannerComponent.Effect.ShowError("Не удалось сохранить PDF"),
                    )
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to save PDF" }
                _state.value = _state.value.copy(isSaving = false)
                _effects.trySend(
                    ScannerComponent.Effect.ShowError("Ошибка при сохранении: ${e.message}"),
                )
            }
        }
    }

    private fun buildUniqueFilename(rawName: String): String {
        val normalized = normalizeFileName(rawName)
        val base = "$normalized.pdf"
        if (!fileStorage.fileExists(base)) return base

        var counter = 1
        while (true) {
            val candidate = "${normalized}__dup$counter.pdf"
            if (!fileStorage.fileExists(candidate)) return candidate
            counter++
        }
    }

    // endregion

    companion object {
        private val invalidChars = Regex("[\\\\/:*?\"<>|]")

        fun normalizeFileName(name: String): String {
            val cleaned = name.replace(invalidChars, "").trim()
            return cleaned.ifEmpty { generateDefaultFileName() }
        }

        @Suppress("MagicNumber")
        fun generateDefaultFileName(): String {
            val now = System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
            val d = now.date
            val t = now.time

            @Suppress("DEPRECATION")
            val day = d.dayOfMonth.toString().padStart(2, '0')

            @Suppress("DEPRECATION")
            val month = d.monthNumber.toString().padStart(2, '0')
            val hour = t.hour.toString().padStart(2, '0')
            val minute = t.minute.toString().padStart(2, '0')
            return "Скан_$day.$month.${d.year}_$hour-$minute"
        }
    }
}
