package io.github.kroune.cumobile.presentation.common

/** Format a score, showing one decimal place only if needed. */
@Suppress("MagicNumber")
fun formatScore(value: Double): String {
    val rounded = (value * 10).toInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

/**
 * Formats a [Double] for display: shows as integer when whole, otherwise as-is.
 *
 * E.g. `5.0` -> `"5"`, `3.5` -> `"3.5"`.
 */
fun Double.displayScore(): String {
    val long = toLong()
    return if (this == long.toDouble()) long.toString() else toString()
}
