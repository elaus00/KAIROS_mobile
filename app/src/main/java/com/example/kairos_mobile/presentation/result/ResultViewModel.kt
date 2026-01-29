package com.example.kairos_mobile.presentation.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.Classification
import com.example.kairos_mobile.domain.model.ConfidenceLevel
import com.example.kairos_mobile.domain.model.CaptureType
import com.example.kairos_mobile.domain.model.Result
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.usecase.todo.CreateTodoFromCaptureUseCase
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
 * ResultScreen ViewModel
 * 신뢰도 기반 UI 분기 및 저장 처리
 */
@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val captureRepository: CaptureRepository,
    private val createTodoFromCaptureUseCase: CreateTodoFromCaptureUseCase
) : ViewModel() {

    private val captureId: String = savedStateHandle.get<String>("captureId") ?: ""

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ResultEvent>()
    val events: SharedFlow<ResultEvent> = _events.asSharedFlow()

    private var autoSaveJob: Job? = null

    init {
        loadCapture()
    }

    /**
     * 캡처 로드
     */
    private fun loadCapture() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, captureId = captureId) }

            when (val result = captureRepository.getCaptureById(captureId)) {
                is Result.Success -> {
                    val capture = result.data
                    if (capture != null) {
                        val classification = capture.classification
                        val confidence = classification?.confidence ?: 0f
                        val confidenceLevel = ConfidenceLevel.fromConfidence(confidence)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                content = capture.content,
                                classification = classification,
                                confidenceLevel = confidenceLevel
                            )
                        }

                        // 95% 이상이면 자동저장 타이머 시작
                        if (confidenceLevel == ConfidenceLevel.HIGH) {
                            startAutoSaveTimer()
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "캡처를 찾을 수 없습니다"
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "로드 실패"
                        )
                    }
                }
                is Result.Loading -> {
                    // 이미 로딩 상태
                }
            }
        }
    }

    /**
     * 자동저장 타이머 시작 (3초)
     */
    private fun startAutoSaveTimer() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isAutoSaveActive = true, autoSaveProgress = 0f, autoSaveCountdown = 3)
            }

            // 3초 동안 프로그레스 업데이트
            val totalDuration = 3000L
            val interval = 50L
            var elapsed = 0L

            while (elapsed < totalDuration) {
                delay(interval)
                elapsed += interval
                val progress = elapsed.toFloat() / totalDuration
                val countdown = ((totalDuration - elapsed) / 1000).toInt() + 1

                _uiState.update {
                    it.copy(
                        autoSaveProgress = progress,
                        autoSaveCountdown = countdown.coerceAtLeast(1)
                    )
                }
            }

            // 타이머 완료 → 자동 저장
            onConfirmSave()
        }
    }

    /**
     * 자동저장 중지 (수정 버튼 클릭 시)
     */
    fun onStopAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = null
        _uiState.update {
            it.copy(
                isAutoSaveActive = false,
                autoSaveProgress = 0f,
                isEditMode = true
            )
        }
    }

    /**
     * 수정 모드 진입
     */
    fun onEnterEditMode() {
        autoSaveJob?.cancel()
        _uiState.update {
            it.copy(
                isAutoSaveActive = false,
                isEditMode = true,
                editedType = it.classification?.type,
                editedTitle = it.classification?.title ?: "",
                editedTags = it.classification?.tags ?: emptyList()
            )
        }
    }

    /**
     * 수정 모드 종료
     */
    fun onExitEditMode() {
        _uiState.update {
            it.copy(
                isEditMode = false,
                editedType = null,
                editedTitle = "",
                editedTags = emptyList()
            )
        }
    }

    /**
     * 타입 선택 (LOW 신뢰도 또는 수정 모드에서)
     */
    fun onTypeSelected(type: CaptureType) {
        _uiState.update { it.copy(editedType = type) }
    }

    /**
     * 제목 수정
     */
    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(editedTitle = title) }
    }

    /**
     * 태그 추가
     */
    fun onTagAdded(tag: String) {
        if (tag.isBlank()) return
        _uiState.update {
            it.copy(editedTags = it.editedTags + tag.trim())
        }
    }

    /**
     * 태그 제거
     */
    fun onTagRemoved(tag: String) {
        _uiState.update {
            it.copy(editedTags = it.editedTags - tag)
        }
    }

    /**
     * 저장 확인 (이대로 저장 또는 자동저장 완료)
     */
    fun onConfirmSave() {
        val state = _uiState.value
        val type = state.currentType ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // TODO 타입인 경우 투두 생성
            if (type == CaptureType.TODO) {
                val classification = state.classification?.copy(
                    type = type,
                    title = state.currentTitle,
                    tags = state.currentTags
                ) ?: return@launch

                when (val result = createTodoFromCaptureUseCase(captureId, classification)) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isSaving = false, isSaved = true) }
                        delay(300)
                        _events.emit(ResultEvent.NavigateBack)
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = result.exception.message ?: "저장 실패"
                            )
                        }
                    }
                    is Result.Loading -> {}
                }
            } else {
                // 다른 타입은 이미 저장되어 있으므로 바로 완료
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
                delay(300)
                _events.emit(ResultEvent.NavigateBack)
            }
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}

/**
 * ResultScreen 이벤트
 */
sealed class ResultEvent {
    data object NavigateBack : ResultEvent()
}
