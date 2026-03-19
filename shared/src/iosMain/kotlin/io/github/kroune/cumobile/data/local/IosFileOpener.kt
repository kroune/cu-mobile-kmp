package io.github.kroune.cumobile.data.local

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * iOS implementation of [FileOpener].
 *
 * TODO: Implement with UIDocumentInteractionController.
 */
internal class IosFileOpener : FileOpener {
    override fun openFile(path: String): Boolean {
        logger.info { "File opening not yet implemented on iOS: $path" }
        return false
    }
}
