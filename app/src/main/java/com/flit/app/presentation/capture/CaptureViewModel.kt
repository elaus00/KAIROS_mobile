package com.flit.app.presentation.capture

import android.app.Application
import android.os.Trace
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.ImageRepository
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.capture.SubmitCaptureUseCase
import com.flit.app.presentation.widget.WidgetUpdateHelper
import com.flit.app.tracing.AppTrace
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
    private val application: Application,
    private val submitCaptureUseCase: SubmitCaptureUseCase,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val captureRepository: CaptureRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {
    companion object {
        private const val TRACE_FIRST_INPUT_LATENCY = "first_input_latency"
        private const val TRACE_CAPTURE_SAVE = "capture_save_completion"
        private const val KEY_DRAFT_TEXT = "draft_capture"
        private const val KEY_DRAFT_IMAGE_URI = "draft_capture_image_uri"
    }


    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CaptureEvent>()
    val events: SharedFlow<CaptureEvent> = _events.asSharedFlow()
    private var lastSavedDraftText: String = ""
    private var lastSavedDraftImageUri: String? = null

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
            val draft = userPreferenceRepository.getString(KEY_DRAFT_TEXT, "")
            val draftImageUri = userPreferenceRepository.getString(KEY_DRAFT_IMAGE_URI, "")
            val normalizedDraftImageUri = draftImageUri.takeIf { uri -> uri.isNotBlank() }
            lastSavedDraftText = draft
            lastSavedDraftImageUri = normalizedDraftImageUri
            _uiState.update {
                it.copy(
                    inputText = draft,
                    imageUri = normalizedDraftImageUri
                )
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
        if (_uiState.value.isSubmitting) return
        val currentText = _uiState.value.inputText
        val currentImageUri = _uiState.value.imageUri
        if (currentText.isBlank() && currentImageUri == null) return

        viewModelScope.launch {
            AppTrace.suspendSection(TRACE_CAPTURE_SAVE) {
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
                    userPreferenceRepository.setString(KEY_DRAFT_IMAGE_URI, "")
                    lastSavedDraftText = ""
                    lastSavedDraftImageUri = null

                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            inputText = "",
                            imageUri = null
                        )
                    }
                    _events.emit(CaptureEvent.SubmitSuccess)
                    // 위젯 갱신
                    WidgetUpdateHelper.updateCaptureWidget(application)
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = e.message ?: "저장에 실패했어요. 다시 시도해주세요"
                        )
                    }
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
        val currentImageUri = _uiState.value.imageUri
        if (currentText == lastSavedDraftText && currentImageUri == lastSavedDraftImageUri) return

        viewModelScope.launch {
            userPreferenceRepository.setString(KEY_DRAFT_TEXT, currentText)
            userPreferenceRepository.setString(KEY_DRAFT_IMAGE_URI, currentImageUri ?: "")
            lastSavedDraftText = currentText
            lastSavedDraftImageUri = currentImageUri
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
