package com.example.kairos_mobile.presentation.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.CaptureSource
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.model.SearchQuery
import com.example.kairos_mobile.domain.usecase.search.SearchCapturesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Search 화면 ViewModel
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCapturesUseCase: SearchCapturesUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "SearchViewModel"
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /**
     * 검색 텍스트 변경
     */
    fun onSearchTextChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }
    }

    /**
     * 타입 필터 토글
     */
    fun onTypeFilterToggle(type: CaptureType) {
        _uiState.update { state ->
            val newTypes = if (type in state.selectedTypes) {
                state.selectedTypes - type
            } else {
                state.selectedTypes + type
            }
            state.copy(selectedTypes = newTypes)
        }
    }

    /**
     * 소스 필터 토글
     */
    fun onSourceFilterToggle(source: CaptureSource) {
        _uiState.update { state ->
            val newSources = if (source in state.selectedSources) {
                state.selectedSources - source
            } else {
                state.selectedSources + source
            }
            state.copy(selectedSources = newSources)
        }
    }

    /**
     * 검색 실행
     */
    fun onSearch() {
        val state = _uiState.value

        // 검색 쿼리 생성
        val query = SearchQuery(
            text = state.searchText,
            types = state.selectedTypes,
            sources = state.selectedSources,
            dateRange = state.dateRange
        )

        viewModelScope.launch {
            // 로딩 시작 (첫 검색이므로 오프셋 0)
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    hasSearched = true,
                    currentOffset = 0,
                    searchResults = emptyList()  // 기존 결과 초기화
                )
            }

            // 검색 수행
            when (val result = searchCapturesUseCase(query, offset = 0, limit = state.pageSize)) {
                is Result.Success -> {
                    val results = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = results,
                            hasMore = results.size >= state.pageSize,
                            currentOffset = results.size
                        )
                    }
                    Log.d(TAG, "Search completed: ${results.size} results")
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "검색 실패"
                        )
                    }
                    Log.e(TAG, "Search failed", result.exception)
                }
                is Result.Loading -> {
                    // 로딩 상태는 이미 설정됨
                }
            }
        }
    }

    /**
     * 더 많은 결과 로드 (페이징)
     */
    fun onLoadMore() {
        val state = _uiState.value

        // 이미 로딩 중이거나 더 이상 결과가 없으면 중단
        if (state.isLoading || !state.hasMore) {
            return
        }

        // 검색 쿼리 생성
        val query = SearchQuery(
            text = state.searchText,
            types = state.selectedTypes,
            sources = state.selectedSources,
            dateRange = state.dateRange
        )

        viewModelScope.launch {
            // 로딩 시작
            _uiState.update { it.copy(isLoading = true) }

            // 추가 결과 로드
            when (val result = searchCapturesUseCase(
                query,
                offset = state.currentOffset,
                limit = state.pageSize
            )) {
                is Result.Success -> {
                    val newResults = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = it.searchResults + newResults,
                            hasMore = newResults.size >= state.pageSize,
                            currentOffset = it.currentOffset + newResults.size
                        )
                    }
                    Log.d(TAG, "Loaded ${newResults.size} more results")
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "추가 로드 실패"
                        )
                    }
                    Log.e(TAG, "Load more failed", result.exception)
                }
                is Result.Loading -> {
                    // 로딩 상태는 이미 설정됨
                }
            }
        }
    }

    /**
     * 모든 필터 초기화
     */
    fun onClearFilters() {
        _uiState.update {
            it.copy(
                searchText = "",
                selectedTypes = emptySet(),
                selectedSources = emptySet(),
                dateRange = null
            )
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
