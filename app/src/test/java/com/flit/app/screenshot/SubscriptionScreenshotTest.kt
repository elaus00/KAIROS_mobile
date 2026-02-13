package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.domain.model.SubscriptionFeatures
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.presentation.subscription.SubscriptionContent
import com.flit.app.presentation.subscription.SubscriptionUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * SubscriptionContent 스크린샷 테스트
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SubscriptionScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun subscription_free_tier() {
        composeRule.setContent {
            FlitTheme {
                SubscriptionContent(
                    uiState = SubscriptionUiState(
                        tier = SubscriptionTier.FREE,
                        features = SubscriptionFeatures(),
                        isLoading = false,
                        error = null
                    ),
                    onNavigateBack = {},
                    onUpgrade = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/subscription_free_tier.png")
    }

    @Test
    fun subscription_premium_tier() {
        composeRule.setContent {
            FlitTheme {
                SubscriptionContent(
                    uiState = SubscriptionUiState(
                        tier = SubscriptionTier.PREMIUM,
                        features = SubscriptionFeatures(
                            aiGrouping = true,
                            inboxClassify = true,
                            semanticSearch = true,
                            noteReorganize = true,
                            analyticsDashboard = true,
                            ocr = true,
                            classificationPreset = true,
                            customInstruction = true
                        ),
                        isLoading = false,
                        error = null
                    ),
                    onNavigateBack = {},
                    onUpgrade = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/subscription_premium_tier.png")
    }

    @Test
    fun subscription_loading() {
        composeRule.setContent {
            FlitTheme {
                SubscriptionContent(
                    uiState = SubscriptionUiState(
                        tier = SubscriptionTier.FREE,
                        features = SubscriptionFeatures(),
                        isLoading = true,
                        error = null
                    ),
                    onNavigateBack = {},
                    onUpgrade = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/subscription_loading.png")
    }
}
