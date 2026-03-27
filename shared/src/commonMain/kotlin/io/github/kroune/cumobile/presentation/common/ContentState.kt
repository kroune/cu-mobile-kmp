package io.github.kroune.cumobile.presentation.common

/**
 * Sealed interface representing the loading state of a piece of content.
 *
 * Used across all screens to replace `isLoading` / `error` / plain data fields
 * with a single typed wrapper that supports progressive loading.
 */
sealed interface ContentState<out T> {
    data object Loading : ContentState<Nothing>

    data class Success<T>(val data: T) : ContentState<T>

    data class Error(val message: String) : ContentState<Nothing>
}

/** Returns the data if this is [ContentState.Success], or `null` otherwise. */
val <T> ContentState<T>.dataOrNull: T?
    get() = (this as? ContentState.Success)?.data

/** Returns `true` if this is [ContentState.Loading]. */
val ContentState<*>.isLoading: Boolean
    get() = this is ContentState.Loading

/** Returns `true` if this is [ContentState.Error]. */
val ContentState<*>.isError: Boolean
    get() = this is ContentState.Error

/** Returns `true` if this is [ContentState.Success]. */
val ContentState<*>.isSuccess: Boolean
    get() = this is ContentState.Success

/** Returns the error message if this is [ContentState.Error], or `null` otherwise. */
val ContentState<*>.errorOrNull: String?
    get() = (this as? ContentState.Error)?.message
