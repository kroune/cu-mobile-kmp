package io.github.kroune.cumobile.data.repository

import io.github.kroune.cumobile.data.local.AuthLocalDataSource
import io.github.kroune.cumobile.data.network.ApiService
import io.github.kroune.cumobile.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implementation of [AuthRepository] that uses [AuthLocalDataSource] for cookie
 * persistence and [ApiService] for cookie validation.
 */
class AuthRepositoryImpl(
    private val localDataSource: AuthLocalDataSource,
    private val apiService: ApiService,
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

    override suspend fun validateCookie(): Boolean {
        val cookie = localDataSource.cookieFlow.first() ?: return false
        val isValid = apiService.validateAuth(cookie)
        if (!isValid) {
            clearCookie()
        }
        return isValid
    }
}
