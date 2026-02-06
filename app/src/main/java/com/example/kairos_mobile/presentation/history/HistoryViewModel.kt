package com.example.kairos_mobile.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.usecase.capture.GetAllCapturesUseCase
import com.example.kairos_mobile.domain.usecase.capture.HardDeleteCaptureUseCase
import com.example.kairos_mobile.domain.usecase.capture.SoftDeleteCaptureUseCase
import com.example.kairos_mobile.domain.usecase.capture.UndoDeleteCaptureUseCase
import com.example.kairos_mobile.domain.usecase.classification.ChangeClassificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 전체 기록 화면 ViewModel
 * 역시간순 페이지네이션 + 스와이프 삭제 + 실행 취소
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getAllCapturesUseCase: GetAllCapturesUseCase,
    private val softDeleteCaptureUseCase: SoftDeleteCaptureUseCase,
    private val hardDeleteCaptureUseCase: HardDeleteCaptureUseCase,
    private val undoDeleteCaptureUseCase: UndoDeleteCaptureUseCase,
    private val changeClassificationUseCase: ChangeClassificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HistoryEvent>()
    val events: SharedFlow<HistoryEvent> = _events.asSharedFlow()

    private var currentPage = 0
    private var loadJob: Job? = null
    private val pendingHardDeleteJobs = mutableMapOf<String, Job>()

    companion object {
        private const val PAGE_SIZE = 20
        private const val HARD_DELETE_DELAY_MS = 3_000L
    }

    init {
        loadFirstPage()
    }

    /**
     * 첫 페이지 로드
     */
    private fun loadFirstPage() {
        loadJob?.cancel()
        currentPage = 0
        _uiState.update { it.copy(isLoading = true, captures = emptyList(), hasMore = true) }

        loadJob = viewModelScope.launch {
            getAllCapturesUseCase(offset = 0, limit = PAGE_SIZE).collect { captures ->
                _uiState.update {
                    it.copy(
                        captures = captures,
                        isLoading = false,
                        hasMore = captures.size >= PAGE_SIZE
                    )
                }
            }
        }
    }

    /**
     * 다음 페이지 로드 (무한 스크롤)
     */
    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return

        _uiState.update { it.copy(isLoadingMore = true) }
        currentPage++

        viewModelScope.launch {
            try {
                val newCaptures = getAllCapturesUseCase(
                    offset = currentPage * PAGE_SIZE,
                    limit = PAGE_SIZE
                ).first()
                _uiState.update {
                    it.copy(
                        captures = (it.captures + newCaptures).distinctBy { c -> c.id },
                        isLoadingMore = false,
                        hasMore = newCaptures.size >= PAGE_SIZE
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        errorMessage = e.message ?: "기록을 불러오지 못했습니다."
                    )
                }
            }
        }
    }

    /**
     * 스와이프 삭제 (소프트 삭제)
     */
    fun deleteCaptureById(captureId: String) {
        viewModelScope.launch {
            try {
                softDeleteCaptureUseCase(captureId)
                // UI에서 즉시 제거
                _uiState.update {
                    it.copy(captures = it.captures.filter { c -> c.id != captureId })
                }
                scheduleHardDelete(captureId)
                _events.emit(HistoryEvent.DeleteSuccess(captureId))
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "삭제에 실패했습니다.")
                }
            }
        }
    }

    /**
     * 삭제 실행 취소
     */
    fun undoDelete(captureId: String) {
        viewModelScope.launch {
            try {
                pendingHardDeleteJobs.remove(captureId)?.cancel()
                undoDeleteCaptureUseCase(captureId)
                // 목록 새로고침
                loadFirstPage()
                _events.emit(HistoryEvent.UndoSuccess)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "실행 취소에 실패했습니다.")
                }
            }
        }
    }

    /**
     * 분류 유형 변경
     */
    fun changeClassification(
        captureId: String,
        newType: ClassifiedType,
        newSubType: NoteSubType?
    ) {
        viewModelScope.launch {
            try {
                changeClassificationUseCase(captureId, newType, newSubType)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "분류 변경에 실패했습니다.")
                }
            }
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun scheduleHardDelete(captureId: String) {
        pendingHardDeleteJobs.remove(captureId)?.cancel()
        pendingHardDeleteJobs[captureId] = viewModelScope.launch {
            try {
                delay(HARD_DELETE_DELAY_MS)
                hardDeleteCaptureUseCase(captureId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "완전 삭제에 실패했습니다.")
                }
            } finally {
                pendingHardDeleteJobs.remove(captureId)
            }
        }
    }

    override fun onCleared() {
        pendingHardDeleteJobs.values.forEach { it.cancel() }
        pendingHardDeleteJobs.clear()
        super.onCleared()
    }
}
