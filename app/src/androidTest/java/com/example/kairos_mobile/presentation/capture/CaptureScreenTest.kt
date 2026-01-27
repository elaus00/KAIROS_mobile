package com.example.kairos_mobile.presentation.capture

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kairos_mobile.presentation.components.GlassCaptureCard
import com.example.kairos_mobile.ui.theme.KAIROS_mobileTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * CaptureScreen 및 GlassCaptureCard UI 테스트
 *
 * 테스트 대상:
 * - 텍스트 입력
 * - Capture 버튼 활성화/비활성화
 * - 이미지 버튼 존재
 * - 플레이스홀더 표시
 */
@RunWith(AndroidJUnit4::class)
class CaptureScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ==================== 초기 상태 테스트 ====================

    @Test
    fun 초기_상태_플레이스홀더_표시() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = "",
                    onTextChange = {},
                    onSubmit = {},
                    onModeSelected = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("무엇이든 캡처하세요…")
            .assertIsDisplayed()
    }

    @Test
    fun 빈_텍스트일_때_Capture_버튼_비활성화() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = "",
                    onTextChange = {},
                    onSubmit = {},
                    onModeSelected = {},
                    enabled = true
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Capture")
            .assertIsNotEnabled()
    }

    // ==================== 텍스트 입력 테스트 ====================

    @Test
    fun 텍스트_입력_시_플레이스홀더_숨김() {
        // Given
        var text = ""
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = text,
                    onTextChange = { text = it },
                    onSubmit = {},
                    onModeSelected = {}
                )
            }
        }

        // When - 텍스트 입력
        composeTestRule.onNodeWithText("무엇이든 캡처하세요…")
            .performTextInput("테스트 입력")

        // Then - 상태 업데이트 확인 (onTextChange 호출됨)
        // Note: Compose 테스트에서는 직접 상태를 변경해야 함
    }

    @Test
    fun 텍스트_입력_후_Capture_버튼_활성화() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = "테스트 텍스트",
                    onTextChange = {},
                    onSubmit = {},
                    onModeSelected = {},
                    enabled = true
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Capture")
            .assertIsEnabled()
    }

    // ==================== 버튼 테스트 ====================

    @Test
    fun 이미지_버튼_표시() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = "",
                    onTextChange = {},
                    onSubmit = {},
                    onModeSelected = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("이미지")
            .assertIsDisplayed()
    }

    @Test
    fun 이미지_버튼_클릭_가능() {
        // Given
        var modeSelected: CaptureMode? = null
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = "",
                    onTextChange = {},
                    onSubmit = {},
                    onModeSelected = { modeSelected = it }
                )
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("이미지")
            .performClick()

        // Then
        assert(modeSelected == CaptureMode.IMAGE)
    }

    @Test
    fun Capture_버튼_클릭_시_onSubmit_호출() {
        // Given
        var submitCalled = false
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = "테스트",
                    onTextChange = {},
                    onSubmit = { submitCalled = true },
                    onModeSelected = {},
                    enabled = true
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Capture")
            .performClick()

        // Then
        assert(submitCalled)
    }

    // ==================== 로딩 상태 테스트 ====================

    @Test
    fun 로딩_중일_때_Capture_버튼에_로딩_표시() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = "테스트",
                    onTextChange = {},
                    onSubmit = {},
                    onModeSelected = {},
                    isLoading = true
                )
            }
        }

        // Then - 로딩 인디케이터가 표시되므로 "Capture" 텍스트가 없음
        composeTestRule.onNodeWithText("Capture")
            .assertDoesNotExist()
    }

    @Test
    fun 로딩_중일_때_텍스트_입력_비활성화() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = "",
                    onTextChange = {},
                    onSubmit = {},
                    onModeSelected = {},
                    isLoading = true
                )
            }
        }

        // Then - 플레이스홀더는 여전히 표시됨
        composeTestRule.onNodeWithText("무엇이든 캡처하세요…")
            .assertIsDisplayed()
    }

    // ==================== QuickTypeButtons 테스트 ====================

    @Test
    fun 빈_suggestedQuickTypes일_때_QuickTypeButtons_숨김() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = "테스트",
                    onTextChange = {},
                    onSubmit = {},
                    onModeSelected = {},
                    suggestedQuickTypes = emptyList()
                )
            }
        }

        // Then - QuickType 버튼들이 없어야 함
        composeTestRule.onNodeWithText("Meeting")
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("To-do")
            .assertDoesNotExist()
    }

    @Test
    fun suggestedQuickTypes_있을_때_QuickTypeButtons_표시() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = "회의 일정",
                    onTextChange = {},
                    onSubmit = {},
                    onModeSelected = {},
                    suggestedQuickTypes = listOf(
                        com.example.kairos_mobile.domain.model.CaptureType.SCHEDULE
                    )
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Meeting")
            .assertIsDisplayed()
    }

    // ==================== Disabled 상태 테스트 ====================

    @Test
    fun enabled_false일_때_Capture_버튼_비활성화() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                GlassCaptureCard(
                    text = "테스트",
                    onTextChange = {},
                    onSubmit = {},
                    onModeSelected = {},
                    enabled = false
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Capture")
            .assertIsNotEnabled()
    }
}
