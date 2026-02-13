package com.flit.app

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

internal fun AndroidComposeTestRule<*, MainActivity>.dismissOnboardingIfVisible() {
    waitForIdle()
    val hasSkipButton = onAllNodesWithText("건너뛰기", useUnmergedTree = true)
        .fetchSemanticsNodes()
        .isNotEmpty()
    if (hasSkipButton) {
        onNodeWithText("건너뛰기", useUnmergedTree = true).performClick()
        waitForIdle()
    }
}

internal fun AndroidComposeTestRule<*, MainActivity>.waitForHomeReady(timeoutMillis: Long = 10_000L) {
    waitUntil("", timeoutMillis) {
        onAllNodesWithText("Flit.").fetchSemanticsNodes().isNotEmpty() ||
            onAllNodesWithText("홈").fetchSemanticsNodes().isNotEmpty()
    }
    waitUntil("", timeoutMillis) {
        onAllNodesWithTag("capture_input").fetchSemanticsNodes().isNotEmpty()
    }
    onNodeWithTag("capture_input")
}
