package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.runtime.Composable
import io.github.kroune.cumobile.data.model.PickedFile

/**
 * Handle returned by [rememberImagePicker] for capturing images
 * via camera or gallery.
 */
interface ImagePicker {
    /** Launch the device camera to capture a photo. */
    fun launchCamera()

    /** Launch the gallery to pick one or more images. */
    fun launchGallery()
}

/**
 * Remembers a platform image-picker launcher.
 *
 * When images are captured/selected, [onImagesCaptured] is invoked
 * with a list of [PickedFile] objects containing image bytes.
 */
@Composable
expect fun rememberImagePicker(onImagesCaptured: (List<PickedFile>) -> Unit): ImagePicker
