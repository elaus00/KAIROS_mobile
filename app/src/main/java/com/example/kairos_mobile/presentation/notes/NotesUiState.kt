package com.example.kairos_mobile.presentation.notes

import com.example.kairos_mobile.domain.model.Folder

/**
 * 노트 탭 UI 상태
 * 폴더 기반 노트 관리 (Inbox/Ideas/Bookmarks + 사용자 폴더)
 */
data class NotesUiState(
    /** 폴더 목록 (시스템 + 사용자) */
    val folders: List<FolderWithCount> = emptyList(),
    /** 현재 선택된 폴더 (null = 메인 폴더 리스트) */
    val selectedFolder: Folder? = null,
    /** 선택된 폴더의 노트 목록 */
    val notes: List<NoteWithCapture> = emptyList(),
    /** 로딩 상태 */
    val isLoading: Boolean = false,
    /** 폴더 생성 다이얼로그 표시 */
    val showCreateFolderDialog: Boolean = false,
    /** 폴더 이름 변경 다이얼로그 대상 */
    val renamingFolder: Folder? = null,
    /** 에러 메시지 */
    val errorMessage: String? = null
)

/**
 * 폴더 + 노트 수
 */
data class FolderWithCount(
    val folder: Folder,
    val noteCount: Int
)

/**
 * 노트 + 캡처 정보 (표시용)
 */
data class NoteWithCapture(
    val noteId: String,
    val captureId: String,
    val aiTitle: String?,
    val originalText: String,
    val createdAt: Long
)

/**
 * 노트 화면 이벤트
 */
sealed interface NotesEvent {
    /** 폴더 선택 (노트 목록으로 이동) */
    data class SelectFolder(val folder: Folder) : NotesEvent
    /** 폴더 목록으로 돌아가기 */
    data object BackToFolders : NotesEvent
    /** 폴더 생성 다이얼로그 표시 */
    data object ShowCreateFolderDialog : NotesEvent
    /** 폴더 생성 다이얼로그 닫기 */
    data object DismissCreateFolderDialog : NotesEvent
    /** 폴더 생성 */
    data class CreateFolder(val name: String) : NotesEvent
    /** 폴더 이름 변경 다이얼로그 표시 */
    data class ShowRenameFolderDialog(val folder: Folder) : NotesEvent
    /** 폴더 이름 변경 다이얼로그 닫기 */
    data object DismissRenameFolderDialog : NotesEvent
    /** 폴더 이름 변경 */
    data class RenameFolder(val folderId: String, val newName: String) : NotesEvent
    /** 폴더 삭제 */
    data class DeleteFolder(val folderId: String) : NotesEvent
    /** 에러 메시지 닫기 */
    data object DismissError : NotesEvent
}
