package com.flit.app

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
class CaptureFlowInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun capture_submit_resets_input_state() {
        composeRule.dismissOnboardingIfVisible()
        composeRule.waitForHomeReady()

        composeRule.onNodeWithTag("capture_input").performTextInput("instrumentation capture test")
        composeRule.onNodeWithTag("capture_submit").performClick()

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("capture_submit").assertIsNotEnabled()
    }
}
