package com.example.kairos_mobile.presentation.search

import app.cash.turbine.test
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.usecase.SearchCapturesUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * SearchViewModel 유닛 테스트
 *
 * 테스트 대상:
 * - 검색 텍스트 변경
 * - 타입/소스 필터 토글
 * - 검색 실행 및 결과 처리
 * - 페이징 (더 보기)
 * - 에러 처리
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var searchCapturesUseCase: SearchCapturesUseCase
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        searchCapturesUseCase = mockk()
        viewModel = SearchViewModel(searchCapturesUseCase)
    }

    // ==================== 초기 상태 테스트 ====================

    @Test
    fun `초기 상태는 빈 검색 텍스트와 필터`() {
        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals("", state.searchText)
        assertTrue(state.selectedTypes.isEmpty())
        assertTrue(state.selectedSources.isEmpty())
        assertTrue(state.searchResults.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.hasSearched)
    }

    // ==================== 검색 텍스트 변경 테스트 ====================

    @Test
    fun `검색 텍스트 변경 시 상태 업데이트`() = runTest {
        // When
        viewModel.onSearchTextChanged("테스트 검색어")

        // Then
        assertEquals("테스트 검색어", viewModel.uiState.value.searchText)
    }

    @Test
    fun `검색 텍스트 연속 변경`() = runTest {
        viewModel.uiState.test {
            // 초기 상태
            assertEquals("", awaitItem().searchText)

            // 변경
            viewModel.onSearchTextChanged("첫번째")
            assertEquals("첫번째", awaitItem().searchText)

            viewModel.onSearchTextChanged("두번째")
            assertEquals("두번째", awaitItem().searchText)

            viewModel.onSearchTextChanged("")
            assertEquals("", awaitItem().searchText)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== 타입 필터 테스트 ====================

    @Test
    fun `타입 필터 토글 - 추가`() = runTest {
        // When
        viewModel.onTypeFilterToggle(CaptureType.SCHEDULE)

        // Then
        assertTrue(viewModel.uiState.value.selectedTypes.contains(CaptureType.SCHEDULE))
    }

    @Test
    fun `타입 필터 토글 - 제거`() = runTest {
        // Given
        viewModel.onTypeFilterToggle(CaptureType.SCHEDULE)
        assertTrue(viewModel.uiState.value.selectedTypes.contains(CaptureType.SCHEDULE))

        // When
        viewModel.onTypeFilterToggle(CaptureType.SCHEDULE)

        // Then
        assertFalse(viewModel.uiState.value.selectedTypes.contains(CaptureType.SCHEDULE))
    }

    @Test
    fun `여러 타입 필터 선택 가능`() = runTest {
        // When
        viewModel.onTypeFilterToggle(CaptureType.SCHEDULE)
        viewModel.onTypeFilterToggle(CaptureType.TODO)
        viewModel.onTypeFilterToggle(CaptureType.IDEA)

        // Then
        val selectedTypes = viewModel.uiState.value.selectedTypes
        assertEquals(3, selectedTypes.size)
        assertTrue(selectedTypes.contains(CaptureType.SCHEDULE))
        assertTrue(selectedTypes.contains(CaptureType.TODO))
        assertTrue(selectedTypes.contains(CaptureType.IDEA))
    }

    // ==================== 소스 필터 테스트 ====================

    @Test
    fun `소스 필터 토글 - 추가`() = runTest {
        // When
        viewModel.onSourceFilterToggle(CaptureSource.IMAGE)

        // Then
        assertTrue(viewModel.uiState.value.selectedSources.contains(CaptureSource.IMAGE))
    }

    @Test
    fun `소스 필터 토글 - 제거`() = runTest {
        // Given
        viewModel.onSourceFilterToggle(CaptureSource.IMAGE)

        // When
        viewModel.onSourceFilterToggle(CaptureSource.IMAGE)

        // Then
        assertFalse(viewModel.uiState.value.selectedSources.contains(CaptureSource.IMAGE))
    }

    // ==================== 검색 실행 테스트 ====================

    @Test
    fun `검색 성공 시 결과 업데이트`() = runTest {
        // Given
        val captures = listOf(
            Capture(id = "1", content = "테스트 캡처 1"),
            Capture(id = "2", content = "테스트 캡처 2")
        )
        coEvery { searchCapturesUseCase(any(), eq(0), eq(20)) } returns Result.Success(captures)

        // When
        viewModel.onSearchTextChanged("테스트")
        viewModel.onSearch()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.hasSearched)
        assertFalse(state.isLoading)
        assertEquals(2, state.searchResults.size)
        assertNull(state.errorMessage)
    }

    @Test
    fun `검색 실패 시 에러 메시지 설정`() = runTest {
        // Given
        coEvery { searchCapturesUseCase(any(), any(), any()) } returns
            Result.Error(RuntimeException("검색 실패"))

        // When
        viewModel.onSearch()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("검색 실패", state.errorMessage)
    }

    @Test
    fun `검색 결과가 pageSize 미만이면 hasMore false`() = runTest {
        // Given - 20개 미만 결과
        val captures = listOf(Capture(id = "1", content = "캡처"))
        coEvery { searchCapturesUseCase(any(), eq(0), eq(20)) } returns Result.Success(captures)

        // When
        viewModel.onSearch()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.hasMore)
    }

    @Test
    fun `검색 결과가 pageSize 이상이면 hasMore true`() = runTest {
        // Given - 20개 결과
        val captures = (1..20).map { Capture(id = "$it", content = "캡처 $it") }
        coEvery { searchCapturesUseCase(any(), eq(0), eq(20)) } returns Result.Success(captures)

        // When
        viewModel.onSearch()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.hasMore)
    }

    // ==================== 페이징 테스트 ====================

    @Test
    fun `더 보기 시 기존 결과에 추가`() = runTest {
        // Given - 첫 검색
        val firstPageCaptures = (1..20).map { Capture(id = "$it", content = "캡처 $it") }
        coEvery { searchCapturesUseCase(any(), eq(0), eq(20)) } returns Result.Success(firstPageCaptures)

        viewModel.onSearch()
        advanceUntilIdle()
        assertEquals(20, viewModel.uiState.value.searchResults.size)

        // Given - 두 번째 페이지
        val secondPageCaptures = (21..30).map { Capture(id = "$it", content = "캡처 $it") }
        coEvery { searchCapturesUseCase(any(), eq(20), eq(20)) } returns Result.Success(secondPageCaptures)

        // When
        viewModel.onLoadMore()
        advanceUntilIdle()

        // Then
        assertEquals(30, viewModel.uiState.value.searchResults.size)
    }

    @Test
    fun `hasMore가 false면 더 보기 무시`() = runTest {
        // Given - 결과가 적어서 hasMore = false
        val captures = listOf(Capture(id = "1", content = "캡처"))
        coEvery { searchCapturesUseCase(any(), any(), any()) } returns Result.Success(captures)

        viewModel.onSearch()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.hasMore)

        val resultCount = viewModel.uiState.value.searchResults.size

        // When
        viewModel.onLoadMore()
        advanceUntilIdle()

        // Then - 결과 변화 없음
        assertEquals(resultCount, viewModel.uiState.value.searchResults.size)
    }

    // ==================== 필터 초기화 테스트 ====================

    @Test
    fun `필터 초기화 시 모든 필터 제거`() = runTest {
        // Given
        viewModel.onSearchTextChanged("테스트")
        viewModel.onTypeFilterToggle(CaptureType.SCHEDULE)
        viewModel.onSourceFilterToggle(CaptureSource.IMAGE)

        // When
        viewModel.onClearFilters()

        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.searchText)
        assertTrue(state.selectedTypes.isEmpty())
        assertTrue(state.selectedSources.isEmpty())
        assertNull(state.dateRange)
    }

    // ==================== 에러 처리 테스트 ====================

    @Test
    fun `에러 메시지 닫기`() = runTest {
        // Given
        coEvery { searchCapturesUseCase(any(), any(), any()) } returns
            Result.Error(RuntimeException("에러"))
        viewModel.onSearch()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)

        // When
        viewModel.onErrorDismissed()

        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
