package com.example.kairos_mobile.presentation.notes.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.repository.FolderRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import com.example.kairos_mobile.domain.usecase.note.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 노트 상세 화면 ViewModel
 * 노트 조회, 편집, 저장 처리
 */
@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val folderRepository: FolderRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val noteId: String = savedStateHandle.get<String>("noteId") ?: ""

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

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
                                editedBody = noteDetail.body ?: "",
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
     * 변경사항 저장
     */
    fun onSave() {
        val state = _uiState.value
        val noteDetail = state.noteDetail ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                // 제목 변경 확인
                val titleChanged = state.editedTitle != (noteDetail.aiTitle ?: "")
                if (titleChanged && state.editedTitle.isNotBlank()) {
                    updateNoteUseCase.updateTitle(noteDetail.captureId, state.editedTitle)
                }

                // 본문 변경 확인
                val bodyChanged = state.editedBody != (noteDetail.body ?: "")
                if (bodyChanged) {
                    val newBody = state.editedBody.ifBlank { null }
                    updateNoteUseCase.updateBody(noteDetail.noteId, newBody)
                }

                // 폴더 변경 확인
                val folderChanged = state.selectedFolderId != noteDetail.folderId
                if (folderChanged && state.selectedFolderId != null) {
                    updateNoteUseCase.moveToFolder(noteDetail.noteId, state.selectedFolderId)
                }

                _uiState.update { it.copy(isSaving = false, hasChanges = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    /**
     * 에러 메시지 닫기
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(error = null) }
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
                body != (noteDetail.body ?: "") ||
                folderId != noteDetail.folderId
    }
}
