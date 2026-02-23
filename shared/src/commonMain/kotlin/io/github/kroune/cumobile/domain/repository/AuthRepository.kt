package io.github.kroune.cumobile.domain.repository

import kotlinx.coroutines.flow.Flow

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

    /** Validates the current cookie against the API. */
    suspend fun validateCookie(): Boolean
}
