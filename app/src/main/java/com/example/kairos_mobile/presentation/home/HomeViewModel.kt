package com.example.kairos_mobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Home 화면 ViewModel (PRD v4.0)
 * 입력 기능은 QuickCaptureViewModel로 분리됨
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecentCaptures()
        loadTodaySchedule()
    }

    /**
     * 이벤트 처리
     */
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.DismissError -> dismissError()
            is HomeEvent.ClearSubmitSuccess -> clearSubmitSuccess()
            else -> { /* QuickCapture로 이동됨 */ }
        }
    }

    /**
     * 최근 캡처 로드
     */
    private fun loadRecentCaptures() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCaptures = true) }
            captureRepository.getRecentCaptures(6).collect { captures ->
                _uiState.update { it.copy(
                    recentCaptures = captures,
                    isLoadingCaptures = false
                ) }
            }
        }
    }

    /**
     * 오늘 일정 로드
     */
    private fun loadTodaySchedule() {
        viewModelScope.launch {
            val today = LocalDate.now()
            scheduleRepository.getSchedulesByDate(today).collect { schedules ->
                val nextSchedule = schedules.firstOrNull { !it.isPast() }
                _uiState.update { it.copy(
                    nextSchedule = nextSchedule,
                    todayScheduleCount = schedules.size
                ) }
            }
        }
    }

    /**
     * 오류 메시지 닫기
     */
    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 제출 성공 상태 초기화
     */
    private fun clearSubmitSuccess() {
        _uiState.update { it.copy(submitSuccess = false) }
    }
}
