package com.example.kairos_mobile.presentation.search

import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import com.example.kairos_mobile.domain.usecase.search.SearchCapturesUseCase
import com.example.kairos_mobile.domain.usecase.search.SemanticSearchUseCase
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
    private lateinit var semanticSearchUseCase: SemanticSearchUseCase
    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        searchCapturesUseCase = mockk()
        trackEventUseCase = mockk(relaxed = true)
        semanticSearchUseCase = mockk(relaxed = true)
        subscriptionRepository = mockk()
        every { subscriptionRepository.getCachedTier() } returns SubscriptionTier.FREE
        viewModel = SearchViewModel(searchCapturesUseCase, trackEventUseCase, semanticSearchUseCase, subscriptionRepository)
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

    // ── 시맨틱 검색 테스트 ──

    @Test
    fun `toggleSemanticMode_activates_semantic_search`() = runTest {
        // given: 시맨틱 검색 결과 mock
        val semanticResults = listOf(
            com.example.kairos_mobile.domain.model.SemanticSearchResult(
                captureId = "c1",
                score = 0.95,
                snippet = "test snippet"
            )
        )
        coEvery { semanticSearchUseCase("query") } returns semanticResults

        // when: 검색어 입력 후 시맨틱 모드 활성화
        viewModel.onSearchTextChanged("query")
        advanceTimeBy(350)
        advanceUntilIdle()

        viewModel.toggleSemanticMode(true)
        advanceUntilIdle()

        // then: 시맨틱 모드 활성화 및 자동으로 시맨틱 검색 실행됨
        assertTrue(viewModel.uiState.value.isSemanticMode)
        assertEquals(1, viewModel.uiState.value.semanticResults.size)
        assertEquals("c1", viewModel.uiState.value.semanticResults[0].captureId)
    }

    @Test
    fun `semantic_search_returns_results`() = runTest {
        // given: 시맨틱 검색 결과 mock
        val semanticResults = listOf(
            com.example.kairos_mobile.domain.model.SemanticSearchResult(
                captureId = "c1",
                score = 0.95,
                snippet = "test snippet"
            )
        )
        coEvery { semanticSearchUseCase("test query") } returns semanticResults

        // when: 시맨틱 모드 활성화 후 검색
        viewModel.toggleSemanticMode(true)
        viewModel.onSearchTextChanged("test query")
        advanceTimeBy(350)
        advanceUntilIdle()

        // then: 시맨틱 검색 결과 반영
        assertEquals(1, viewModel.uiState.value.semanticResults.size)
        assertEquals("c1", viewModel.uiState.value.semanticResults[0].captureId)
        assertEquals(0.95, viewModel.uiState.value.semanticResults[0].score, 0.01)
    }

    @Test
    fun `semantic_search_handles_subscription_error`() = runTest {
        // given: 구독 필요 예외 발생
        coEvery { semanticSearchUseCase("query") } throws
            com.example.kairos_mobile.domain.model.ApiException.SubscriptionRequired("Premium required")

        // when: 시맨틱 검색 실행
        viewModel.toggleSemanticMode(true)
        viewModel.onSearchTextChanged("query")
        advanceTimeBy(350)
        advanceUntilIdle()

        // then: 시맨틱 모드 비활성화 및 에러 메시지 표시
        assertFalse(viewModel.uiState.value.isSemanticMode)
        assertEquals("Premium 구독이 필요합니다", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onErrorDismissed_clears_error_message`() = runTest {
        // given: 에러 발생
        coEvery { semanticSearchUseCase("query") } throws
            com.example.kairos_mobile.domain.model.ApiException.SubscriptionRequired("Premium required")

        viewModel.toggleSemanticMode(true)
        viewModel.onSearchTextChanged("query")
        advanceTimeBy(350)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage != null)

        // when: 에러 메시지 닫기
        viewModel.onErrorDismissed()
        advanceUntilIdle()

        // then: 에러 메시지 초기화
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

}
