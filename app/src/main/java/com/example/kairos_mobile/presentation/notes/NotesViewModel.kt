package com.example.kairos_mobile.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairos_mobile.domain.model.NoteFolder
import com.example.kairos_mobile.domain.repository.BookmarkRepository
import com.example.kairos_mobile.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * NotesScreen ViewModel (PRD v4.0)
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadNotes()
        loadBookmarks()
        loadNoteCounts()
    }

    /**
     * 이벤트 처리
     */
    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.SelectTab -> selectTab(event.tab)
            is NotesEvent.SelectFolder -> selectFolder(event.folder)
            is NotesEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is NotesEvent.ClearSearch -> clearSearch()
            is NotesEvent.DeleteNote -> deleteNote(event.noteId)
            is NotesEvent.DeleteBookmark -> deleteBookmark(event.bookmarkId)
            is NotesEvent.MoveNoteToFolder -> moveNoteToFolder(event.noteId, event.folder)
            is NotesEvent.DismissError -> dismissError()
        }
    }

    /**
     * 탭 선택
     */
    private fun selectTab(tab: NotesTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /**
     * 폴더 선택
     */
    private fun selectFolder(folder: NoteFolder?) {
        _uiState.update { it.copy(selectedFolder = folder) }
        loadNotes()
    }

    /**
     * 검색어 업데이트 (디바운싱 적용)
     */
    private fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearching = query.isNotEmpty()) }

        searchJob?.cancel()
        if (query.isEmpty()) {
            loadNotes()
            loadBookmarks()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // 300ms 디바운싱
            searchNotes(query)
            searchBookmarks(query)
        }
    }

    /**
     * 검색 초기화
     */
    private fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", isSearching = false) }
        loadNotes()
        loadBookmarks()
    }

    /**
     * 노트 로드
     */
    private fun loadNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val notesFlow = when (val folder = _uiState.value.selectedFolder) {
                null -> noteRepository.getAllNotes()
                else -> noteRepository.getNotesByFolder(folder)
            }

            notesFlow
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "노트 로드 실패"
                        )
                    }
                }
                .collect { notes ->
                    _uiState.update {
                        it.copy(
                            notes = notes,
                            isLoading = false
                        )
                    }
                }
        }
    }

    /**
     * 북마크 로드
     */
    private fun loadBookmarks() {
        viewModelScope.launch {
            bookmarkRepository.getAllBookmarks()
                .catch { /* 에러 무시 */ }
                .collect { bookmarks ->
                    _uiState.update { it.copy(bookmarks = bookmarks) }
                }
        }
    }

    /**
     * 노트 개수 로드
     */
    private fun loadNoteCounts() {
        viewModelScope.launch {
            // 전체 노트 개수
            noteRepository.getNoteCount()
                .catch { /* 에러 무시 */ }
                .collect { count ->
                    _uiState.update { it.copy(totalNoteCount = count) }
                }
        }

        viewModelScope.launch {
            // 전체 북마크 개수
            bookmarkRepository.getBookmarkCount()
                .catch { /* 에러 무시 */ }
                .collect { count ->
                    _uiState.update { it.copy(totalBookmarkCount = count) }
                }
        }

        // 폴더별 노트 개수
        NoteFolder.entries.forEach { folder ->
            viewModelScope.launch {
                noteRepository.getNoteCountByFolder(folder)
                    .catch { /* 에러 무시 */ }
                    .collect { count ->
                        _uiState.update { state ->
                            state.copy(
                                noteCountByFolder = state.noteCountByFolder + (folder to count)
                            )
                        }
                    }
            }
        }
    }

    /**
     * 노트 검색
     */
    private fun searchNotes(query: String) {
        viewModelScope.launch {
            noteRepository.searchNotes(query)
                .catch { /* 에러 무시 */ }
                .collect { notes ->
                    _uiState.update {
                        it.copy(notes = notes, isSearching = false)
                    }
                }
        }
    }

    /**
     * 북마크 검색
     */
    private fun searchBookmarks(query: String) {
        viewModelScope.launch {
            bookmarkRepository.searchBookmarks(query)
                .catch { /* 에러 무시 */ }
                .collect { bookmarks ->
                    _uiState.update { it.copy(bookmarks = bookmarks) }
                }
        }
    }

    /**
     * 노트 삭제
     */
    private fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
        }
    }

    /**
     * 북마크 삭제
     */
    private fun deleteBookmark(bookmarkId: String) {
        viewModelScope.launch {
            bookmarkRepository.deleteBookmark(bookmarkId)
        }
    }

    /**
     * 노트 폴더 이동
     */
    private fun moveNoteToFolder(noteId: String, folder: NoteFolder) {
        viewModelScope.launch {
            noteRepository.moveToFolder(noteId, folder)
        }
    }

    /**
     * 에러 메시지 닫기
     */
    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
