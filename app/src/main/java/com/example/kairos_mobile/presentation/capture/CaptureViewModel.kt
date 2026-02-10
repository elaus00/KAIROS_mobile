package com.example.kairos_mobile.presentation.capture

import android.os.Trace
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.ImageRepository
import com.example.kairos_mobile.domain.repository.UserPreferenceRepository
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
    private val userPreferenceRepository: UserPreferenceRepository,
    private val captureRepository: CaptureRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {
    companion object {
        private const val TRACE_FIRST_INPUT_LATENCY = "first_input_latency"
        private const val KEY_DRAFT_TEXT = "draft_capture"
        private const val KEY_CAPTURE_FONT_SIZE = "capture_font_size"
    }


    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CaptureEvent>()
    val events: SharedFlow<CaptureEvent> = _events.asSharedFlow()

    init {
        observeUnconfirmedCount()
        loadDraft()
        loadFontSize()
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
            val draft = userPreferenceRepository.getString(KEY_DRAFT_TEXT, "")
            if (draft.isNotBlank()) {
                _uiState.update {
                    it.copy(inputText = draft)
                }
            }
        }
    }

    /**
     * 글씨 크기 설정 로드 (설정 변경 후 화면 복귀 시 재호출)
     */
    fun loadFontSize() {
        viewModelScope.launch {
            val sizeKey = userPreferenceRepository.getString(KEY_CAPTURE_FONT_SIZE, "MEDIUM")
            val (fontSize, lineHeight) = when (sizeKey) {
                "SMALL" -> 16 to 28
                "LARGE" -> 24 to 40
                else -> 20 to 34
            }
            _uiState.update { it.copy(fontSize = fontSize, lineHeight = lineHeight) }
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
                it.copy(inputText = text)
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
        val currentImageUri = _uiState.value.imageUri
        if (currentText.isBlank() && currentImageUri == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            try {
                // 이미지가 있으면 내부 저장소에 복사
                val savedImageUri = currentImageUri?.let {
                    imageRepository.saveImage(Uri.parse(it))
                }

                submitCaptureUseCase(
                    text = currentText.ifBlank { "이미지 캡처" },
                    imageUri = savedImageUri
                )

                // 임시 저장 삭제
                userPreferenceRepository.setString(KEY_DRAFT_TEXT, "")

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        inputText = "",
                        imageUri = null
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
     * 이미지 선택 처리
     */
    fun handleImageSelected(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri.toString()) }
    }

    /**
     * 첨부 이미지 제거
     */
    fun removeImage() {
        _uiState.update { it.copy(imageUri = null) }
    }

    /**
     * 임시 저장 (앱 이탈 시)
     */
    fun saveDraft() {
        val currentText = _uiState.value.inputText
        if (currentText.isBlank()) return

        viewModelScope.launch {
            userPreferenceRepository.setString(KEY_DRAFT_TEXT, currentText)
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
