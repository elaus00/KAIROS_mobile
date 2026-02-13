package com.flit.app.screenshot

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.presentation.capture.CaptureContent
import com.flit.app.presentation.capture.CaptureUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * CaptureContent 스크린샷 테스트
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CaptureScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun capture_default_state() {
        composeRule.setContent {
            FlitTheme {
                CaptureContent(
                    uiState = CaptureUiState(),
                    onNavigateToSettings = {},
                    onNavigateToHistory = {},
                    autoFocusCapture = false,
                    onUpdateInput = {},
                    onSubmit = {},
                    onImageSelected = {},
                    onRemoveImage = {},
                    onToggleStatusSheet = {},
                    onDismissStatusSheet = {},
                    onSaveDraft = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/capture_default.png")
    }

    @Test
    fun capture_with_text() {
        composeRule.setContent {
            FlitTheme {
                CaptureContent(
                    uiState = CaptureUiState(
                        inputText = "떠오르는 생각을 기록합니다.",
                        unconfirmedCount = 5
                    ),
                    onNavigateToSettings = {},
                    onNavigateToHistory = {},
                    autoFocusCapture = false,
                    onUpdateInput = {},
                    onSubmit = {},
                    onImageSelected = {},
                    onRemoveImage = {},
                    onToggleStatusSheet = {},
                    onDismissStatusSheet = {},
                    onSaveDraft = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/capture_with_text.png")
    }

    @Test
    fun capture_submitting() {
        composeRule.setContent {
            FlitTheme {
                CaptureContent(
                    uiState = CaptureUiState(
                        inputText = "제출 중인 캡처",
                        isSubmitting = true
                    ),
                    onNavigateToSettings = {},
                    onNavigateToHistory = {},
                    autoFocusCapture = false,
                    onUpdateInput = {},
                    onSubmit = {},
                    onImageSelected = {},
                    onRemoveImage = {},
                    onToggleStatusSheet = {},
                    onDismissStatusSheet = {},
                    onSaveDraft = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/capture_submitting.png")
    }
}
