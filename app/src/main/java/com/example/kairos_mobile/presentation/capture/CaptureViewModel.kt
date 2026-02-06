package com.example.kairos_mobile.presentation.capture

import android.os.Trace
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.usecase.capture.DeleteDraftUseCase
import com.example.kairos_mobile.domain.usecase.capture.GetDraftUseCase
import com.example.kairos_mobile.domain.usecase.capture.SaveDraftUseCase
import com.example.kairos_mobile.domain.usecase.capture.SubmitCaptureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 캡처 화면 ViewModel
 * 텍스트 입력 → TEMP 저장 → SyncQueue에 CLASSIFY 등록
 */
@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val submitCaptureUseCase: SubmitCaptureUseCase,
    private val saveDraftUseCase: SaveDraftUseCase,
    private val getDraftUseCase: GetDraftUseCase,
    private val deleteDraftUseCase: DeleteDraftUseCase,
    private val captureRepository: CaptureRepository
) : ViewModel() {
    companion object {
        private const val TRACE_FIRST_INPUT_LATENCY = "first_input_latency"
    }


    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CaptureEvent>()
    val events: SharedFlow<CaptureEvent> = _events.asSharedFlow()

    init {
        observeUnconfirmedCount()
        loadDraft()
    }

    /**
     * 미확인 분류 수 실시간 관찰
     */
    private fun observeUnconfirmedCount() {
        viewModelScope.launch {
            captureRepository.getUnconfirmedCount().collect { count ->
                _uiState.update { it.copy(unconfirmedCount = count) }
            }
        }
    }

    /**
     * 임시 저장 텍스트 로드
     */
    private fun loadDraft() {
        viewModelScope.launch {
            val draft = getDraftUseCase()
            if (draft.isNotBlank()) {
                _uiState.update {
                    it.copy(
                        inputText = draft,
                        characterCount = draft.length
                    )
                }
            }
        }
    }

    /**
     * 입력 텍스트 업데이트
     */
    fun updateInput(text: String) {
        if (text.length > _uiState.value.maxCharacterCount) return
        val shouldTraceFirstInput = _uiState.value.inputText.isBlank() && text.isNotBlank()
        if (shouldTraceFirstInput) {
            Trace.beginSection(TRACE_FIRST_INPUT_LATENCY)
        }
        try {
            _uiState.update {
                it.copy(
                    inputText = text,
                    characterCount = text.length
                )
            }
        } finally {
            if (shouldTraceFirstInput) {
                Trace.endSection()
            }
        }
    }

    /**
     * 캡처 제출
     */
    fun submit() {
        val currentText = _uiState.value.inputText
        if (currentText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            try {
                submitCaptureUseCase(currentText)

                // 임시 저장 삭제
                deleteDraftUseCase()

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        inputText = "",
                        characterCount = 0
                    )
                }
                _events.emit(CaptureEvent.SubmitSuccess)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message ?: "저장에 실패했습니다."
                    )
                }
            }
        }
    }

    /**
     * 임시 저장 (앱 이탈 시)
     */
    fun saveDraft() {
        val currentText = _uiState.value.inputText
        if (currentText.isBlank()) return

        viewModelScope.launch {
            saveDraftUseCase(currentText)
        }
    }

    /**
     * AI Status Sheet 표시/숨김
     */
    fun toggleStatusSheet() {
        _uiState.update { it.copy(showStatusSheet = !it.showStatusSheet) }
    }

    /**
     * AI Status Sheet 닫기
     */
    fun dismissStatusSheet() {
        _uiState.update { it.copy(showStatusSheet = false) }
    }

    /**
     * 에러 메시지 닫기
     */
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
