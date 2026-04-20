package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val logger = KotlinLogging.logger {}

/**
 * Base class for repositories that require an authenticated cookie.
 *
 * Encapsulates the common pattern of retrieving the cookie from
 * [AuthLocalDataSource] and short-circuiting when the user is not
 * authenticated.
 *
 * All work runs on [AppDispatchers.io] so callers can safely invoke
 * from `Dispatchers.Main.immediate` — network and DataStore reads won't
 * block the UI thread.
 *
 * Dependencies are held as [Lazy] so wiring a repository singleton in Koin
 * doesn't transitively instantiate [AuthLocalDataSource] / [AppDispatchers]
 * before the first actual API call.
 */
internal open class CookieAwareRepository(
    private val authLocal: Lazy<AuthLocalDataSource>,
    private val dispatchers: Lazy<AppDispatchers>,
) {
    /**
     * Executes [block] with a valid cookie on [AppDispatchers.io]. Returns
     * `null` when the user is not authenticated (cookie is absent).
     */
    protected suspend fun <T> withCookie(block: suspend (String) -> T?): T? =
        withContext(dispatchers().io) {
            val cookie = authLocal().cookieFlow.first()
            if (cookie == null) {
                logger.debug { "No auth cookie available, skipping API call" }
                return@withContext null
            }
            block(cookie)
        }

    /**
     * Executes [block] with a valid cookie on [AppDispatchers.io]. Returns
     * `false` when the user is not authenticated (cookie is absent).
     */
    protected suspend fun withCookieOrFalse(block: suspend (String) -> Boolean): Boolean =
        withContext(dispatchers().io) {
            val cookie = authLocal().cookieFlow.first()
            if (cookie == null) {
                logger.debug { "No auth cookie available, skipping API call" }
                return@withContext false
            }
            block(cookie)
        }
}
