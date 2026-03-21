@file:Suppress("MagicNumber")
@file:OptIn(ExperimentalForeignApi::class)

package io.github.kroune.cumobile.data.local

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGContextRotateCTM
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSMutableData
import platform.Foundation.appendBytes
import platform.UIKit.UIGraphicsBeginImageContext
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPageWithInfo
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.posix.memcpy
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val logger = KotlinLogging.logger {}

/**
 * iOS implementation of [PdfGenerator].
 *
 * Uses UIKit PDF context to render images into a PDF document.
 */
class IosPdfGenerator(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : PdfGenerator {
    override suspend fun generatePdf(
        pages: List<PdfPageInput>,
        compress: Boolean,
    ): ByteArray? =
        withContext(defaultDispatcher) {
            try {
                val pdfData = NSMutableData()
                UIGraphicsBeginPDFContextToData(pdfData, CGRectMake(0.0, 0.0, 0.0, 0.0), null)
                try {
                    val processedImages = pages.mapNotNull { page -> processPage(page) }
                    for (image in processedImages) {
                        val imgWidth = image.size.useContents { width }
                        val imgHeight = image.size.useContents { height }
                        val pageRect = CGRectMake(0.0, 0.0, imgWidth, imgHeight)
                        UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                        image.drawInRect(pageRect)
                    }
                } finally {
                    UIGraphicsEndPDFContext()
                }

                val length = pdfData.length.toInt()
                if (length == 0) return@withContext null

                val bytes = ByteArray(length)
                bytes.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), pdfData.bytes, pdfData.length)
                }
                bytes
            } catch (e: Exception) {
                logger.error(e) { "Failed to generate PDF on iOS" }
                null
            }
        }

    private fun processPage(page: PdfPageInput): UIImage? {
        val nsData = NSMutableData().apply {
            if (page.imageBytes.isNotEmpty()) {
                page.imageBytes.usePinned { pinned ->
                    appendBytes(pinned.addressOf(0), page.imageBytes.size.toULong())
                }
            }
        }
        val image = UIImage(data = nsData) ?: return null
        if (page.rotationDegrees == 0f) return image
        return rotateImage(image, page.rotationDegrees)
    }

    private fun rotateImage(
        image: UIImage,
        degrees: Float,
    ): UIImage? {
        val radians = degrees * PI / 180.0
        val imgWidth = image.size.useContents { width }
        val imgHeight = image.size.useContents { height }
        val rotatedSize = CGSizeMake(
            abs(imgWidth * cos(radians)) + abs(imgHeight * sin(radians)),
            abs(imgWidth * sin(radians)) + abs(imgHeight * cos(radians)),
        )

        UIGraphicsBeginImageContext(rotatedSize)
        val context = UIGraphicsGetCurrentContext()
        if (context == null) {
            UIGraphicsEndImageContext()
            return null
        }

        val rotW = rotatedSize.useContents { width }
        val rotH = rotatedSize.useContents { height }
        CGContextTranslateCTM(context, rotW / 2.0, rotH / 2.0)
        CGContextRotateCTM(context, radians)
        image.drawInRect(
            CGRectMake(-imgWidth / 2.0, -imgHeight / 2.0, imgWidth, imgHeight),
        )

        val rotatedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return rotatedImage
    }
}
