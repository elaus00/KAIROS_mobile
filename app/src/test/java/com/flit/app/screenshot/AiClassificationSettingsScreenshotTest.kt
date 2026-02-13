package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.domain.model.ClassificationPreset
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.presentation.settings.ai.AiClassificationSettingsContent
import com.flit.app.presentation.settings.ai.AiClassificationSettingsUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * AiClassificationSettingsContent 스크린샷 테스트
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AiClassificationSettingsScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ai_settings_free_tier() {
        composeRule.setContent {
            FlitTheme {
                AiClassificationSettingsContent(
                    uiState = AiClassificationSettingsUiState(
                        presets = listOf(
                            ClassificationPreset(
                                id = "default",
                                name = "기본",
                                description = "일반적인 분류 규칙"
                            )
                        ),
                        selectedPresetId = "default",
                        customInstruction = "",
                        subscriptionTier = SubscriptionTier.FREE
                    ),
                    onNavigateBack = {},
                    onNavigateToSubscription = {},
                    onSetPreset = {},
                    onSetCustomInstruction = {},
                    onSaveCustomInstruction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/ai_settings_free_tier.png")
    }

    @Test
    fun ai_settings_premium_tier() {
        composeRule.setContent {
            FlitTheme {
                AiClassificationSettingsContent(
                    uiState = AiClassificationSettingsUiState(
                        presets = listOf(
                            ClassificationPreset(
                                id = "default",
                                name = "기본",
                                description = "일반적인 분류 규칙"
                            ),
                            ClassificationPreset(
                                id = "work",
                                name = "업무",
                                description = "업무 중심 분류"
                            )
                        ),
                        selectedPresetId = "work",
                        customInstruction = "업무 관련 내용은 일정으로 분류",
                        subscriptionTier = SubscriptionTier.PREMIUM
                    ),
                    onNavigateBack = {},
                    onNavigateToSubscription = {},
                    onSetPreset = {},
                    onSetCustomInstruction = {},
                    onSaveCustomInstruction = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/ai_settings_premium_tier.png")
    }
}
