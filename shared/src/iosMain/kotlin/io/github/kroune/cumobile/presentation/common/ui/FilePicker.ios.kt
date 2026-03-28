package io.github.kroune.cumobile.presentation.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.kroune.cumobile.data.model.PickedFile
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.lastPathComponent
import platform.Foundation.pathExtension
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject
import platform.posix.memcpy

private val logger = KotlinLogging.logger {}

@Composable
actual fun rememberFilePicker(onFilePicked: (PickedFile) -> Unit): FilePicker {
    val delegate = remember { IosFilePickerDelegate(onFilePicked) }
    return remember {
        object : FilePicker {
            override fun launch() {
                presentDocumentPicker(delegate)
            }
        }
    }
}

private fun presentDocumentPicker(delegate: IosFilePickerDelegate) {
    val picker = UIDocumentPickerViewController(
        forOpeningContentTypes = listOf(UTTypeData),
    )
    picker.delegate = delegate
    picker.allowsMultipleSelection = false
    val rootVc = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootVc?.presentViewController(picker, animated = true, completion = null)
}

private class IosFilePickerDelegate(
    private val onFilePicked: (PickedFile) -> Unit,
) : NSObject(),
    UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        readFileFromUrl(url)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun readFileFromUrl(url: NSURL) {
        try {
            val accessing = url.startAccessingSecurityScopedResource()
            val data = NSData.dataWithContentsOfURL(url)
            if (accessing) url.stopAccessingSecurityScopedResource()

            if (data == null || data.length.toInt() == 0) {
                logger.warn { "Failed to read file data from $url" }
                return
            }
            val bytes = ByteArray(data.length.toInt())
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
            val name = url.lastPathComponent ?: "file"
            val contentType = mimeTypeFromExtension(url.pathExtension ?: "")
            onFilePicked(PickedFile(name, bytes, contentType, bytes.size.toLong()))
        } catch (e: Exception) {
            logger.error(e) { "Failed to pick file from $url" }
        }
    }
}

private fun mimeTypeFromExtension(ext: String): String =
    when (ext.lowercase()) {
        "pdf" -> "application/pdf"
        "doc", "docx" -> "application/msword"
        "xls", "xlsx" -> "application/vnd.ms-excel"
        "ppt", "pptx" -> "application/vnd.ms-powerpoint"
        "txt" -> "text/plain"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "zip" -> "application/zip"
        "rar" -> "application/x-rar-compressed"
        else -> "application/octet-stream"
    }
