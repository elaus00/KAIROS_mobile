package com.flit.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.ApiException
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.domain.repository.SubscriptionRepository
import com.flit.app.domain.usecase.analytics.TrackEventUseCase
import com.flit.app.domain.usecase.search.SearchCapturesUseCase
import com.flit.app.domain.usecase.search.SemanticSearchUseCase
import com.flit.app.tracing.AppTrace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 검색 화면 ViewModel
 * FTS 기반 실시간 검색 (300ms 디바운싱) + 분류/날짜 필터
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCapturesUseCase: SearchCapturesUseCase,
    private val trackEventUseCase: TrackEventUseCase,
    private val semanticSearchUseCase: SemanticSearchUseCase,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    companion object {
        private const val TRACE_LOCAL_SEARCH_EXECUTION = "local_search_execution"
        private const val TRACE_SEMANTIC_SEARCH_EXECUTION = "semantic_search_execution"
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSubscriptionStatus()
    }

    /** 구독 상태 로드 — AI 시맨틱 검색 토글 표시 제어 */
    private fun loadSubscriptionStatus() {
        val isPremium = subscriptionRepository.getCachedTier() == SubscriptionTier.PREMIUM
        _uiState.update { it.copy(isPremium = isPremium) }
    }

    /**
     * 검색 텍스트 변경 (디바운싱 적용)
     */
    fun onSearchTextChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }

        searchJob?.cancel()
        if (text.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), semanticResults = emptyList(), hasSearched = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            if (_uiState.value.isSemanticMode) {
                executeSemanticSearch()
            } else {
                executeSearch()
            }
        }
    }

    /**
     * 분류 유형 필터 변경
     */
    fun setTypeFilter(type: ClassifiedType?) {
        _uiState.update { it.copy(selectedType = type) }
        triggerSearch()
    }

    /**
     * 필터 변경 시 즉시 검색 재실행
     */
    private fun triggerSearch() {
        val query = _uiState.value.searchText
        if (query.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            executeSearch()
        }
    }

    /**
     * 검색 실행 (필터 유무에 따라 분기)
     */
    private suspend fun executeSearch() {
        val state = _uiState.value
        val query = state.searchText
        if (query.isBlank()) return

        val hasFilters = state.selectedType != null

        try {
            if (hasFilters) {
                AppTrace.suspendSection(TRACE_LOCAL_SEARCH_EXECUTION) {
                    // 필터 포함 검색 (suspend)
                    val results = searchCapturesUseCase.searchFiltered(
                        query = query,
                        type = state.selectedType,
                        startDate = null,
                        endDate = null
                    )
                    _uiState.update {
                        it.copy(searchResults = results, hasSearched = true)
                    }

                    // 검색 수행 분석 이벤트
                    trackEventUseCase(
                        eventType = "search_performed",
                        eventData = """{"result_count":${results.size},"result_clicked":false,"filtered":true}"""
                    )
                }
            } else {
                // 기본 검색 (Flow 수집)
                searchCapturesUseCase(query)
                    .catch { e ->
                        _uiState.update {
                            it.copy(errorMessage = e.message ?: "검색 실패")
                        }
                    }
                    .collect { results ->
                        _uiState.update {
                            it.copy(searchResults = results, hasSearched = true)
                        }

                        // 검색 수행 분석 이벤트
                        trackEventUseCase(
                            eventType = "search_performed",
                            eventData = """{"result_count":${results.size},"result_clicked":false}"""
                        )
                    }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = e.message ?: "검색 실패")
            }
        }
    }

    /**
     * AI 시맨틱 검색 모드 토글
     */
    fun toggleSemanticMode(enabled: Boolean) {
        _uiState.update { it.copy(isSemanticMode = enabled, semanticResults = emptyList()) }
        if (enabled && _uiState.value.searchText.isNotBlank()) {
            executeSemanticSearch()
        } else if (!enabled && _uiState.value.searchText.isNotBlank()) {
            triggerSearch()
        }
    }

    /**
     * 시맨틱 검색 실행
     */
    private fun executeSemanticSearch() {
        val query = _uiState.value.searchText
        if (query.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSemanticLoading = true) }
            AppTrace.suspendSection(TRACE_SEMANTIC_SEARCH_EXECUTION) {
                try {
                    val results = semanticSearchUseCase(query)
                    _uiState.update {
                        it.copy(
                            semanticResults = results,
                            isSemanticLoading = false,
                            hasSearched = true
                        )
                    }
                } catch (e: ApiException.SubscriptionRequired) {
                    _uiState.update {
                        it.copy(
                            isSemanticMode = false,
                            isSemanticLoading = false,
                            errorMessage = "Premium 구독이 필요합니다"
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isSemanticLoading = false,
                            errorMessage = e.message ?: "시맨틱 검색 실패"
                        )
                    }
                }
            }
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
