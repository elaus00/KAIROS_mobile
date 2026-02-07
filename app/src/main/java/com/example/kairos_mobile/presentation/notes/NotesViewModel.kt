package com.example.kairos_mobile.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.FolderType
import com.example.kairos_mobile.domain.usecase.folder.CreateFolderUseCase
import com.example.kairos_mobile.domain.usecase.folder.DeleteFolderUseCase
import com.example.kairos_mobile.domain.usecase.folder.GetAllFoldersUseCase
import com.example.kairos_mobile.domain.usecase.folder.RenameFolderUseCase
import com.example.kairos_mobile.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
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
    private val createFolderUseCase: CreateFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val renameFolderUseCase: RenameFolderUseCase,
    private val noteRepository: NoteRepository
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

            combine(
                getAllFoldersUseCase(),
                noteRepository.getFolderNoteCounts()
            ) { folders, countMap ->
                folders.map { folder ->
                    FolderWithCount(folder, countMap[folder.id] ?: 0)
                }.filter { folderWithCount ->
                    if (folderWithCount.folder.type == FolderType.INBOX) {
                        folderWithCount.noteCount > 0
                    } else {
                        true
                    }
                }
            }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message)
                    }
                }
                .collect { foldersWithCount ->
                    _uiState.update {
                        it.copy(folders = foldersWithCount, isLoading = false)
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
            noteRepository.getNotesWithActiveCaptureByFolderId(folderId)
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
                .collect { notes ->
                    val notesWithCapture = notes.map { note ->
                        NoteWithCapture(
                            noteId = note.noteId,
                            captureId = note.captureId,
                            aiTitle = note.aiTitle,
                            originalText = note.originalText,
                            createdAt = note.createdAt
                        )
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
