package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.presentation.notes.NotesContentInternal
import com.flit.app.presentation.notes.NotesUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NotesScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun notes_default_state() {
        composeRule.setContent {
            FlitTheme {
                NotesContentInternal(
                    uiState = NotesUiState(),
                    onEvent = {},
                    onSearchClick = {},
                    onNoteClick = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/notes_default.png")
    }
}
