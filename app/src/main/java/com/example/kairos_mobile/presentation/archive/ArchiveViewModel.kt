package com.example.kairos_mobile.presentation.archive

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.usecase.insight.GetAllInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Archive 화면 ViewModel
 */
@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val getAllInsightsUseCase: GetAllInsightsUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "ArchiveViewModel"
    }

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    // 중복 구독 방지를 위한 Job 관리
    private var loadJob: Job? = null

    init {
        loadInsightsGroupedByDate()
    }

    /**
     * 날짜별로 그룹화된 인사이트 로드
     * 이전 로드 작업이 있으면 취소하고 새로 시작
     */
    private fun loadInsightsGroupedByDate() {
        // 이전 Job 취소
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // 로딩 시작
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                getAllInsightsUseCase.getInsightsGroupedByDate()
                    .collect { groupedInsights ->
                        _uiState.update {
                            it.copy(
                                groupedInsights = groupedInsights,
                                isLoading = false,
                                hasMore = false  // 날짜 그룹화는 일단 전체 로드
                            )
                        }
                        Log.d(TAG, "Loaded ${groupedInsights.size} date groups")
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "로드 실패"
                    )
                }
                Log.e(TAG, "Failed to load grouped insights", e)
            }
        }
    }

    /**
     * 인사이트 확장/축소 토글
     */
    fun onToggleExpand(insightId: String) {
        _uiState.update { state ->
            val newExpandedIds = if (insightId in state.expandedInsightIds) {
                state.expandedInsightIds - insightId
            } else {
                state.expandedInsightIds + insightId
            }
            state.copy(expandedInsightIds = newExpandedIds)
        }
    }

    /**
     * 새로고침
     */
    fun onRefresh() {
        loadInsightsGroupedByDate()
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
