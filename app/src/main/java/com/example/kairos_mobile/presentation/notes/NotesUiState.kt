package com.example.kairos_mobile.presentation.notes

import com.example.kairos_mobile.domain.model.Bookmark
import com.example.kairos_mobile.domain.model.Note
import com.example.kairos_mobile.domain.model.NoteFolder

/**
 * NotesScreen UI 상태 (PRD v4.0)
 */
data class NotesUiState(
    // 현재 탭 (노트/북마크)
    val selectedTab: NotesTab = NotesTab.NOTES,

    // 선택된 폴더 (null = 전체)
    val selectedFolder: NoteFolder? = null,

    // 검색어
    val searchQuery: String = "",

    // 노트 목록
    val notes: List<Note> = emptyList(),

    // 북마크 목록
    val bookmarks: List<Bookmark> = emptyList(),

    // 노트 개수 (폴더별)
    val noteCountByFolder: Map<NoteFolder, Int> = emptyMap(),

    // 전체 노트 개수
    val totalNoteCount: Int = 0,

    // 전체 북마크 개수
    val totalBookmarkCount: Int = 0,

    // 로딩 상태
    val isLoading: Boolean = false,

    // 검색 중 상태
    val isSearching: Boolean = false,

    // 에러 메시지
    val errorMessage: String? = null
)

/**
 * 노트/북마크 탭
 */
enum class NotesTab {
    NOTES,
    BOOKMARKS;

    fun getDisplayName(): String {
        return when (this) {
            NOTES -> "노트"
            BOOKMARKS -> "북마크"
        }
    }
}

/**
 * Notes 화면 이벤트
 */
sealed interface NotesEvent {
    data class SelectTab(val tab: NotesTab) : NotesEvent
    data class SelectFolder(val folder: NoteFolder?) : NotesEvent
    data class UpdateSearchQuery(val query: String) : NotesEvent
    data object ClearSearch : NotesEvent
    data class DeleteNote(val noteId: String) : NotesEvent
    data class DeleteBookmark(val bookmarkId: String) : NotesEvent
    data class MoveNoteToFolder(val noteId: String, val folder: NoteFolder) : NotesEvent
    data object DismissError : NotesEvent
}
