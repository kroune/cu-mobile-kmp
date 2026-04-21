package io.github.kroune.cumobile.data.network

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.isSecure
import io.ktor.util.date.GMTDate

/**
 * A [CookiesStorage] that can be reset between auth sessions.
 * Allows reusing a single [io.ktor.client.HttpClient] across multiple login attempts
 * while isolating per-session Keycloak cookies.
 *
 * Thread-safety note: all access happens sequentially on a single auth flow
 * (reset → startAuth → submit → … → exchange), so no synchronization is needed.
 */
class ResettableCookieStorage : CookiesStorage {
    private val cookies = mutableListOf<Cookie>()

    override suspend fun get(requestUrl: Url): List<Cookie> {
        val now = GMTDate()
        return cookies.filter { matches(it, requestUrl, now) }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        val domain = cookie.domain?.lowercase() ?: requestUrl.host.lowercase()
        val path = cookie.path ?: "/"
        cookies.removeAll {
            it.name == cookie.name &&
                it.domain?.lowercase() == domain &&
                (it.path ?: "/") == path
        }
        val expires = cookie.expires
        if (expires == null || expires >= GMTDate()) {
            cookies.add(cookie.copy(domain = domain, path = path))
        }
    }

    override fun close(): Unit = Unit

    fun reset() = cookies.clear()

    private fun matches(cookie: Cookie, url: Url, now: GMTDate): Boolean {
        val expires = cookie.expires
        if (expires != null && expires < now) return false

        if (cookie.secure && !url.protocol.isSecure()) return false

        val domain = cookie.domain?.lowercase() ?: return false
        val host = url.host.lowercase()
        val domainMatchesHost = when {
            host == domain -> true
            host == domain.trimStart('.') -> true
            host.endsWith(".$domain") -> true
            host.endsWith(domain) -> true
            else -> false
        }
        if (!domainMatchesHost) return false

        val path = cookie.path ?: "/"
        return url.encodedPath.startsWith(path)
    }
}
