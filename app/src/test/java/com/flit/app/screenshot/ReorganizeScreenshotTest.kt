package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.presentation.notes.reorganize.ProposedItem
import com.flit.app.presentation.notes.reorganize.ReorganizeContent
import com.flit.app.presentation.notes.reorganize.ReorganizeUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * ReorganizeContent 스크린샷 테스트
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReorganizeScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reorganize_loading() {
        composeRule.setContent {
            FlitTheme {
                ReorganizeContent(
                    uiState = ReorganizeUiState(
                        isLoading = true,
                        isApplying = false,
                        proposals = emptyList(),
                        error = null
                    ),
                    onNavigateBack = {},
                    onApply = {},
                    onRetry = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/reorganize_loading.png")
    }

    @Test
    fun reorganize_with_proposals() {
        composeRule.setContent {
            FlitTheme {
                ReorganizeContent(
                    uiState = ReorganizeUiState(
                        isLoading = false,
                        isApplying = false,
                        proposals = listOf(
                            ProposedItem(
                                folderName = "업무",
                                folderType = "CUSTOM",
                                action = "CREATE",
                                captureIds = listOf("1", "2", "3")
                            ),
                            ProposedItem(
                                folderName = "개인",
                                folderType = "CUSTOM",
                                action = "MOVE",
                                captureIds = listOf("4", "5")
                            )
                        ),
                        error = null
                    ),
                    onNavigateBack = {},
                    onApply = {},
                    onRetry = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/reorganize_with_proposals.png")
    }

    @Test
    fun reorganize_error() {
        composeRule.setContent {
            FlitTheme {
                ReorganizeContent(
                    uiState = ReorganizeUiState(
                        isLoading = false,
                        isApplying = false,
                        proposals = emptyList(),
                        error = "재구성 제안을 가져오는 중 오류가 발생했습니다"
                    ),
                    onNavigateBack = {},
                    onApply = {},
                    onRetry = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/reorganize_error.png")
    }
}
