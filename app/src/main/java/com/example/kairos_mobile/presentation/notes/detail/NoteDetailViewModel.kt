package com.example.kairos_mobile.presentation.notes.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.FolderRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import com.example.kairos_mobile.domain.usecase.note.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 노트 상세 화면 ViewModel
 * 노트 조회, 편집, 자동 저장, 삭제 처리
 */
@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val folderRepository: FolderRepository,
    private val captureRepository: CaptureRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val noteId: String = savedStateHandle.get<String>("noteId") ?: ""

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    /** 삭제 후 휴지통 이동 예약 Job (실행 취소 시 취소) */
    private var trashJob: Job? = null

    init {
        loadNoteDetail()
        loadFolders()
    }

    /**
     * 노트 상세 로드
     */
    private fun loadNoteDetail() {
        viewModelScope.launch {
            noteRepository.getNoteDetail(noteId).collectLatest { noteDetail ->
                if (noteDetail != null) {
                    val currentState = _uiState.value
                    // 최초 로드 시에만 편집 필드 초기화
                    if (currentState.isLoading) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                noteDetail = noteDetail,
                                editedTitle = noteDetail.aiTitle ?: "",
                                editedBody = noteDetail.body ?: noteDetail.originalText,
                                selectedFolderId = noteDetail.folderId
                            )
                        }
                        // 캡처 재방문 분석 이벤트 발행 (최초 로드 시 1회)
                        val timeSinceCreation = System.currentTimeMillis() - noteDetail.createdAt
                        trackEventUseCase(
                            "capture_revisited",
                            """{"time_since_creation_ms":$timeSinceCreation,"access_method":"list"}"""
                        )
                    } else {
                        _uiState.update { it.copy(noteDetail = noteDetail) }
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "노트를 찾을 수 없습니다")
                    }
                }
            }
        }
    }

    /**
     * 폴더 목록 로드
     */
    private fun loadFolders() {
        viewModelScope.launch {
            folderRepository.getAllFolders().collectLatest { folders ->
                _uiState.update { it.copy(folders = folders) }
            }
        }
    }

    /**
     * 제목 변경
     */
    fun onTitleChanged(title: String) {
        _uiState.update {
            it.copy(
                editedTitle = title,
                hasChanges = checkChanges(title = title)
            )
        }
    }

    /**
     * 본문 변경
     */
    fun onBodyChanged(body: String) {
        _uiState.update {
            it.copy(
                editedBody = body,
                hasChanges = checkChanges(body = body)
            )
        }
    }

    /**
     * 폴더 변경
     */
    fun onFolderChanged(folderId: String?) {
        _uiState.update {
            it.copy(
                selectedFolderId = folderId,
                hasChanges = checkChanges(folderId = folderId)
            )
        }
    }

    /**
     * 원본 텍스트 접힘/펼침 토글
     */
    fun onToggleOriginalText() {
        _uiState.update { it.copy(showOriginalText = !it.showOriginalText) }
    }

    /**
     * 자동 저장 후 뒤로가기 — §4.1 입력 보호 원칙
     * 변경사항이 있으면 저장 후 navigate back 이벤트 발생
     */
    fun autoSaveAndExit() {
        val state = _uiState.value
        if (!state.hasChanges || state.isDeleted) {
            _uiState.update { it.copy(shouldNavigateBack = true) }
            return
        }

        viewModelScope.launch {
            saveChangesInternal()
            _uiState.update { it.copy(shouldNavigateBack = true) }
        }
    }

    /**
     * 저장 로직 (내부용)
     */
    private suspend fun saveChangesInternal() {
        val state = _uiState.value
        val noteDetail = state.noteDetail ?: return

        try {
            // 제목 변경 확인
            val titleChanged = state.editedTitle != (noteDetail.aiTitle ?: "")
            if (titleChanged && state.editedTitle.isNotBlank()) {
                updateNoteUseCase.updateTitle(noteDetail.captureId, state.editedTitle)
            }

            // 본문 변경 확인 (checkChanges와 동일 기준)
            val bodyChanged = state.editedBody != (noteDetail.body ?: noteDetail.originalText)
            if (bodyChanged) {
                val newBody = state.editedBody.ifBlank { null }
                updateNoteUseCase.updateBody(noteDetail.noteId, newBody)
            }

            // 폴더 변경 확인
            val folderChanged = state.selectedFolderId != noteDetail.folderId
            if (folderChanged && state.selectedFolderId != null) {
                updateNoteUseCase.moveToFolder(noteDetail.noteId, state.selectedFolderId)
            }

            _uiState.update { it.copy(hasChanges = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    /**
     * 노트 삭제 — §4.4 Snackbar 실행 취소 패턴
     * softDelete 후 5초 대기, 실행 취소 없으면 moveToTrash
     */
    fun onDelete() {
        val captureId = _uiState.value.noteDetail?.captureId ?: return

        viewModelScope.launch {
            captureRepository.softDelete(captureId)
            _uiState.update { it.copy(isDeleted = true) }
        }

        // 5초 후 휴지통 이동 예약
        trashJob = viewModelScope.launch {
            delay(SNACKBAR_DURATION_MS)
            captureRepository.moveToTrash(captureId)
            _uiState.update { it.copy(shouldNavigateBack = true) }
        }
    }

    /**
     * 삭제 실행 취소
     */
    fun onUndoDelete() {
        val captureId = _uiState.value.noteDetail?.captureId ?: return

        // 휴지통 이동 예약 취소
        trashJob?.cancel()
        trashJob = null

        viewModelScope.launch {
            captureRepository.undoSoftDelete(captureId)
            _uiState.update { it.copy(isDeleted = false) }
        }
    }

    /**
     * navigate back 이벤트 소비
     */
    fun onNavigateBackHandled() {
        _uiState.update { it.copy(shouldNavigateBack = false) }
    }

    /**
     * 데이터 재로드 (에러 시 다시 시도)
     */
    fun onRetry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadNoteDetail()
        loadFolders()
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 공유용 텍스트 생성
     */
    fun onShare() {
        val detail = _uiState.value.noteDetail ?: return
        val title = _uiState.value.editedTitle.ifBlank {
            detail.aiTitle ?: detail.originalText.take(50)
        }
        val content = _uiState.value.editedBody.ifBlank {
            detail.body ?: detail.originalText
        }
        val text = buildString {
            appendLine(title)
            appendLine()
            append(content)
        }
        _uiState.update { it.copy(shareText = text) }
    }

    /**
     * 공유 텍스트 소비 후 초기화
     */
    fun onShareHandled() {
        _uiState.update { it.copy(shareText = null) }
    }

    /**
     * 변경 여부 확인
     */
    private fun checkChanges(
        title: String = _uiState.value.editedTitle,
        body: String = _uiState.value.editedBody,
        folderId: String? = _uiState.value.selectedFolderId
    ): Boolean {
        val noteDetail = _uiState.value.noteDetail ?: return false
        return title != (noteDetail.aiTitle ?: "") ||
                body != (noteDetail.body ?: noteDetail.originalText) ||
                folderId != noteDetail.folderId
    }

    companion object {
        private const val SNACKBAR_DURATION_MS = 5000L
    }
}
