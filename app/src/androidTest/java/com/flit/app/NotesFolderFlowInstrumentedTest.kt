package com.flit.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class NotesFolderFlowInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun notes_folder_filter_chip_is_interactive() {
        composeRule.dismissOnboardingIfVisible()
        composeRule.waitForHomeReady()

        composeRule.onNodeWithTag("tab_notes").performClick()
        composeRule.waitUntil("", 10_000L) {
            composeRule.onAllNodesWithText("전체").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("전체").performClick()
    }
}
