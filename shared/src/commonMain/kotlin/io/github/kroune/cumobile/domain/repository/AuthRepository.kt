package io.github.kroune.cumobile.domain.repository

import kotlinx.coroutines.flow.Flow

/** Result of server-side cookie validation. */
enum class CookieValidationResult {
    /** Server accepted the cookie (HTTP 200). */
    Valid,

    /** Server rejected the cookie (HTTP 401) — cookie should be cleared. */
    Invalid,

    /** Could not reach the server (network error, timeout, etc.) — keep the cookie. */
    NetworkError,
}

/**
 * Repository interface for authentication operations.
 * Implementations handle cookie storage, validation, and auth state management.
 */
interface AuthRepository {
    /** Flow indicating whether the user is currently authenticated. */
    val isAuthenticated: Flow<Boolean>

    /** Flow emitting the current BFF cookie value, or null. */
    val cookieFlow: Flow<String?>

    /** Saves the authentication cookie and marks the user as authenticated. */
    suspend fun saveCookie(cookie: String)

    /** Clears the stored cookie and marks the user as unauthenticated. */
    suspend fun clearCookie()

    /** Checks whether a cookie is stored locally (no network request). */
    suspend fun hasCookie(): Boolean

    /**
     * Validates the current cookie against the API.
     *
     * Returns [CookieValidationResult.Valid] on success, [CookieValidationResult.Invalid]
     * when the server rejects the cookie (401), or [CookieValidationResult.NetworkError]
     * when the server is unreachable. Clears the cookie automatically on [CookieValidationResult.Invalid].
     */
    suspend fun validateCookie(): CookieValidationResult
}
