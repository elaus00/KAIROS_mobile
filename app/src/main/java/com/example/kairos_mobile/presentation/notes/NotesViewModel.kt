package com.example.kairos_mobile.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.FolderType
import com.example.kairos_mobile.domain.usecase.folder.CreateFolderUseCase
import com.example.kairos_mobile.domain.usecase.folder.DeleteFolderUseCase
import com.example.kairos_mobile.domain.usecase.folder.GetAllFoldersUseCase
import com.example.kairos_mobile.domain.usecase.folder.RenameFolderUseCase
import com.example.kairos_mobile.domain.usecase.note.GetNotesByFolderUseCase
import com.example.kairos_mobile.domain.repository.CaptureRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 노트 탭 ViewModel
 * 폴더 기반 노트 관리
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getAllFoldersUseCase: GetAllFoldersUseCase,
    private val getNotesByFolderUseCase: GetNotesByFolderUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val renameFolderUseCase: RenameFolderUseCase,
    private val noteRepository: NoteRepository,
    private val captureRepository: CaptureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private var foldersJob: Job? = null
    private var notesJob: Job? = null

    init {
        loadFolders()
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.SelectFolder -> selectFolder(event.folder)
            is NotesEvent.BackToFolders -> backToFolders()
            is NotesEvent.ShowCreateFolderDialog -> showCreateFolderDialog()
            is NotesEvent.DismissCreateFolderDialog -> dismissCreateFolderDialog()
            is NotesEvent.CreateFolder -> createFolder(event.name)
            is NotesEvent.ShowRenameFolderDialog -> showRenameFolderDialog(event.folder)
            is NotesEvent.DismissRenameFolderDialog -> dismissRenameFolderDialog()
            is NotesEvent.RenameFolder -> renameFolder(event.folderId, event.newName)
            is NotesEvent.DeleteFolder -> deleteFolder(event.folderId)
            is NotesEvent.DismissError -> dismissError()
        }
    }

    /**
     * 폴더 목록 로드
     * Inbox는 노트가 있을 때만 표시, Ideas/Bookmarks는 항상 표시
     */
    private fun loadFolders() {
        foldersJob?.cancel()
        foldersJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getAllFoldersUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message)
                    }
                }
                .collect { folders ->
                    // 각 폴더의 노트 수 조회
                    val foldersWithCount = folders.map { folder ->
                        FolderWithCount(folder, 0)
                    }
                    _uiState.update {
                        it.copy(folders = foldersWithCount, isLoading = false)
                    }
                    // 개별 노트 수 업데이트
                    folders.forEach { folder -> loadNoteCount(folder) }
                }
        }
    }

    /**
     * 폴더별 노트 수 로드
     */
    private fun loadNoteCount(folder: Folder) {
        viewModelScope.launch {
            noteRepository.getNoteCountByFolderId(folder.id)
                .catch { /* 에러 무시 */ }
                .collect { count ->
                    _uiState.update { state ->
                        val updatedFolders = state.folders.map { fc ->
                            if (fc.folder.id == folder.id) fc.copy(noteCount = count)
                            else fc
                        }.filter { fc ->
                            // Inbox는 노트가 있을 때만 표시
                            if (fc.folder.type == FolderType.INBOX) fc.noteCount > 0
                            else true
                        }
                        state.copy(folders = updatedFolders)
                    }
                }
        }
    }

    /**
     * 폴더 선택 → 노트 목록 표시
     */
    private fun selectFolder(folder: Folder) {
        _uiState.update { it.copy(selectedFolder = folder) }
        loadNotesForFolder(folder.id)
    }

    /**
     * 폴더 목록으로 돌아가기
     */
    private fun backToFolders() {
        notesJob?.cancel()
        _uiState.update { it.copy(selectedFolder = null, notes = emptyList()) }
    }

    /**
     * 폴더 내 노트 로드
     */
    private fun loadNotesForFolder(folderId: String) {
        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            getNotesByFolderUseCase(folderId)
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
                .collect { notes ->
                    // 노트 + 캡처 정보 조합
                    val notesWithCapture = notes.mapNotNull { note ->
                        val capture = captureRepository.getCaptureById(note.captureId)
                        capture?.let {
                            NoteWithCapture(
                                noteId = note.id,
                                captureId = note.captureId,
                                aiTitle = it.aiTitle,
                                originalText = it.originalText,
                                createdAt = it.createdAt
                            )
                        }
                    }
                    _uiState.update { it.copy(notes = notesWithCapture) }
                }
        }
    }

    private fun showCreateFolderDialog() {
        _uiState.update { it.copy(showCreateFolderDialog = true) }
    }

    private fun dismissCreateFolderDialog() {
        _uiState.update { it.copy(showCreateFolderDialog = false) }
    }

    private fun createFolder(name: String) {
        viewModelScope.launch {
            try {
                createFolderUseCase(name)
                _uiState.update { it.copy(showCreateFolderDialog = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun showRenameFolderDialog(folder: Folder) {
        _uiState.update { it.copy(renamingFolder = folder) }
    }

    private fun dismissRenameFolderDialog() {
        _uiState.update { it.copy(renamingFolder = null) }
    }

    private fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            try {
                renameFolderUseCase(folderId, newName)
                _uiState.update { it.copy(renamingFolder = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            try {
                deleteFolderUseCase(folderId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
