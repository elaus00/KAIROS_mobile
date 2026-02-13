package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.domain.model.CalendarSyncStatus
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.NoteSubType
import com.flit.app.presentation.detail.CaptureDetailContent
import com.flit.app.presentation.detail.CaptureDetailUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * CaptureDetailScreen 스크린샷 테스트
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CaptureDetailScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun captureDetailContent_todo_default() {
        composeRule.setContent {
            FlitTheme {
                CaptureDetailContent(
                    uiState = CaptureDetailUiState(
                        captureId = "capture1",
                        aiTitle = "할 일 예시",
                        originalText = "이것은 할 일 원문입니다.",
                        classifiedType = ClassifiedType.TODO,
                        tags = listOf("태그1", "태그2"),
                        createdAt = System.currentTimeMillis()
                    ),
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    onShare = {},
                    onRetry = {},
                    onChangeClassification = { _, _ -> },
                    onApproveCalendarSync = {},
                    onRejectCalendarSync = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/capture_detail_todo.png")
    }

    @Test
    fun captureDetailContent_schedule_withCalendarSync() {
        composeRule.setContent {
            FlitTheme {
                CaptureDetailContent(
                    uiState = CaptureDetailUiState(
                        captureId = "capture2",
                        aiTitle = "회의 일정",
                        originalText = "내일 오후 3시 팀 미팅",
                        classifiedType = ClassifiedType.SCHEDULE,
                        tags = listOf("회의"),
                        scheduleId = "schedule1",
                        scheduleStartTime = System.currentTimeMillis() + 86400000,
                        calendarSyncStatus = CalendarSyncStatus.SUGGESTION_PENDING,
                        createdAt = System.currentTimeMillis()
                    ),
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    onShare = {},
                    onRetry = {},
                    onChangeClassification = { _, _ -> },
                    onApproveCalendarSync = {},
                    onRejectCalendarSync = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/capture_detail_schedule_pending.png")
    }

    @Test
    fun captureDetailContent_notes_withImage() {
        composeRule.setContent {
            FlitTheme {
                CaptureDetailContent(
                    uiState = CaptureDetailUiState(
                        captureId = "capture3",
                        aiTitle = "노트 예시",
                        originalText = "이것은 이미지가 첨부된 노트입니다.",
                        classifiedType = ClassifiedType.NOTES,
                        noteSubType = NoteSubType.INBOX,
                        tags = listOf("노트", "이미지"),
                        imageUri = "content://media/sample.jpg",
                        createdAt = System.currentTimeMillis()
                    ),
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    onShare = {},
                    onRetry = {},
                    onChangeClassification = { _, _ -> },
                    onApproveCalendarSync = {},
                    onRejectCalendarSync = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/capture_detail_notes_with_image.png")
    }

    @Test
    fun captureDetailContent_loading() {
        composeRule.setContent {
            FlitTheme {
                CaptureDetailContent(
                    uiState = CaptureDetailUiState(isLoading = true),
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    onShare = {},
                    onRetry = {},
                    onChangeClassification = { _, _ -> },
                    onApproveCalendarSync = {},
                    onRejectCalendarSync = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/capture_detail_loading.png")
    }
}
