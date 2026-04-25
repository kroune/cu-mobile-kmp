package com.thirdparty.cumobile.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Always-on generator: cold start only, no user interaction. Warms the
 * classloader-heavy parts of the app: `AndroidApplication.onCreate`, Koin
 * initialization, the Decompose `RootComponent`, splash screen handling,
 * and the first Compose composition.
 *
 * Intentionally free of testTag lookups so it keeps working through any UI
 * refactor. If this one fails, the project's build is broken in a bigger way.
 */
@RunWith(AndroidJUnit4::class)
class StartupBaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(packageName = TARGET_PACKAGE) {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), WAIT_FOR_APP_MS)
            device.waitForIdle()
        }
    }
}
