package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.presentation.calendar.CalendarContentBody
import com.flit.app.presentation.calendar.CalendarUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CalendarScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun calendar_default_state() {
        composeRule.setContent {
            FlitTheme {
                CalendarContentBody(
                    uiState = CalendarUiState()
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/calendar_default.png")
    }
}
