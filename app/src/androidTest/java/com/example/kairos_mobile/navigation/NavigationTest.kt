package com.example.kairos_mobile.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.kairos_mobile.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Navigation Instrumented 테스트
 *
 * 테스트 대상:
 * - 탭 간 네비게이션
 * - 백스택 관리
 * - 화면 전환
 */
@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun 초기_화면은_Capture() {
        // Then - Capture 화면의 플레이스홀더 텍스트 확인
        composeTestRule.onNodeWithText("무엇이든 캡처하세요…")
            .assertIsDisplayed()
    }

    @Test
    fun Search_탭_클릭_시_Search_화면_표시() {
        // When
        composeTestRule.onNodeWithContentDescription("Search")
            .performClick()

        // Then
        composeTestRule.onNodeWithText("Search")
            .assertIsDisplayed()
    }

    @Test
    fun Archive_탭_클릭_시_Archive_화면_표시() {
        // When
        composeTestRule.onNodeWithContentDescription("Archive")
            .performClick()

        // Then - Archive 화면 확인 (뒤로가기 버튼 존재)
        composeTestRule.onNodeWithContentDescription("뒤로가기")
            .assertIsDisplayed()
    }

    @Test
    fun Settings_탭_클릭_시_Settings_화면_표시() {
        // When
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Then - Settings 화면에서 "설정" 텍스트 확인
        composeTestRule.onNodeWithText("설정")
            .assertIsDisplayed()
    }

    @Test
    fun Search_화면에서_Capture_탭으로_이동() {
        // When - Search로 이동
        composeTestRule.onNodeWithContentDescription("Search")
            .performClick()

        // Search 화면 확인
        composeTestRule.onNodeWithText("Search")
            .assertIsDisplayed()

        // When - Capture로 이동
        composeTestRule.onNodeWithContentDescription("Capture")
            .performClick()

        // Then
        composeTestRule.onNodeWithText("무엇이든 캡처하세요…")
            .assertIsDisplayed()
    }

    @Test
    fun 모든_탭_순회_테스트() {
        // Capture -> Search
        composeTestRule.onNodeWithContentDescription("Search")
            .performClick()
        composeTestRule.waitForIdle()

        // Search -> Archive
        composeTestRule.onNodeWithContentDescription("Archive")
            .performClick()
        composeTestRule.waitForIdle()

        // Archive -> Settings
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()
        composeTestRule.waitForIdle()

        // Settings -> Capture
        composeTestRule.onNodeWithContentDescription("Capture")
            .performClick()
        composeTestRule.waitForIdle()

        // Then - 다시 Capture 화면
        composeTestRule.onNodeWithText("무엇이든 캡처하세요…")
            .assertIsDisplayed()
    }

    @Test
    fun Search_화면_뒤로가기_버튼_동작() {
        // When - Search로 이동
        composeTestRule.onNodeWithContentDescription("Search")
            .performClick()

        // 뒤로가기 클릭
        composeTestRule.onNodeWithContentDescription("뒤로가기")
            .performClick()

        // Then - Capture 화면으로 돌아옴
        composeTestRule.onNodeWithText("무엇이든 캡처하세요…")
            .assertIsDisplayed()
    }

    @Test
    fun 여러_탭_이동_후_Capture로_직접_이동() {
        // 여러 화면 이동
        composeTestRule.onNodeWithContentDescription("Search")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Archive")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()
        composeTestRule.waitForIdle()

        // When - Capture 탭 직접 클릭
        composeTestRule.onNodeWithContentDescription("Capture")
            .performClick()
        composeTestRule.waitForIdle()

        // Then - Capture 화면 정상 표시
        composeTestRule.onNodeWithText("무엇이든 캡처하세요…")
            .assertIsDisplayed()
    }
}
