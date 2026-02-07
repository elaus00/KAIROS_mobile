package com.example.kairos_mobile.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.Capture
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.usecase.capture.GetAllCapturesUseCase
import com.example.kairos_mobile.domain.usecase.capture.HardDeleteCaptureUseCase
import com.example.kairos_mobile.domain.usecase.capture.SoftDeleteCaptureUseCase
import com.example.kairos_mobile.domain.usecase.capture.UndoDeleteCaptureUseCase
import com.example.kairos_mobile.domain.usecase.classification.ChangeClassificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.runBlocking
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

    private val pageJobs = mutableMapOf<Int, Job>()
    private val pageSnapshots = mutableMapOf<Int, List<Capture>>()
    private var highestPageRequested = -1
    private var loadingMorePage: Int? = null
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
        pageJobs.values.forEach { it.cancel() }
        pageJobs.clear()
        pageSnapshots.clear()
        highestPageRequested = -1
        loadingMorePage = null
        _uiState.update { it.copy(isLoading = true, captures = emptyList(), hasMore = true) }
        subscribePage(0)
    }

    /**
     * 다음 페이지 로드 (무한 스크롤)
     */
    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return

        val nextPage = highestPageRequested + 1
        if (pageJobs.containsKey(nextPage)) return

        loadingMorePage = nextPage
        _uiState.update { it.copy(isLoadingMore = true) }
        subscribePage(nextPage)
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

    private fun subscribePage(page: Int) {
        highestPageRequested = maxOf(highestPageRequested, page)
        pageJobs[page]?.cancel()
        pageJobs[page] = viewModelScope.launch {
            try {
                getAllCapturesUseCase(
                    offset = page * PAGE_SIZE,
                    limit = PAGE_SIZE
                ).collect { captures ->
                    pageSnapshots[page] = captures
                    val mergedCaptures = (0..highestPageRequested)
                        .flatMap { index -> pageSnapshots[index].orEmpty() }
                        .distinctBy { capture -> capture.id }
                    val hasMore = pageSnapshots[highestPageRequested]?.size ?: 0 >= PAGE_SIZE
                    val isLoadingMoreFinished = loadingMorePage == page
                    if (isLoadingMoreFinished) {
                        loadingMorePage = null
                    }
                    _uiState.update { state ->
                        state.copy(
                            captures = mergedCaptures,
                            isLoading = false,
                            isLoadingMore = if (isLoadingMoreFinished) false else state.isLoadingMore,
                            hasMore = hasMore,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                if (loadingMorePage == page) {
                    loadingMorePage = null
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = e.message ?: "기록을 불러오지 못했습니다."
                    )
                }
            }
        }
    }

    override fun onCleared() {
        pageJobs.values.forEach { it.cancel() }
        pageJobs.clear()
        val pendingIds = pendingHardDeleteJobs.keys.toList()
        pendingHardDeleteJobs.values.forEach { it.cancel() }
        pendingHardDeleteJobs.clear()
        runBlocking(Dispatchers.IO) {
            pendingIds.forEach { captureId ->
                runCatching { hardDeleteCaptureUseCase(captureId) }
            }
        }
        super.onCleared()
    }
}
