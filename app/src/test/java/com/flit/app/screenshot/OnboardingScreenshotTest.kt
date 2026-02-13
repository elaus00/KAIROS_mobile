package com.flit.app.screenshot

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.presentation.onboarding.OnboardingContent
import com.flit.app.presentation.onboarding.OnboardingUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun onboarding_default_state() {
        composeRule.setContent {
            FlitTheme {
                OnboardingContent(
                    uiState = OnboardingUiState(),
                    onSkip = {},
                    onUpdateInput = {},
                    onCompleteOnboarding = {},
                    onCalendarConnect = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/onboarding_default.png")
    }
}
