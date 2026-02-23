package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.first

private val logger = KotlinLogging.logger {}

/**
 * Base class for repositories that require an authenticated cookie.
 *
 * Encapsulates the common pattern of retrieving the cookie from
 * [AuthLocalDataSource] and short-circuiting when the user is not
 * authenticated.
 */
internal open class CookieAwareRepository(
    private val authLocal: AuthLocalDataSource,
) {
    /**
     * Executes [block] with a valid cookie. Returns `null` when the user
     * is not authenticated (cookie is absent).
     */
    protected suspend fun <T> withCookie(block: suspend (String) -> T?): T? {
        val cookie = authLocal.cookieFlow.first()
        if (cookie == null) {
            logger.debug { "No auth cookie available, skipping API call" }
            return null
        }
        return block(cookie)
    }

    /**
     * Executes [block] with a valid cookie. Returns `false` when the user
     * is not authenticated (cookie is absent).
     */
    protected suspend fun withCookieOrFalse(block: suspend (String) -> Boolean): Boolean {
        val cookie = authLocal.cookieFlow.first()
        if (cookie == null) {
            logger.debug { "No auth cookie available, skipping API call" }
            return false
        }
        return block(cookie)
    }
}
