package com.example.kairos_mobile.presentation.archive

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.usecase.GetAllCapturesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getAllCapturesUseCase: GetAllCapturesUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "ArchiveViewModel"
    }

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        loadCapturesGroupedByDate()
    }

    /**
     * 날짜별로 그룹화된 캡처 로드
     */
    private fun loadCapturesGroupedByDate() {
        viewModelScope.launch {
            // 로딩 시작
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                getAllCapturesUseCase.getCapturesGroupedByDate()
                    .collect { groupedCaptures ->
                        _uiState.update {
                            it.copy(
                                groupedCaptures = groupedCaptures,
                                isLoading = false,
                                hasMore = false  // 날짜 그룹화는 일단 전체 로드
                            )
                        }
                        Log.d(TAG, "Loaded ${groupedCaptures.size} date groups")
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "로드 실패"
                    )
                }
                Log.e(TAG, "Failed to load grouped captures", e)
            }
        }
    }

    /**
     * 캡처 확장/축소 토글
     */
    fun onToggleExpand(captureId: String) {
        _uiState.update { state ->
            val newExpandedIds = if (captureId in state.expandedCaptureIds) {
                state.expandedCaptureIds - captureId
            } else {
                state.expandedCaptureIds + captureId
            }
            state.copy(expandedCaptureIds = newExpandedIds)
        }
    }

    /**
     * 새로고침
     */
    fun onRefresh() {
        loadCapturesGroupedByDate()
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
