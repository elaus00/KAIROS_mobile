package com.example.kairos_mobile.presentation.notifications

import app.cash.turbine.test
import com.example.kairos_mobile.domain.model.Notification
import com.example.kairos_mobile.domain.model.NotificationType
import com.example.kairos_mobile.domain.usecase.GetNotificationsUseCase
import com.example.kairos_mobile.domain.usecase.MarkNotificationAsReadUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * NotificationsViewModel 유닛 테스트
 *
 * 테스트 대상:
 * - 알림 로드
 * - 알림 클릭 (읽음 처리)
 * - 필터 변경
 * - 에러 처리
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getNotificationsUseCase: GetNotificationsUseCase
    private lateinit var markNotificationAsReadUseCase: MarkNotificationAsReadUseCase
    private lateinit var viewModel: NotificationsViewModel

    private val sampleNotifications = listOf(
        Notification(
            id = "1",
            title = "캡처 완료",
            message = "새 캡처가 저장되었습니다",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            type = NotificationType.CAPTURE_COMPLETE
        ),
        Notification(
            id = "2",
            title = "동기화 완료",
            message = "5개 항목이 동기화되었습니다",
            timestamp = System.currentTimeMillis() - 3600000,
            isRead = true,
            type = NotificationType.SYNC_COMPLETE
        ),
        Notification(
            id = "3",
            title = "리마인더",
            message = "미팅 알림",
            timestamp = System.currentTimeMillis() - 7200000,
            isRead = false,
            type = NotificationType.REMINDER
        )
    )

    @Before
    fun setup() {
        getNotificationsUseCase = mockk()
        markNotificationAsReadUseCase = mockk(relaxed = true)
    }

    private fun createViewModel(): NotificationsViewModel {
        return NotificationsViewModel(getNotificationsUseCase, markNotificationAsReadUseCase)
    }

    // ==================== 초기 로딩 테스트 ====================

    @Test
    fun `초기화 시 알림 로드`() = runTest {
        // Given
        every { getNotificationsUseCase() } returns flowOf(sampleNotifications)

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.notifications.size)
        assertEquals(2, state.unreadCount)  // 읽지 않은 알림 2개
    }

    @Test
    fun `빈 알림 목록도 정상 처리`() = runTest {
        // Given
        every { getNotificationsUseCase() } returns flowOf(emptyList())

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.notifications.isEmpty())
        assertEquals(0, viewModel.uiState.value.unreadCount)
    }

    // ==================== 알림 클릭 테스트 ====================

    @Test
    fun `알림 클릭 시 읽음 처리 호출`() = runTest {
        // Given
        every { getNotificationsUseCase() } returns flowOf(sampleNotifications)
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onNotificationClick("1")
        advanceUntilIdle()

        // Then
        coVerify { markNotificationAsReadUseCase("1") }
    }

    // ==================== 필터 테스트 ====================

    @Test
    fun `초기 필터는 ALL`() = runTest {
        // Given
        every { getNotificationsUseCase() } returns flowOf(sampleNotifications)

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(NotificationFilter.ALL, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `UNREAD 필터 적용 시 필터 상태 변경`() = runTest {
        // Given
        every { getNotificationsUseCase() } returns flowOf(sampleNotifications)
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onFilterChanged(NotificationFilter.UNREAD)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(NotificationFilter.UNREAD, state.selectedFilter)
    }

    @Test
    fun `READ 필터 적용 시 필터 상태 변경`() = runTest {
        // Given
        every { getNotificationsUseCase() } returns flowOf(sampleNotifications)
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onFilterChanged(NotificationFilter.READ)
        advanceUntilIdle()

        // Then
        assertEquals(NotificationFilter.READ, viewModel.uiState.value.selectedFilter)
    }

    // ==================== 새로고침 테스트 ====================

    @Test
    fun `새로고침 시 알림 다시 로드`() = runTest {
        // Given
        every { getNotificationsUseCase() } returns flowOf(sampleNotifications)
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onRefresh()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ==================== 에러 처리 테스트 ====================

    @Test
    fun `에러 메시지 닫기`() = runTest {
        // Given
        every { getNotificationsUseCase() } returns flowOf(emptyList())
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onErrorDismissed()

        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }

    // ==================== 읽지 않은 알림 개수 테스트 ====================

    @Test
    fun `읽지 않은 알림 개수 정확히 계산`() = runTest {
        // Given
        val notifications = listOf(
            Notification(id = "1", title = "1", message = "1", timestamp = 1L, isRead = false),
            Notification(id = "2", title = "2", message = "2", timestamp = 2L, isRead = false),
            Notification(id = "3", title = "3", message = "3", timestamp = 3L, isRead = false),
            Notification(id = "4", title = "4", message = "4", timestamp = 4L, isRead = true),
            Notification(id = "5", title = "5", message = "5", timestamp = 5L, isRead = true)
        )
        every { getNotificationsUseCase() } returns flowOf(notifications)

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(3, viewModel.uiState.value.unreadCount)
    }

    @Test
    fun `모두 읽은 경우 unreadCount 0`() = runTest {
        // Given
        val notifications = listOf(
            Notification(id = "1", title = "1", message = "1", timestamp = 1L, isRead = true),
            Notification(id = "2", title = "2", message = "2", timestamp = 2L, isRead = true)
        )
        every { getNotificationsUseCase() } returns flowOf(notifications)

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(0, viewModel.uiState.value.unreadCount)
    }

    // ==================== 상태 Flow 테스트 ====================

    @Test
    fun `상태 Flow 정상 동작`() = runTest {
        // Given
        every { getNotificationsUseCase() } returns flowOf(sampleNotifications)
        viewModel = createViewModel()

        viewModel.uiState.test {
            // 초기 또는 로딩 완료 상태
            val state = awaitItem()
            assertFalse(state.isLoading) // 로딩 완료 후 상태

            cancelAndIgnoreRemainingEvents()
        }
    }
}
