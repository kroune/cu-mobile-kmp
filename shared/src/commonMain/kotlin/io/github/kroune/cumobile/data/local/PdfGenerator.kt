package io.github.kroune.cumobile.data.local

/**
 * Input data for a single PDF page.
 *
 * @property imageBytes Raw image bytes (JPEG/PNG).
 * @property rotationDegrees Rotation to apply before rendering (degrees, clockwise).
 */
class PdfPageInput(
    val imageBytes: ByteArray,
    val rotationDegrees: Float,
)

/**
 * Platform-specific PDF generator.
 *
 * Takes a list of image pages with rotation metadata,
 * applies transformations, and produces a PDF document.
 */
interface PdfGenerator {
    /**
     * Generates a PDF from the given image pages.
     *
     * @param pages List of images with rotation info.
     * @param compress If true, images are resized (max 1920px) and compressed.
     * @return PDF bytes, or null on failure.
     */
    suspend fun generatePdf(
        pages: List<PdfPageInput>,
        compress: Boolean,
    ): ByteArray?
}
