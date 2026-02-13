package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.presentation.auth.LoginContent
import com.flit.app.presentation.auth.LoginUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LoginScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun login_default_state() {
        composeRule.setContent {
            FlitTheme {
                LoginContent(
                    uiState = LoginUiState(),
                    onNavigateBack = {},
                    onStartGoogleLogin = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/login_default.png")
    }
}
