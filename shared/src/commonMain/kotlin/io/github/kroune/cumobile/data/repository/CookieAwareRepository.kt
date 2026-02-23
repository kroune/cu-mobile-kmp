package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.network.ApiService
import kotlinx.coroutines.flow.first

/**
 * Base class for repositories that require an authenticated cookie.
 *
 * Encapsulates the common pattern of retrieving the cookie from
 * [AuthLocalDataSource] and short-circuiting when the user is not
 * authenticated.
 */
open class CookieAwareRepository(
    private val authLocal: AuthLocalDataSource,
    protected val apiService: ApiService,
) {
    /**
     * Executes [block] with a valid cookie. Returns `null` when the user
     * is not authenticated (cookie is absent).
     */
    protected suspend fun <T> withCookie(block: suspend (String) -> T?): T? {
        val cookie = authLocal.cookieFlow.first() ?: return null
        return block(cookie)
    }

    /**
     * Executes [block] with a valid cookie. Returns `false` when the user
     * is not authenticated (cookie is absent).
     */
    protected suspend fun withCookieOrFalse(block: suspend (String) -> Boolean): Boolean {
        val cookie = authLocal.cookieFlow.first() ?: return false
        return block(cookie)
    }
}
