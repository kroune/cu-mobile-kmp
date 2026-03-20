@file:Suppress("MagicNumber")
@file:OptIn(ExperimentalForeignApi::class)

package io.github.kroune.cumobile.data.local

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPageWithInfo
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIImage
import platform.posix.memcpy

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

                val processedImages = pages.mapNotNull { page -> processPage(page) }
                for (image in processedImages) {
                    val pageRect = CGRectMake(0.0, 0.0, image.size.width, image.size.height)
                    UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                    image.drawInRect(pageRect)
                }

                UIGraphicsEndPDFContext()

                val bytes = ByteArray(pdfData.length.toInt())
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
        val nsData = page.imageBytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = page.imageBytes.size.toULong(),
            )
        }
        val image = UIImage(data = nsData) ?: return null
        if (page.rotationDegrees == 0f) return image
        return rotateImage(image, page.rotationDegrees)
    }

    private fun rotateImage(
        image: UIImage,
        degrees: Float,
    ): UIImage? {
        val radians = degrees * kotlin.math.PI / 180.0
        val size = image.size
        val rotatedSize = CGSizeMake(
            kotlin.math.abs(size.width * kotlin.math.cos(radians)) +
                kotlin.math.abs(size.height * kotlin.math.sin(radians)),
            kotlin.math.abs(size.width * kotlin.math.sin(radians)) +
                kotlin.math.abs(size.height * kotlin.math.cos(radians)),
        )

        platform.UIKit.UIGraphicsBeginImageContext(rotatedSize)
        val context = platform.UIKit.UIGraphicsGetCurrentContext() ?: return null

        platform.CoreGraphics.CGContextTranslateCTM(
            context,
            rotatedSize.width / 2.0,
            rotatedSize.height / 2.0,
        )
        platform.CoreGraphics.CGContextRotateCTM(context, radians)
        image.drawInRect(
            CGRectMake(
                -size.width / 2.0,
                -size.height / 2.0,
                size.width,
                size.height,
            ),
        )

        val rotatedImage = platform.UIKit.UIGraphicsGetImageFromCurrentImageContext()
        platform.UIKit.UIGraphicsEndImageContext()
        return rotatedImage
    }
}
