package com.example.kairos_mobile.presentation.search

import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import com.example.kairos_mobile.domain.usecase.search.SearchCapturesUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
import com.example.kairos_mobile.util.TestFixtures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * SearchViewModel 단위 테스트
 * - 초기 상태 확인
 * - 디바운싱 검색 (300ms)
 * - 빈 입력 시 결과 초기화
 * - 빠른 연속 입력 시 마지막만 검색
 * - 검색 에러 처리
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var searchCapturesUseCase: SearchCapturesUseCase
    private lateinit var trackEventUseCase: TrackEventUseCase
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        searchCapturesUseCase = mockk()
        trackEventUseCase = mockk(relaxed = true)
        viewModel = SearchViewModel(searchCapturesUseCase, trackEventUseCase)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initial_state_is_empty`() = runTest {
        // then: 초기 상태 확인
        val state = viewModel.uiState.value
        assertEquals("", state.searchText)
        assertTrue(state.searchResults.isEmpty())
        assertFalse(state.hasSearched)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `search_returns_results_after_debounce`() = runTest {
        // given: 검색 결과 mock
        val results = listOf(
            TestFixtures.capture(id = "c1", originalText = "검색어 포함 텍스트"),
            TestFixtures.capture(id = "c2", originalText = "다른 검색어 텍스트")
        )
        every { searchCapturesUseCase("검색어") } returns flowOf(results)

        // when: 검색어 입력 + 디바운스 대기
        viewModel.onSearchTextChanged("검색어")
        advanceTimeBy(350)
        advanceUntilIdle()

        // then: 결과 반영
        val state = viewModel.uiState.value
        assertEquals(2, state.searchResults.size)
        assertTrue(state.hasSearched)
        assertEquals("검색어", state.searchText)
    }

    @Test
    fun `blank_clears_results`() = runTest {
        // given: 기존 검색 결과가 있는 상태 구성
        val results = listOf(TestFixtures.capture(id = "c1"))
        every { searchCapturesUseCase("테스트") } returns flowOf(results)

        viewModel.onSearchTextChanged("테스트")
        advanceTimeBy(350)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasSearched)

        // when: 빈 텍스트 입력
        viewModel.onSearchTextChanged("")
        advanceUntilIdle()

        // then: 결과 초기화
        val state = viewModel.uiState.value
        assertTrue(state.searchResults.isEmpty())
        assertFalse(state.hasSearched)
    }

    @Test
    fun `rapid_typing_only_searches_last`() = runTest {
        // given: 마지막 검색어에 대한 mock만 설정
        val results = listOf(TestFixtures.capture(id = "c1", originalText = "abc"))
        every { searchCapturesUseCase("abc") } returns flowOf(results)

        // when: 빠른 연속 입력 (디바운스 300ms 내에 모두 발생)
        viewModel.onSearchTextChanged("a")
        advanceTimeBy(100)
        viewModel.onSearchTextChanged("ab")
        advanceTimeBy(100)
        viewModel.onSearchTextChanged("abc")
        advanceTimeBy(350)
        advanceUntilIdle()

        // then: 마지막 검색어 "abc"만 실제 검색됨
        verify(exactly = 1) { searchCapturesUseCase("abc") }
        assertEquals(1, viewModel.uiState.value.searchResults.size)
    }

    @Test
    fun `search_error_sets_message`() = runTest {
        // given: 검색 중 예외 발생
        every { searchCapturesUseCase("에러") } returns flow {
            throw RuntimeException("검색 실패")
        }

        // when: 검색 실행
        viewModel.onSearchTextChanged("에러")
        advanceTimeBy(350)
        advanceUntilIdle()

        // then: 에러 메시지 설정
        assertEquals("검색 실패", viewModel.uiState.value.errorMessage)
    }

    // ── 필터 기능 테스트 ──

    @Test
    fun `setTypeFilter_triggers_filtered_search`() = runTest {
        // given: 기본 검색 결과 + 필터 검색 결과 mock
        val results = listOf(TestFixtures.capture(id = "c1"))
        every { searchCapturesUseCase("검색어") } returns flowOf(results)

        val filteredResults = listOf(
            TestFixtures.capture(id = "c2", classifiedType = ClassifiedType.TODO)
        )
        coEvery {
            searchCapturesUseCase.searchFiltered("검색어", ClassifiedType.TODO, null, null)
        } returns filteredResults

        // when: 검색어 입력 → 디바운스 → 필터 적용
        viewModel.onSearchTextChanged("검색어")
        advanceTimeBy(350)
        advanceUntilIdle()

        viewModel.setTypeFilter(ClassifiedType.TODO)
        advanceUntilIdle()

        // then
        assertEquals(ClassifiedType.TODO, viewModel.uiState.value.selectedType)
        assertEquals(1, viewModel.uiState.value.searchResults.size)
        assertEquals("c2", viewModel.uiState.value.searchResults[0].id)
    }

    @Test
    fun `setDateRange_triggers_filtered_search`() = runTest {
        // given
        val results = listOf(TestFixtures.capture(id = "c1"))
        every { searchCapturesUseCase("테스트") } returns flowOf(results)

        val filteredResults = listOf(
            TestFixtures.capture(id = "c3", createdAt = 5000L)
        )
        coEvery {
            searchCapturesUseCase.searchFiltered("테스트", null, 1000L, 9000L)
        } returns filteredResults

        // when
        viewModel.onSearchTextChanged("테스트")
        advanceTimeBy(350)
        advanceUntilIdle()

        viewModel.setDateRange(1000L, 9000L)
        advanceUntilIdle()

        // then
        assertEquals(1000L, viewModel.uiState.value.startDate)
        assertEquals(9000L, viewModel.uiState.value.endDate)
        assertEquals(1, viewModel.uiState.value.searchResults.size)
    }

    @Test
    fun `clearFilters_returns_to_flow_search`() = runTest {
        // given
        val results = listOf(TestFixtures.capture(id = "c1"))
        every { searchCapturesUseCase("검색") } returns flowOf(results)

        val filteredResults = listOf(
            TestFixtures.capture(id = "c2", classifiedType = ClassifiedType.TODO)
        )
        coEvery {
            searchCapturesUseCase.searchFiltered("검색", ClassifiedType.TODO, null, null)
        } returns filteredResults

        // 검색 + 필터 적용
        viewModel.onSearchTextChanged("검색")
        advanceTimeBy(350)
        advanceUntilIdle()

        viewModel.setTypeFilter(ClassifiedType.TODO)
        advanceUntilIdle()
        assertEquals(ClassifiedType.TODO, viewModel.uiState.value.selectedType)

        // when: 필터 초기화
        viewModel.clearFilters()
        advanceUntilIdle()

        // then: 필터 해제
        assertNull(viewModel.uiState.value.selectedType)
        assertNull(viewModel.uiState.value.startDate)
        assertNull(viewModel.uiState.value.endDate)
    }
}
