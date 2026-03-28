package io.github.kroune.cumobile.data.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExtractJsStringValueTest {

    @Test
    fun doubleQuotedValue() {
        val html = """window.authConfiguration.urls.loginAction = "https://example.com/login";"""
        assertEquals("https://example.com/login", extractJsStringValue(html, "urls.loginAction"))
    }

    @Test
    fun singleQuotedValue() {
        val html = """window.authConfiguration.activePage = 'login-page';"""
        assertEquals("login-page", extractJsStringValue(html, "activePage"))
    }

    @Test
    fun keyNotPresent() {
        val html = """window.authConfiguration.activePage = "login";"""
        assertNull(extractJsStringValue(html, "missingKey"))
    }

    @Test
    fun returnsLastOccurrenceWhenMultiple() {
        val html = """
            window.authConfiguration.activePage = "first";
            window.authConfiguration.activePage = "second";
        """.trimIndent()
        assertEquals("second", extractJsStringValue(html, "activePage"))
    }

    @Test
    fun valueWithAmpersands() {
        val html = """window.authConfiguration.urls.loginAction = "https://id.example.ru/auth?foo=1&bar=2";"""
        assertEquals("https://id.example.ru/auth?foo=1&bar=2", extractJsStringValue(html, "urls.loginAction"))
    }

    @Test
    fun whitespaceAroundEquals() {
        val html = """window.authConfiguration.activePage  =   "otp-page";"""
        assertEquals("otp-page", extractJsStringValue(html, "activePage"))
    }

    @Test
    fun emptyStringValue() {
        val html = """window.authConfiguration.systemMessage.content = "";"""
        assertEquals("", extractJsStringValue(html, "systemMessage.content"))
    }

    @Test
    fun noQuoteAfterEquals() {
        val html = """window.authConfiguration.activePage = 42;"""
        assertNull(extractJsStringValue(html, "activePage"))
    }
}
