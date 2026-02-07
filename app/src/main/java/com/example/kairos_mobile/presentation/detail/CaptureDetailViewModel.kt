package com.example.kairos_mobile.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ScheduleRepository
import com.example.kairos_mobile.domain.usecase.calendar.ApproveCalendarSuggestionUseCase
import com.example.kairos_mobile.domain.usecase.calendar.RejectCalendarSuggestionUseCase
import com.example.kairos_mobile.domain.usecase.calendar.SyncScheduleToCalendarUseCase
import com.example.kairos_mobile.domain.usecase.classification.ChangeClassificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 캡처 상세 화면 ViewModel
 * 분류 칩 변경 + 원문 확인
 */
@HiltViewModel
class CaptureDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val captureRepository: CaptureRepository,
    private val changeClassification: ChangeClassificationUseCase,
    private val scheduleRepository: ScheduleRepository,
    private val syncScheduleToCalendar: SyncScheduleToCalendarUseCase,
    private val approveSuggestion: ApproveCalendarSuggestionUseCase,
    private val rejectSuggestion: RejectCalendarSuggestionUseCase
) : ViewModel() {

    private val captureId: String = savedStateHandle.get<String>("captureId") ?: ""

    private val _uiState = MutableStateFlow(CaptureDetailUiState())
    val uiState: StateFlow<CaptureDetailUiState> = _uiState.asStateFlow()

    init {
        loadCapture()
    }

    /**
     * 캡처 로드
     */
    private fun loadCapture() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val capture = captureRepository.getCaptureById(captureId)
            if (capture != null) {
                // SCHEDULE 타입이면 일정의 동기화 상태 조회
                val schedule = if (capture.classifiedType == ClassifiedType.SCHEDULE) {
                    scheduleRepository.getScheduleByCaptureId(capture.id)
                } else null

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        captureId = capture.id,
                        originalText = capture.originalText,
                        aiTitle = capture.aiTitle,
                        classifiedType = capture.classifiedType,
                        noteSubType = capture.noteSubType,
                        imageUri = capture.imageUri,
                        createdAt = capture.createdAt,
                        scheduleId = schedule?.id,
                        calendarSyncStatus = schedule?.calendarSyncStatus,
                        googleEventId = schedule?.googleEventId
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "캡처를 찾을 수 없습니다")
                }
            }
        }
    }

    /**
     * 분류 유형 변경
     */
    fun onChangeClassification(newType: ClassifiedType, newSubType: NoteSubType? = null) {
        viewModelScope.launch {
            try {
                changeClassification(captureId, newType, newSubType)
                _uiState.update {
                    it.copy(
                        classifiedType = newType,
                        noteSubType = newSubType
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 캘린더 제안 승인
     */
    fun onApproveCalendarSync(scheduleId: String) {
        viewModelScope.launch {
            try {
                approveSuggestion(scheduleId)
                loadCapture()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    /**
     * 캘린더 제안 거부
     */
    fun onRejectCalendarSync(scheduleId: String) {
        viewModelScope.launch {
            try {
                rejectSuggestion(scheduleId)
                loadCapture()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
}
