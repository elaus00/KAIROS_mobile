package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.presentation.trash.TrashContent
import com.flit.app.presentation.trash.TrashUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TrashScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun trash_default_state() {
        composeRule.setContent {
            FlitTheme {
                TrashContent(
                    uiState = TrashUiState(),
                    onNavigateBack = {},
                    onRestoreItem = {},
                    onDeleteItem = {},
                    onEmptyTrash = {},
                    onDismissError = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/trash_default.png")
    }
}
