package com.flit.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SearchFlowInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun notes_search_opens_and_accepts_query() {
        composeRule.dismissOnboardingIfVisible()
        composeRule.waitForHomeReady()

        composeRule.onNodeWithTag("tab_notes").performClick()
        composeRule.waitUntil("", 10_000L) {
            composeRule.onAllNodesWithContentDescription("검색").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithContentDescription("검색").performClick()
        composeRule.waitUntil("", 10_000L) {
            composeRule.onAllNodesWithTag("search_input").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("search_input").performTextInput("아이디어")
    }
}
