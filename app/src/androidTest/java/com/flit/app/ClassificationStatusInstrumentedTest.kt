package com.flit.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
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
class ClassificationStatusInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun status_sheet_opens_from_bell_action() {
        composeRule.dismissOnboardingIfVisible()
        composeRule.waitForHomeReady()

        composeRule.onNodeWithContentDescription("AI 분류 현황").performClick()
        composeRule.waitUntil("", 10_000L) {
            composeRule.onAllNodesWithText("AI 분류 현황").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("AI 분류 현황")
    }
}
