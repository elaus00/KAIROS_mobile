package com.flit.app.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.flit.app.ui.theme.FlitTheme

/**
 * 스크린샷 테스트 공통 유틸리티
 */

/** 공통 Roborazzi 옵션 */
val DefaultRoborazziOptions = RoborazziOptions()

/** 스크린샷 디렉토리 */
private const val SCREENSHOT_DIR = "src/test/screenshots"

/**
 * FlitTheme으로 래핑하여 스크린샷 캡처
 * @param name 스크린샷 파일명 (예: "capture_empty.png")
 * @param content 캡처할 Composable
 */
fun ComposeContentTestRule.captureScreenshot(
    name: String,
    content: @Composable () -> Unit
) {
    setContent {
        FlitTheme {
            content()
        }
    }
    onRoot().captureRoboImage(
        filePath = "$SCREENSHOT_DIR/$name",
        roborazziOptions = DefaultRoborazziOptions
    )
}
