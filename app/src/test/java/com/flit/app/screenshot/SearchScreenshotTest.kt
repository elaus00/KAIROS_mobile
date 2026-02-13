package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.presentation.search.SearchContent
import com.flit.app.presentation.search.SearchUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SearchScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun search_default_state() {
        composeRule.setContent {
            FlitTheme {
                SearchContent(
                    uiState = SearchUiState(),
                    onBackClick = {},
                    onCaptureClick = {},
                    onSearchTextChanged = {},
                    onTypeFilterSelected = {},
                    onToggleSemanticMode = {},
                    onErrorDismissed = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/search_default.png")
    }
}
