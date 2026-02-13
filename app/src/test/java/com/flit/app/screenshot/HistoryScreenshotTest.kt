package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.presentation.history.HistoryContent
import com.flit.app.presentation.history.HistoryUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HistoryScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun history_default_state() {
        composeRule.setContent {
            FlitTheme {
                HistoryContent(
                    uiState = HistoryUiState(),
                    events = emptyFlow(),
                    onNavigateBack = {},
                    onCaptureClick = {},
                    onDeleteCapture = {},
                    onUndoDelete = {},
                    onDismissError = {},
                    onTypeFilterSelected = {},
                    onDateRangeSelected = { _, _ -> },
                    onChangeClassification = { _, _, _ -> },
                    onLoadMore = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/history_default.png")
    }
}
