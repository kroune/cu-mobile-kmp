package io.github.kroune.cumobile.presentation.common

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeTest {

    @Test
    fun testStripEmojiPrefix() {
        assertEquals("Математика", stripEmojiPrefix("📐 Математика"))
        assertEquals("Разработка", stripEmojiPrefix("💻 Разработка"))
        assertEquals("Physics", stripEmojiPrefix("Physics"))
        assertEquals("", stripEmojiPrefix(""))
    }
}
