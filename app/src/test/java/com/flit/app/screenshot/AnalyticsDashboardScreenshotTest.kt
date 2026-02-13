package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.presentation.settings.analytics.AnalyticsDashboardContent
import com.flit.app.presentation.settings.analytics.AnalyticsDashboardUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AnalyticsDashboardScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun analytics_dashboard_default_state() {
        composeRule.setContent {
            FlitTheme {
                AnalyticsDashboardContent(
                    uiState = AnalyticsDashboardUiState(),
                    onNavigateBack = {},
                    onRetry = {},
                    onErrorDismissed = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/analytics_dashboard_default.png")
    }
}
