package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.network.ProfileApiService
import io.github.kroune.cumobile.domain.repository.AuthRepository
import io.github.kroune.cumobile.domain.repository.CookieValidationResult
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val logger = KotlinLogging.logger {}

private const val CookieLogPrefixLen = 20

/**
 * Implementation of [AuthRepository] that uses [AuthLocalDataSource] for cookie
 * persistence and [ProfileApiService] for cookie validation.
 */
internal class AuthRepositoryImpl(
    private val localDataSource: AuthLocalDataSource,
    private val profileApi: ProfileApiService,
) : AuthRepository {
    override val isAuthenticated: Flow<Boolean> =
        localDataSource.cookieFlow.map { it != null }

    override val cookieFlow: Flow<String?> =
        localDataSource.cookieFlow

    override suspend fun saveCookie(cookie: String) {
        localDataSource.saveCookie(cookie)
    }

    override suspend fun clearCookie() {
        localDataSource.clearCookie()
    }

    override suspend fun hasCookie(): Boolean =
        localDataSource.cookieFlow.first() != null

    override suspend fun validateCookie(): CookieValidationResult {
        val cookie = localDataSource.cookieFlow.first()
        if (cookie == null) {
            logger.warn { "validateCookie: no cookie stored" }
            return CookieValidationResult.Invalid
        }
        logger.info { "validateCookie: cookie length=${cookie.length}, prefix=${cookie.take(CookieLogPrefixLen)}..." }
        val authResult = profileApi.checkAuth(cookie)
        logger.info { "validateCookie: checkAuth returned $authResult" }
        return when (authResult) {
            true -> CookieValidationResult.Valid
            false -> {
                logger.warn { "validateCookie: server rejected cookie (401), clearing" }
                clearCookie()
                CookieValidationResult.Invalid
            }
            null -> {
                logger.warn { "validateCookie: network error, keeping cookie" }
                CookieValidationResult.NetworkError
            }
        }
    }
}
