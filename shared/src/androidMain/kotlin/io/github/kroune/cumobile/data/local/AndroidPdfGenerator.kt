@file:Suppress("MagicNumber")

package io.github.kroune.cumobile.data.local

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private val logger = KotlinLogging.logger {}

/**
 * Android implementation of [PdfGenerator].
 *
 * Uses [android.graphics.pdf.PdfDocument] to create PDF files
 * from image pages with rotation and optional compression.
 */
class AndroidPdfGenerator(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : PdfGenerator {
    override suspend fun generatePdf(
        pages: List<PdfPageInput>,
        compress: Boolean,
    ): ByteArray? =
        withContext(defaultDispatcher) {
            try {
                val document = PdfDocument()

                for ((index, page) in pages.withIndex()) {
                    var bitmap = BitmapFactory.decodeByteArray(
                        page.imageBytes,
                        0,
                        page.imageBytes.size,
                    ) ?: continue

                    bitmap = applyRotation(bitmap, page.rotationDegrees)

                    if (compress) {
                        bitmap = compressBitmap(bitmap)
                    }

                    val pageInfo = PdfDocument.PageInfo
                        .Builder(
                            bitmap.width,
                            bitmap.height,
                            index + 1,
                        ).create()

                    val pdfPage = document.startPage(pageInfo)
                    pdfPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    document.finishPage(pdfPage)
                }

                val outputStream = ByteArrayOutputStream()
                document.writeTo(outputStream)
                document.close()
                outputStream.toByteArray()
            } catch (e: Exception) {
                logger.error(e) { "Failed to generate PDF" }
                null
            }
        }

    private fun applyRotation(
        bitmap: Bitmap,
        degrees: Float,
    ): Bitmap {
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true,
        )
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val maxDimension = 1920
        if (bitmap.width <= maxDimension && bitmap.height <= maxDimension) {
            return bitmap
        }
        val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true,
        )
    }
}
