package com.example.kairos_mobile.presentation.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.usecase.capture.DeleteDraftUseCase
import com.example.kairos_mobile.domain.usecase.capture.GetDraftsUseCase
import com.example.kairos_mobile.domain.usecase.capture.SaveDraftUseCase
import com.example.kairos_mobile.domain.util.KeywordMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
 * QuickCapture 오버레이/팝업 ViewModel
 */
@HiltViewModel
class QuickCaptureViewModel @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val saveDraftUseCase: SaveDraftUseCase,
    private val getDraftsUseCase: GetDraftsUseCase,
    private val deleteDraftUseCase: DeleteDraftUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickCaptureUiState())
    val uiState: StateFlow<QuickCaptureUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QuickCaptureEvent>()
    val events: SharedFlow<QuickCaptureEvent> = _events.asSharedFlow()

    private var classificationJob: Job? = null

    init {
        loadDrafts()
    }

    /**
     * Draft 목록 로드
     */
    private fun loadDrafts() {
        viewModelScope.launch {
            getDraftsUseCase().collect { drafts ->
                _uiState.update { it.copy(drafts = drafts) }
            }
        }
    }

    /**
     * 입력 텍스트 업데이트
     */
    fun updateInput(text: String) {
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
            isClassifying = false
        ) }
    }

    /**
     * 분류 타입 수동 변경
     */
    fun setType(type: CaptureType) {
        _uiState.update { it.copy(suggestedType = type) }
    }

    /**
     * 캡처 제출
     */
    fun submit() {
        val currentText = _uiState.value.inputText
        if (currentText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val suggestedType = _uiState.value.suggestedType
            val result = if (suggestedType != null) {
                captureRepository.saveCaptureWithType(currentText, suggestedType)
            } else {
                captureRepository.saveCapture(currentText)
            }

            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isSubmitting = false,
                        inputText = "",
                        characterCount = 0,
                        suggestedType = null
                    ) }
                    _events.emit(QuickCaptureEvent.SubmitSuccess)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isSubmitting = false,
                        errorMessage = result.exception.message ?: "저장에 실패했습니다."
                    ) }
                }
                is Result.Loading -> {
                    // 로딩 상태 유지
                }
            }
        }
    }

    /**
     * Draft로 저장 (축소 버튼)
     */
    fun saveDraft() {
        val currentText = _uiState.value.inputText
        if (currentText.isBlank()) {
            viewModelScope.launch {
                _events.emit(QuickCaptureEvent.MinimizeSuccess)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMinimizing = true) }

            val result = saveDraftUseCase(currentText, _uiState.value.suggestedType)
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isMinimizing = false,
                        inputText = "",
                        characterCount = 0,
                        suggestedType = null
                    ) }
                    _events.emit(QuickCaptureEvent.MinimizeSuccess)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isMinimizing = false,
                        errorMessage = result.exception.message ?: "임시저장에 실패했습니다."
                    ) }
                }
                is Result.Loading -> {
                    // 로딩 상태 유지
                }
            }
        }
    }

    /**
     * 닫기 (내용 삭제)
     */
    fun close() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                inputText = "",
                characterCount = 0,
                suggestedType = null
            ) }
            _events.emit(QuickCaptureEvent.CloseSuccess)
        }
    }

    /**
     * Draft 선택 (이어서 작성)
     */
    fun selectDraft(draft: Capture) {
        _uiState.update { it.copy(
            inputText = draft.content,
            characterCount = draft.content.length,
            selectedDraftId = draft.id
        ) }

        // 타입 추천 수행
        if (draft.content.length >= 3) {
            suggestType(draft.content)
        }
    }

    /**
     * Draft 삭제
     */
    fun deleteDraft(draftId: String) {
        viewModelScope.launch {
            deleteDraftUseCase(draftId)
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 입력창 초기화
     */
    fun clearInput() {
        _uiState.update { it.copy(
            inputText = "",
            characterCount = 0,
            suggestedType = null,
            selectedDraftId = null
        ) }
    }
}

/**
 * QuickCapture UI 상태
 */
data class QuickCaptureUiState(
    // 입력
    val inputText: String = "",
    val characterCount: Int = 0,
    val maxCharacterCount: Int = 500,

    // 분류
    val suggestedType: CaptureType? = null,
    val isClassifying: Boolean = false,

    // Draft
    val drafts: List<Capture> = emptyList(),
    val selectedDraftId: String? = null,

    // 처리 상태
    val isSubmitting: Boolean = false,
    val isMinimizing: Boolean = false,
    val errorMessage: String? = null
)

/**
 * QuickCapture 이벤트
 */
sealed class QuickCaptureEvent {
    /** 전송 성공 */
    data object SubmitSuccess : QuickCaptureEvent()

    /** 축소 성공 (Draft 저장 완료) */
    data object MinimizeSuccess : QuickCaptureEvent()

    /** 닫기 성공 */
    data object CloseSuccess : QuickCaptureEvent()
}
