package io.github.kroune.cumobile.data.network

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpdateCheckerTest {
    @Test
    fun newerMajorVersion() {
        assertTrue(UpdateChecker.isNewerVersion("2.0.0", "1.0.0"))
    }

    @Test
    fun newerMinorVersion() {
        assertTrue(UpdateChecker.isNewerVersion("1.1.0", "1.0.0"))
    }

    @Test
    fun newerPatchVersion() {
        assertTrue(UpdateChecker.isNewerVersion("1.0.2", "1.0.1"))
    }

    @Test
    fun sameVersion() {
        assertFalse(UpdateChecker.isNewerVersion("1.0.1", "1.0.1"))
    }

    @Test
    fun olderVersion() {
        assertFalse(UpdateChecker.isNewerVersion("1.0.0", "1.0.1"))
    }

    @Test
    fun differentLengthVersions() {
        assertTrue(UpdateChecker.isNewerVersion("1.0.1.1", "1.0.1"))
        assertFalse(UpdateChecker.isNewerVersion("1.0", "1.0.1"))
    }
}
