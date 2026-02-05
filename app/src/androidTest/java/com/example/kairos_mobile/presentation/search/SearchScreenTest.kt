package com.example.kairos_mobile.presentation.search

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kairos_mobile.presentation.components.search.SearchBar
import com.example.kairos_mobile.ui.theme.KAIROS_mobileTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * SearchScreen 관련 UI 테스트
 *
 * 테스트 대상:
 * - 검색 바 상호작용
 * - 검색 실행
 * - 필터 칩
 */
@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ==================== SearchBar 테스트 ====================

    @Test
    fun 검색바_플레이스홀더_표시() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                SearchBar(
                    text = "",
                    onTextChange = {},
                    onSearch = {},
                    onClear = {}
                )
            }
        }

        // Then - 기본 placeholder는 "캡처 검색…"
        composeTestRule.onNodeWithText("캡처 검색…")
            .assertIsDisplayed()
    }

    @Test
    fun 검색바_텍스트_입력() {
        // Given
        var searchText = ""
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                SearchBar(
                    text = searchText,
                    onTextChange = { searchText = it },
                    onSearch = {},
                    onClear = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("캡처 검색…")
            .performTextInput("테스트")

        // Then - onTextChange 호출 확인
        // Note: 실제 테스트에서는 상태 관리가 필요함
    }

    @Test
    fun 검색바_검색_버튼_클릭() {
        // Given
        var searchCalled = false
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                SearchBar(
                    text = "테스트",
                    onTextChange = {},
                    onSearch = { searchCalled = true },
                    onClear = {}
                )
            }
        }

        // When - 검색 버튼 텍스트로 클릭
        composeTestRule.onNodeWithText("검색")
            .performClick()

        // Then
        assert(searchCalled)
    }

    @Test
    fun 텍스트_있을_때_클리어_버튼_표시() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                SearchBar(
                    text = "테스트",
                    onTextChange = {},
                    onSearch = {},
                    onClear = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("지우기")
            .assertIsDisplayed()
    }

    @Test
    fun 빈_텍스트일_때_클리어_버튼_숨김() {
        // Given
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                SearchBar(
                    text = "",
                    onTextChange = {},
                    onSearch = {},
                    onClear = {}
                )
            }
        }

        // Then - 클리어 버튼이 없어야 함
        val clearNodes = composeTestRule.onAllNodesWithContentDescription("지우기")
            .fetchSemanticsNodes()
        assert(clearNodes.isEmpty()) { "Clear button should not be visible when text is empty" }
    }

    @Test
    fun 클리어_버튼_클릭_시_onClear_호출() {
        // Given
        var clearCalled = false
        composeTestRule.setContent {
            KAIROS_mobileTheme {
                SearchBar(
                    text = "테스트",
                    onTextChange = {},
                    onSearch = {},
                    onClear = { clearCalled = true }
                )
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("지우기")
            .performClick()

        // Then
        assert(clearCalled)
    }
}
