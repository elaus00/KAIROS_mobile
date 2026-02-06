package com.example.kairos_mobile.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * FTS 기반 실시간 검색 (300ms 디바운싱)
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCapturesUseCase: SearchCapturesUseCase
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
            executeSearch(text)
        }
    }

    /**
     * FTS 검색 실행
     */
    private fun executeSearch(query: String) {
        viewModelScope.launch {
            searchCapturesUseCase(query)
                .catch { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: "검색 실패")
                    }
                }
                .collect { results ->
                    _uiState.update {
                        it.copy(
                            searchResults = results,
                            hasSearched = true
                        )
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
