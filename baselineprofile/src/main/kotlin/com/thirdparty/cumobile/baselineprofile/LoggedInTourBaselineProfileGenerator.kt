package com.thirdparty.cumobile.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import io.github.kroune.cumobile.baseline.BaselineTestTags
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Full happy-path tour through the logged-in surface: auth via `bff.cookie`,
 * all four tabs, one course detail.
 *
 * Gated on the `bffCookie` instrumentation argument. Without a cookie, the
 * test is skipped via `Assume.assumeTrue` so the aggregate generation still
 * succeeds. Pass the cookie via:
 *
 * ```
 * ./gradlew :androidApp:generateReleaseBaselineProfile \
 *   -Pandroid.testInstrumentationRunnerArguments.bffCookie=$BFF_COOKIE
 * ```
 */
@RunWith(AndroidJUnit4::class)
class LoggedInTourBaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        val cookie = InstrumentationRegistry.getArguments().getString("bffCookie")
        Assume.assumeTrue(
            "bffCookie argument not provided — skipping logged-in tour",
            !cookie.isNullOrBlank(),
        )

        rule.collect(packageName = TARGET_PACKAGE) {
            pressHome()
            startActivityAndWait()

            loginWithBffCookie(cookie!!)
            tourTabs()
            openFirstCourseDetail()
        }
    }
}

private fun MacrobenchmarkScope.loginWithBffCookie(cookie: String) {
    device.wait(
        Until.hasObject(By.res(BaselineTestTags.LOGIN_SWITCH_BFF)),
        WAIT_FOR_SCREEN_MS,
    )
    device.findObject(By.res(BaselineTestTags.LOGIN_SWITCH_BFF)).click()

    device.wait(
        Until.hasObject(By.res(BaselineTestTags.LOGIN_BFF_COOKIE_FIELD)),
        WAIT_FOR_SCREEN_MS,
    )
    device.findObject(By.res(BaselineTestTags.LOGIN_BFF_COOKIE_FIELD)).text = cookie
    device.findObject(By.res(BaselineTestTags.LOGIN_BFF_COOKIE_SUBMIT)).click()

    device.wait(
        Until.hasObject(By.res(BaselineTestTags.TAB_HOME)),
        WAIT_FOR_LOGIN_RESPONSE_MS,
    )
    device.waitForIdle()
}

private fun MacrobenchmarkScope.tourTabs() {
    val tabs = listOf(
        BaselineTestTags.TAB_TASKS,
        BaselineTestTags.TAB_COURSES,
        BaselineTestTags.TAB_FILES,
        BaselineTestTags.TAB_HOME,
    )
    tabs.forEach { tag ->
        device.findObject(By.res(tag)).click()
        device.waitForIdle()
        device.findObject(By.scrollable(true))?.fling(Direction.DOWN)
    }
}

private fun MacrobenchmarkScope.openFirstCourseDetail() {
    device.findObject(By.res(BaselineTestTags.TAB_COURSES)).click()
    val hasCard = device.wait(
        Until.hasObject(By.res(BaselineTestTags.FIRST_COURSE_CARD)),
        WAIT_FOR_SCREEN_MS,
    )
    if (!hasCard) return

    device.findObject(By.res(BaselineTestTags.FIRST_COURSE_CARD)).click()
    device.waitForIdle()
    device.pressBack()
    device.waitForIdle()
}
