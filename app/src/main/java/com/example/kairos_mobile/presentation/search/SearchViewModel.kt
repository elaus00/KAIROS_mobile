package com.example.kairos_mobile.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import com.example.kairos_mobile.domain.usecase.search.SearchCapturesUseCase
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
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    /**
     * 검색 텍스트 변경 (디바운싱 적용)
     */
    fun onSearchTextChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }

        searchJob?.cancel()
        if (text.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), hasSearched = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            executeSearch()
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
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
