package com.flit.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
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
class SettingsThemeInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun settings_theme_option_is_selectable() {
        composeRule.dismissOnboardingIfVisible()
        composeRule.waitForHomeReady()

        composeRule.onNodeWithContentDescription("설정").performClick()
        composeRule.waitUntil("", 10_000L) {
            composeRule.onAllNodesWithText("설정").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("다크 모드").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("다크 모드").assertIsDisplayed()
    }
}
