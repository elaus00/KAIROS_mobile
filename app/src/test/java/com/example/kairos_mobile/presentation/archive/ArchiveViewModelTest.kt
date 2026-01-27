package com.example.kairos_mobile.presentation.archive

import app.cash.turbine.test
import com.example.kairos_mobile.domain.model.Insight
import com.example.kairos_mobile.domain.usecase.insight.GetAllInsightsUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
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
 * ArchiveViewModel 유닛 테스트
 *
 * 테스트 대상:
 * - 날짜별 그룹화된 캡처 로드
 * - 캡처 확장/축소 토글
 * - 새로고침
 * - 에러 처리
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getAllInsightsUseCase: GetAllInsightsUseCase
    private lateinit var viewModel: ArchiveViewModel

    @Before
    fun setup() {
        getAllInsightsUseCase = mockk()
    }

    private fun createViewModel(): ArchiveViewModel {
        return ArchiveViewModel(getAllInsightsUseCase)
    }

    // ==================== 초기 로딩 테스트 ====================

    @Test
    fun `초기화 시 캡처 로드 시작`() = runTest {
        // Given
        val groupedInsights = mapOf(
            "Today" to listOf(Insight(id = "1", content = "오늘 캡처")),
            "Yesterday" to listOf(Insight(id = "2", content = "어제 캡처"))
        )
        every { getAllInsightsUseCase.getInsightsGroupedByDate() } returns flowOf(groupedInsights)

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.groupedInsights.size)
        assertTrue(state.groupedInsights.containsKey("Today"))
        assertTrue(state.groupedInsights.containsKey("Yesterday"))
    }

    @Test
    fun `캡처 로드 성공 시 그룹화된 데이터 표시`() = runTest {
        // Given
        val captures = listOf(
            Insight(id = "1", content = "캡처 1"),
            Insight(id = "2", content = "캡처 2"),
            Insight(id = "3", content = "캡처 3")
        )
        val groupedInsights = mapOf("Today" to captures)
        every { getAllInsightsUseCase.getInsightsGroupedByDate() } returns flowOf(groupedInsights)

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val todayInsights = viewModel.uiState.value.groupedInsights["Today"]
        assertNotNull(todayInsights)
        assertEquals(3, todayInsights?.size)
    }

    @Test
    fun `빈 캡처 목록도 정상 처리`() = runTest {
        // Given
        every { getAllInsightsUseCase.getInsightsGroupedByDate() } returns flowOf(emptyMap())

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.groupedInsights.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ==================== 확장/축소 토글 테스트 ====================

    @Test
    fun `캡처 확장 토글 - 추가`() = runTest {
        // Given
        every { getAllInsightsUseCase.getInsightsGroupedByDate() } returns flowOf(emptyMap())
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onToggleExpand("capture-1")

        // Then
        assertTrue(viewModel.uiState.value.expandedInsightIds.contains("capture-1"))
    }

    @Test
    fun `캡처 확장 토글 - 제거`() = runTest {
        // Given
        every { getAllInsightsUseCase.getInsightsGroupedByDate() } returns flowOf(emptyMap())
        viewModel = createViewModel()
        advanceUntilIdle()

        // 먼저 확장
        viewModel.onToggleExpand("capture-1")
        assertTrue(viewModel.uiState.value.expandedInsightIds.contains("capture-1"))

        // When - 다시 토글하여 축소
        viewModel.onToggleExpand("capture-1")

        // Then
        assertFalse(viewModel.uiState.value.expandedInsightIds.contains("capture-1"))
    }

    @Test
    fun `여러 캡처 동시 확장 가능`() = runTest {
        // Given
        every { getAllInsightsUseCase.getInsightsGroupedByDate() } returns flowOf(emptyMap())
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onToggleExpand("capture-1")
        viewModel.onToggleExpand("capture-2")
        viewModel.onToggleExpand("capture-3")

        // Then
        val expandedIds = viewModel.uiState.value.expandedInsightIds
        assertEquals(3, expandedIds.size)
        assertTrue(expandedIds.containsAll(listOf("capture-1", "capture-2", "capture-3")))
    }

    // ==================== 새로고침 테스트 ====================

    @Test
    fun `새로고침 시 데이터 다시 로드`() = runTest {
        // Given
        val initialInsights = mapOf("Today" to listOf(Insight(id = "1", content = "초기")))
        every { getAllInsightsUseCase.getInsightsGroupedByDate() } returns flowOf(initialInsights)

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
        every { getAllInsightsUseCase.getInsightsGroupedByDate() } returns flowOf(emptyMap())
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onErrorDismissed()

        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }

    // ==================== 상태 Flow 테스트 ====================

    @Test
    fun `상태 Flow 정상 동작`() = runTest {
        // Given
        every { getAllInsightsUseCase.getInsightsGroupedByDate() } returns flowOf(emptyMap())

        viewModel = createViewModel()

        viewModel.uiState.test {
            // 초기 로딩 상태 또는 완료 상태
            val state = awaitItem()
            // 로딩이 완료되면 isLoading이 false여야 함

            cancelAndIgnoreRemainingEvents()
        }
    }
}
