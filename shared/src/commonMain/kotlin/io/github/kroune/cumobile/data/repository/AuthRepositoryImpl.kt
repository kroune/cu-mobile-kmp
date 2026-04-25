package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.network.ProfileApiService
import io.github.kroune.cumobile.domain.repository.AuthRepository
import io.github.kroune.cumobile.domain.repository.CookieValidationResult
import io.github.kroune.cumobile.presentation.common.invoke
import io.github.kroune.cumobile.util.AppDispatchers
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.getValue

private val logger = KotlinLogging.logger {}

private const val CookieLogPrefixLen = 20

/**
 * Implementation of [AuthRepository] that uses [AuthLocalDataSource] for cookie
 * persistence and [ProfileApiService] for cookie validation.
 */
internal class AuthRepositoryImpl(
    private val localDataSourceLazy: Lazy<AuthLocalDataSource>,
    private val profileApiLazy: Lazy<ProfileApiService>,
    private val dispatchersLazy: Lazy<AppDispatchers>,
) : AuthRepository {
    override val isAuthenticated: Flow<Boolean> by lazy {
        localDataSourceLazy().cookieFlow.map { it != null }
    }

    override val cookieFlow: Flow<String?> by lazy { localDataSourceLazy().cookieFlow }

    override suspend fun saveCookie(cookie: String) =
        withContext(dispatchersLazy().io) {
            localDataSourceLazy().saveCookie(cookie)
        }

    override suspend fun clearCookie() =
        withContext(dispatchersLazy().io) {
            localDataSourceLazy().clearCookie()
        }

    override suspend fun hasCookie(): Boolean =
        withContext(dispatchersLazy().io) {
            localDataSourceLazy().cookieFlow.first() != null
        }

    override suspend fun validateCookie(): CookieValidationResult =
        withContext(dispatchersLazy().io) {
            val cookie = localDataSourceLazy().cookieFlow.first()
            if (cookie == null) {
                logger.warn { "validateCookie: no cookie stored" }
                return@withContext CookieValidationResult.Invalid
            }
            logger.info {
                "validateCookie: cookie length=${cookie.length}, prefix=${cookie.take(CookieLogPrefixLen)}..."
            }
            val authResult = profileApiLazy().checkAuth(cookie)
            logger.info { "validateCookie: checkAuth returned $authResult" }
            when (authResult) {
                true -> CookieValidationResult.Valid
                false -> {
                    logger.warn { "validateCookie: server rejected cookie (401), clearing" }
                    localDataSourceLazy().clearCookie()
                    CookieValidationResult.Invalid
                }
                null -> {
                    logger.warn { "validateCookie: network error, keeping cookie" }
                    CookieValidationResult.NetworkError
                }
            }
        }
}
