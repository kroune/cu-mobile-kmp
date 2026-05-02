package io.github.kroune.cumobile.domain.model

data class PickedFileDomain(
    val name: String,
    val bytes: ByteArray,
    val contentType: String,
    val size: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedFileDomain) return false
        return name == other.name && size == other.size && contentType == other.contentType
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}
