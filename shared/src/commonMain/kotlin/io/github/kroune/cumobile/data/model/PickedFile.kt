package io.github.kroune.cumobile.data.model

/**
 * Represents a file selected by the user via a platform file picker.
 *
 * @property name Display name of the file (e.g. "report.pdf").
 * @property bytes Raw file content.
 * @property contentType MIME type (e.g. "application/pdf").
 * @property size File size in bytes.
 */
data class PickedFile(
    val name: String,
    val bytes: ByteArray,
    val contentType: String,
    val size: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedFile) return false
        return name == other.name && size == other.size && contentType == other.contentType
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}
