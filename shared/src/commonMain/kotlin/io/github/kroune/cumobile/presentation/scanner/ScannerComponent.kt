package io.github.kroune.cumobile.presentation.scanner

import com.arkivanov.decompose.value.Value
import io.github.kroune.cumobile.data.model.PickedFile
import kotlinx.coroutines.flow.Flow

/**
 * MVI component for the document scanner screen.
 *
 * Manages captured images (pages), rotation editing,
 * and PDF generation.
 */
interface ScannerComponent {
    val state: Value<State>
    val effects: Flow<Effect>

    fun onIntent(intent: Intent)

    /**
     * A single scanned page.
     *
     * @property id Unique identifier within this session.
     * @property imageBytes Raw image bytes.
     * @property originalName Original filename from camera/gallery.
     * @property rotationDegrees Rotation applied by user (degrees, clockwise).
     */
    class ScanPage(
        val id: String,
        val imageBytes: ByteArray,
        val originalName: String,
        val rotationDegrees: Float = 0f,
    )

    data class State(
        val pages: List<ScanPage> = emptyList(),
        val fileName: String = "",
        val compressImages: Boolean = false,
        val editingPageIndex: Int = -1,
        val isSaving: Boolean = false,
        val isEditing: Boolean = false,
        val editingPage: ScanPage? = null,
        val canSave: Boolean = false,
    )

    sealed interface Intent {
        /** Page management intents. */
        sealed interface Page : Intent {
            data class Add(
                val images: List<PickedFile>,
            ) : Page

            data class Remove(
                val index: Int,
            ) : Page

            data class MoveUp(
                val index: Int,
            ) : Page

            data class MoveDown(
                val index: Int,
            ) : Page
        }

        /** Editor intents (rotation, open/close editor). */
        sealed interface Editor : Intent {
            data class Open(
                val index: Int,
            ) : Editor

            data class UpdateRotation(
                val degrees: Float,
            ) : Editor

            data object Close : Editor
        }

        /** Settings intents (filename, compression). */
        sealed interface Settings : Intent {
            data class UpdateFileName(
                val name: String,
            ) : Settings

            data class SetCompression(
                val enabled: Boolean,
            ) : Settings
        }

        /** Generate PDF and save. */
        data object SavePdf : Intent
    }

    sealed interface Effect {
        data class ShowError(
            val message: String,
        ) : Effect

        data object SaveSuccess : Effect
    }
}
