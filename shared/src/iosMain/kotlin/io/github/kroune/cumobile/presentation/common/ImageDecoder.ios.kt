package io.github.kroune.cumobile.presentation.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.skia.Image

private val logger = KotlinLogging.logger {}

actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap? =
    try {
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (
        @Suppress("TooGenericExceptionCaught") e: Exception,
    ) {
        logger.warn(e) { "Failed to decode image bitmap" }
        null
    }
