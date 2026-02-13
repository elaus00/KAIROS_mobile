package com.flit.app.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.FolderType
import com.flit.app.domain.model.NoteViewType
import com.flit.app.domain.model.NoteWithCapturePreview
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.FolderRepository
import com.flit.app.domain.repository.NoteRepository
import com.flit.app.domain.repository.SubscriptionRepository
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.folder.CreateFolderUseCase
import com.flit.app.domain.usecase.folder.RenameFolderUseCase
import com.flit.app.domain.usecase.note.UpdateNoteUseCase
import com.flit.app.domain.usecase.settings.PreferenceKeys
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
 * 노트 우선 뷰 — 전체 노트를 플랫 리스트로 보여주고 폴더 필터 칩으로 분류
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val createFolderUseCase: CreateFolderUseCase,
    private val renameFolderUseCase: RenameFolderUseCase,
    private val noteRepository: NoteRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val captureRepository: CaptureRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val updateNoteUseCase: UpdateNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private var dataJob: Job? = null

    init {
        loadSubscriptionStatus()
        loadNoteViewType()
        loadData()
    }

    /** 구독 상태 로드 — 프리미엄 기능 UI 표시 제어 */
    private fun loadSubscriptionStatus() {
        val isPremium = subscriptionRepository.getCachedTier() == SubscriptionTier.PREMIUM
        _uiState.update { it.copy(isPremium = isPremium) }
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.SelectFilter -> selectFilter(event.folderId)
            is NotesEvent.ShowCreateFolderDialog -> showCreateFolderDialog()
            is NotesEvent.DismissCreateFolderDialog -> dismissCreateFolderDialog()
            is NotesEvent.CreateFolder -> createFolder(event.name)
            is NotesEvent.ShowRenameFolderDialog -> showRenameFolderDialog(event.folder)
            is NotesEvent.DismissRenameFolderDialog -> dismissRenameFolderDialog()
            is NotesEvent.RenameFolder -> renameFolder(event.folderId, event.newName)
            is NotesEvent.DeleteFolder -> deleteFolder(event.folderId)
            is NotesEvent.DismissError -> dismissError()
            is NotesEvent.ToggleNoteExpand -> toggleNoteExpand(event.noteId)
            is NotesEvent.DeleteNote -> deleteNote(event.captureId)
            is NotesEvent.DismissShareText -> _uiState.update { it.copy(shareText = null) }
            is NotesEvent.MoveNoteToFolder -> moveNoteToFolder(event.noteId, event.folderId)
        }
    }

    /**
     * 폴더 + 전체 노트를 동시에 로드
     * 폴더 이름 매핑 및 필터링을 한곳에서 처리
     */
    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                folderRepository.getAllFolders(),
                noteRepository.getFolderNoteCounts(),
                noteRepository.getAllNotesWithActiveCapture()
            ) { folders, countMap, allNotes ->
                // 폴더 목록 (Inbox는 노트 있을 때만 표시)
                val foldersWithCount = folders.map { folder ->
                    FolderWithCount(folder, countMap[folder.id] ?: 0)
                }.filter { folderWithCount ->
                    if (folderWithCount.folder.type == FolderType.INBOX) {
                        folderWithCount.noteCount > 0
                    } else {
                        true
                    }
                }

                // 폴더 ID → 이름 매핑
                val folderNameMap = folders.associate { it.id to it.name }

                // 노트 목록 (폴더 이름 + 서브타입 포함)
                val notesWithFolder = allNotes.map { preview ->
                    NoteWithCapture(
                        noteId = preview.noteId,
                        captureId = preview.captureId,
                        aiTitle = preview.aiTitle,
                        originalText = preview.originalText,
                        createdAt = preview.createdAt,
                        body = preview.body,
                        folderId = preview.folderId,
                        folderName = preview.folderId?.let { folderNameMap[it] },
                        noteSubType = preview.noteSubType
                    )
                }

                foldersWithCount to notesWithFolder
            }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message)
                    }
                }
                .collect { (foldersWithCount, allNotes) ->
                    val filteredNotes = applyFilter(
                        allNotes,
                        _uiState.value.selectedFilterFolderId
                    )
                    _uiState.update {
                        it.copy(
                            folders = foldersWithCount,
                            notes = filteredNotes,
                            isLoading = false
                        )
                    }
                    // 전체 노트를 캐시 (필터 변경 시 재사용)
                    cachedAllNotes = allNotes
                }
        }
    }

    /** 캐시된 전체 노트 (필터 전환 시 재쿼리 없이 사용) */
    private var cachedAllNotes: List<NoteWithCapture> = emptyList()

    /**
     * 폴더 필터 선택
     */
    private fun selectFilter(folderId: String?) {
        val filtered = applyFilter(cachedAllNotes, folderId)
        _uiState.update {
            it.copy(selectedFilterFolderId = folderId, notes = filtered)
        }
    }

    /** 폴더 ID로 노트 필터링 (null = 전체) */
    private fun applyFilter(
        notes: List<NoteWithCapture>,
        folderId: String?
    ): List<NoteWithCapture> {
        return if (folderId == null) notes
        else notes.filter { it.folderId == folderId }
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
                folderRepository.deleteFolder(folderId)
                // 삭제된 폴더가 현재 필터면 전체로 리셋
                if (_uiState.value.selectedFilterFolderId == folderId) {
                    _uiState.update { it.copy(selectedFilterFolderId = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** 노트 보기 유형 로드 */
    private fun loadNoteViewType() {
        viewModelScope.launch {
            userPreferenceRepository.observeString(
                PreferenceKeys.KEY_NOTE_VIEW_TYPE,
                NoteViewType.LIST.name
            ).collect { value ->
                val viewType = try {
                    NoteViewType.valueOf(value)
                } catch (_: Exception) {
                    NoteViewType.LIST
                }
                _uiState.update { it.copy(viewType = viewType) }
            }
        }
    }

    /** 노트 확장/축소 토글 (단일 확장 정책) */
    private fun toggleNoteExpand(noteId: String) {
        _uiState.update {
            it.copy(
                expandedNoteId = if (it.expandedNoteId == noteId) null else noteId
            )
        }
    }

    /** 노트 삭제 (휴지통으로 이동) */
    private fun deleteNote(captureId: String) {
        viewModelScope.launch {
            try {
                captureRepository.moveToTrash(captureId)
                _uiState.update { it.copy(expandedNoteId = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    /** 노트 폴더 이동 */
    private fun moveNoteToFolder(noteId: String, folderId: String) {
        viewModelScope.launch {
            try {
                updateNoteUseCase.moveToFolder(noteId, folderId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    /** 노트 공유 텍스트 생성 */
    fun shareNote(note: NoteWithCapture) {
        val title = note.aiTitle ?: note.originalText.take(40)
        val content = note.body ?: note.originalText
        val text = buildString {
            appendLine(title)
            appendLine()
            append(content)
        }
        _uiState.update { it.copy(shareText = text) }
    }
}
