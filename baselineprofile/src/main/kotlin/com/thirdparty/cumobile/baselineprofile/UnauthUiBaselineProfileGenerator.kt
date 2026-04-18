package com.thirdparty.cumobile.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import io.github.kroune.cumobile.baseline.BaselineTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Warms the unauthenticated surface: login screen layout, `AnimatedContent`
 * between auth steps, autofill-tagged text fields, SMS observer registration.
 *
 * Assumes the app starts on the login screen (no saved cookie). When a cookie
 * is already present — e.g. a dev running this on their personal emulator —
 * the test will time out on the anchor lookup and skip; that's fine, it just
 * means this device isn't a clean baseline source.
 */
@RunWith(AndroidJUnit4::class)
class UnauthUiBaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(packageName = TARGET_PACKAGE) {
            pressHome()
            startActivityAndWait()

            val switchToBff = device.wait(
                Until.hasObject(By.res(BaselineTestTags.LOGIN_SWITCH_BFF)),
                WAIT_FOR_SCREEN_MS,
            )
            if (!switchToBff) return@collect

            device.findObject(By.res(BaselineTestTags.LOGIN_SWITCH_BFF)).click()
            device.wait(
                Until.hasObject(By.res(BaselineTestTags.LOGIN_BFF_COOKIE_FIELD)),
                WAIT_FOR_SCREEN_MS,
            )
            device.waitForIdle()
        }
    }
}
