package io.github.kroune.cumobile.presentation.common

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Decodes raw image bytes (JPEG, PNG, etc.) into a Compose [ImageBitmap].
 *
 * @return decoded bitmap, or null if decoding fails.
 */
expect fun decodeImageBitmap(bytes: ByteArray): ImageBitmap?
