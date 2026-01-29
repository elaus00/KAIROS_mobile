package com.example.kairos_mobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.util.KeywordMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Home 화면 ViewModel (PRD v4.0)
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var classificationJob: Job? = null

    init {
        loadRecentCaptures()
        loadTodaySchedule()
    }

    /**
     * 이벤트 처리
     */
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.UpdateInput -> updateInput(event.text)
            is HomeEvent.SetInputFocused -> setInputFocused(event.focused)
            is HomeEvent.ClearInput -> clearInput()
            is HomeEvent.Submit -> submit()
            is HomeEvent.SubmitWithType -> submitWithType(event.type)
            is HomeEvent.OpenCamera -> { /* 카메라 열기는 UI 레벨에서 처리 */ }
            is HomeEvent.OpenVoiceInput -> { /* 음성 입력은 UI 레벨에서 처리 */ }
            is HomeEvent.NavigateToCapture -> { /* 네비게이션은 UI 레벨에서 처리 */ }
            is HomeEvent.NavigateToCalendar -> { /* 네비게이션은 UI 레벨에서 처리 */ }
            is HomeEvent.DismissError -> dismissError()
            is HomeEvent.ClearSubmitSuccess -> clearSubmitSuccess()
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
     * 입력 업데이트
     */
    private fun updateInput(text: String) {
        _uiState.update { it.copy(
            inputText = text,
            characterCount = text.length
        ) }

        // 디바운스된 타입 추천
        classificationJob?.cancel()
        if (text.isNotBlank() && text.length >= 3) {
            classificationJob = viewModelScope.launch {
                delay(300) // 300ms 디바운스
                suggestType(text)
            }
        } else {
            _uiState.update { it.copy(
                suggestedType = null,
                isClassifying = false
            ) }
        }
    }

    /**
     * 키워드 기반 타입 추천
     */
    private fun suggestType(text: String) {
        _uiState.update { it.copy(isClassifying = true) }

        val matchedTypes = KeywordMatcher.matchTypes(text)
        val suggestedType = matchedTypes.firstOrNull()

        _uiState.update { it.copy(
            suggestedType = suggestedType,
            isClassifying = false,
            classificationConfidence = if (suggestedType != null) 0.8f else 0f
        ) }
    }

    /**
     * 입력 포커스 상태 설정
     */
    private fun setInputFocused(focused: Boolean) {
        _uiState.update { it.copy(isInputFocused = focused) }
    }

    /**
     * 입력 초기화
     */
    private fun clearInput() {
        _uiState.update { it.copy(
            inputText = "",
            characterCount = 0,
            suggestedType = null,
            isClassifying = false
        ) }
    }

    /**
     * 캡처 제출
     */
    private fun submit() {
        val currentText = _uiState.value.inputText
        if (currentText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val result = captureRepository.saveCapture(currentText)
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isSubmitting = false,
                        submitSuccess = true,
                        inputText = "",
                        characterCount = 0,
                        suggestedType = null
                    ) }
                    loadRecentCaptures()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isSubmitting = false,
                        errorMessage = result.exception.message ?: "캡처 저장에 실패했습니다."
                    ) }
                }
                is Result.Loading -> {
                    // 로딩 상태 유지
                }
            }
        }
    }

    /**
     * 특정 타입으로 캡처 제출
     */
    private fun submitWithType(type: CaptureType) {
        val currentText = _uiState.value.inputText
        if (currentText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val result = captureRepository.saveCaptureWithType(currentText, type)
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isSubmitting = false,
                        submitSuccess = true,
                        inputText = "",
                        characterCount = 0,
                        suggestedType = null
                    ) }
                    loadRecentCaptures()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isSubmitting = false,
                        errorMessage = result.exception.message ?: "캡처 저장에 실패했습니다."
                    ) }
                }
                is Result.Loading -> {
                    // 로딩 상태 유지
                }
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
