package io.github.kroune.cumobile

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = TestApplication::class)
class PreviewScreenshotTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {
    companion object {
        private const val SCREENSHOT_DIR = "build/outputs/roborazzi"

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun previews(): List<ComposablePreview<AndroidPreviewInfo>> {
            return AndroidComposablePreviewScanner()
                .scanPackageTrees("io.github.kroune.cumobile")
                .includePrivatePreviews()
                .getPreviews()
        }
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun capturePreview() {
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview).build()

        composeTestRule.apply {
            mainClock.autoAdvance = false
            setContent { preview() }
            mainClock.advanceTimeBy(5_000)
            onRoot().captureRoboImage(
                filePath = "$SCREENSHOT_DIR/$screenshotId.png",
            )
            mainClock.autoAdvance = true
        }
    }
}
