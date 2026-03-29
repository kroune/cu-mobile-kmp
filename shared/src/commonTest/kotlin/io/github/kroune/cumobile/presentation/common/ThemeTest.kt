package io.github.kroune.cumobile.presentation.common

import io.github.kroune.cumobile.presentation.common.ui.stripEmojiPrefix
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
