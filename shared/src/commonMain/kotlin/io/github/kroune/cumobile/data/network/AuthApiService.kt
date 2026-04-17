package io.github.kroune.cumobile.data.network

import io.github.kroune.cumobile.util.runCatchingCancellable
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.encodeURLParameter

private val logger = KotlinLogging.logger {}

private const val AuthTimeoutMs = 30_000L

/**
 * Result of a Keycloak auth step.
 *
 * - [NextStep] — another form page (email, password, or OTP).
 * - [Redirect] — final redirect to the callback URL with the auth code.
 * - [Error] — auth failed with an error message.
 */
sealed interface AuthStepResult {
    data class NextStep(
        val activePage: String,
        val loginAction: String,
        val phoneNumber: String? = null,
        val errorMessage: String? = null,
    ) : AuthStepResult

    data class Redirect(
        val callbackUrl: String,
    ) : AuthStepResult

    data class Error(
        val message: String,
    ) : AuthStepResult
}

/**
 * Handles Keycloak OIDC authentication flow via direct HTTP calls
 * instead of a WebView. Each instance manages a single auth session
 * with its own cookie jar.
 *
 * The flow is:
 * 1. [startAuth] — GET OIDC auth URL → parse loginAction from HTML
 * 2. [submitUsername] — POST username → password page
 * 3. [submitPassword] — POST password → OTP page
 * 4. [submitOtp] — POST OTP code → 302 redirect to callback
 * 5. [exchangeCallback] — GET callback URL → capture bff.cookie
 */
class AuthApiService {
    private val cookieStorage = AcceptAllCookiesStorage()

    private val client = HttpClient {
        followRedirects = false
        install(HttpCookies) {
            storage = cookieStorage
        }
        install(HttpTimeout) {
            requestTimeoutMillis = AuthTimeoutMs
            connectTimeoutMillis = AuthTimeoutMs
            socketTimeoutMillis = AuthTimeoutMs
        }
    }

    /**
     * Initiates the auth flow by loading the Keycloak login page.
     */
    suspend fun startAuth(): AuthStepResult =
        runCatchingCancellable {
            val response = client.get(AuthOidcUrl)
            handleAuthResponse(response)
        }.getOrElse { e ->
            logger.error(e) { "Failed to start auth" }
            AuthStepResult.Error("Ошибка подключения: ${e.message}")
        }

    /**
     * Submits the email/login to Keycloak.
     */
    suspend fun submitUsername(
        loginAction: String,
        username: String,
    ): AuthStepResult =
        postForm(
            loginAction,
            "username=${username.encodeURLParameter()}",
        )

    /**
     * Submits the password to Keycloak.
     */
    suspend fun submitPassword(
        loginAction: String,
        password: String,
    ): AuthStepResult =
        postForm(
            loginAction,
            "password=${password.encodeURLParameter()}",
        )

    /**
     * Submits the SMS OTP code to Keycloak.
     */
    suspend fun submitOtp(
        loginAction: String,
        code: String,
        phoneNumber: String,
    ): AuthStepResult =
        postForm(
            loginAction,
            buildString {
                append("phoneNumber=${phoneNumber.encodeURLParameter()}")
                append("&code=${code.encodeURLParameter()}")
                append("&action=verify")
                append("&credentialId=")
            },
        )

    /**
     * Follows the callback URL to exchange the auth code for a bff.cookie.
     * Returns the cookie value or null on failure.
     */
    suspend fun exchangeCallback(callbackUrl: String): String? =
        runCatchingCancellable {
            val response = client.get(callbackUrl)
            val setCookies = response.headers.getAll(HttpHeaders.SetCookie).orEmpty()

            val bffCookie = setCookies
                .firstOrNull { it.startsWith("$TargetCookieName=") }
                ?.substringAfter("$TargetCookieName=")
                ?.substringBefore(";")

            if (bffCookie != null) {
                logger.info { "Captured bff.cookie, length=${bffCookie.length}" }
            } else {
                logger.warn { "Callback missing bff.cookie. Status=${response.status}, headers=$setCookies" }
            }
            bffCookie
        }.getOrElse { e ->
            logger.error(e) { "Failed to exchange callback" }
            null
        }

    /**
     * Closes the HTTP client. Call after auth flow completes.
     */
    fun close() {
        client.close()
    }

    private suspend fun postForm(
        url: String,
        formBody: String,
    ): AuthStepResult =
        runCatchingCancellable {
            val response = client.post(url) {
                setBody(TextContent(formBody, ContentType.Application.FormUrlEncoded))
            }
            handleAuthResponse(response)
        }.getOrElse { e ->
            logger.error(e) { "Failed to submit auth form" }
            AuthStepResult.Error("Ошибка подключения: ${e.message}")
        }

    private suspend fun handleAuthResponse(response: HttpResponse): AuthStepResult =
        when (response.status) {
            HttpStatusCode.Found, HttpStatusCode.TemporaryRedirect -> {
                val location = response.headers[HttpHeaders.Location]
                when {
                    location == null -> AuthStepResult.Error("Пустой редирект")
                    location.contains("callback") ->
                        AuthStepResult.Redirect(location)

                    else -> {
                        // Follow internal Keycloak redirects (e.g., locale changes)
                        runCatchingCancellable {
                            val next = client.get(location)
                            handleAuthResponse(next)
                        }.getOrElse { e ->
                            logger.error(e) { "Failed to follow redirect" }
                            AuthStepResult.Error("Ошибка при переходе")
                        }
                    }
                }
            }

            HttpStatusCode.OK -> {
                val html = response.bodyAsText()
                parseAuthPage(html)
            }

            else -> {
                logger.warn { "Unexpected auth status: ${response.status}" }
                AuthStepResult.Error("Ошибка сервера: ${response.status}")
            }
        }
}

/** OIDC authorization URL that starts the Keycloak login flow. */
private const val AuthOidcUrl =
    "https://id.centraluniversity.ru/realms/central-university" +
        "/protocol/openid-connect/auth" +
        "?response_type=code&client_id=api-gateway" +
        "&scope=openid+email+offline_access" +
        "&redirect_uri=https://my.centraluniversity.ru/api/account/signin/callback"

private const val LoginActionKey = "urls.loginAction"
private const val ActivePageKey = "activePage"
private const val PhoneNumberKey = "currentState.phoneNumber"
private const val ErrorMessageKey = "systemMessage.content"
private const val ErrorLevelKey = "systemMessage.level"

/**
 * Parses a Keycloak HTML page to extract the auth state.
 * The page embeds config as JS assignments in `window.authConfiguration`.
 */
internal fun parseAuthPage(html: String): AuthStepResult {
    val loginAction = extractJsStringValue(html, LoginActionKey)
        ?.replace("&amp;", "&")
    val activePage = extractJsStringValue(html, ActivePageKey)
    val phoneNumber = extractJsStringValue(html, PhoneNumberKey)
    val errorMessage = extractJsStringValue(html, ErrorMessageKey)
    val errorLevel = extractJsStringValue(html, ErrorLevelKey)

    logger.info {
        "parseAuthPage: page=$activePage, action=${loginAction?.take(LogPrefixLen)}, " +
            "phone=$phoneNumber, error=$errorMessage, level=$errorLevel"
    }

    if (loginAction == null || activePage == null) {
        val htmlSnippet = html.take(HtmlSnippetLen)
        logger.warn { "Failed to parse auth page. HTML start: $htmlSnippet" }
        return AuthStepResult.Error("Не удалось разобрать страницу авторизации")
    }

    val translatedError = errorMessage?.let { translateKeycloakMessage(it, errorLevel) }

    return AuthStepResult.NextStep(
        activePage = activePage,
        loginAction = loginAction,
        phoneNumber = phoneNumber,
        errorMessage = translatedError,
    )
}

private const val LogPrefixLen = 50
private const val HtmlSnippetLen = 500

/**
 * Extracts a JavaScript string value from HTML like:
 * `window.authConfiguration.urls.loginAction = "https://...";`
 * Returns the last occurrence (later script overrides take precedence).
 */
internal fun extractJsStringValue(
    html: String,
    key: String,
): String? {
    val result = """(.*)?"""
    val someSpaces = """\s*"""
    val pattern = Regex("""${Regex.escape(key)}$someSpaces=$someSpaces(['"])$result\1""")
    return pattern
        .findAll(html)
        .lastOrNull()
        ?.groupValues
        ?.get(2)
}

private val keycloakErrorTranslations: Map<String, String> = mapOf(
    "phoneTokenCodeDoesNotMatch" to "Неверный код",
    "phoneTokenCodeExpired" to "Код истёк, запросите новый",
    "invalidUserMessage" to "Неверный логин или пароль",
    "invalidPasswordMessage" to "Неверный пароль",
    "accountTemporarilyDisabledMessage" to "Аккаунт временно заблокирован",
)

/** Keycloak `systemMessage.level` values for messages the user shouldn't see as errors. */
private val nonErrorLevels = setOf("success", "info")

/**
 * Maps Keycloak's `systemMessage` payload to a user-facing error, or `null` if the
 * payload is informational (e.g. `codeSent` with level `success`) and should not
 * be surfaced as an error at all.
 */
private fun translateKeycloakMessage(
    errorCode: String,
    errorLevel: String?,
): String? {
    if (errorLevel in nonErrorLevels) return null
    return keycloakErrorTranslations[errorCode] ?: errorCode
}
