package com.flit.app.presentation.notes

import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.NoteViewType

/**
 * 노트 탭 UI 상태
 * 노트 우선 뷰 — 전체 노트 리스트 + 폴더 필터 칩
 */
data class NotesUiState(
    /** 폴더 목록 (시스템 + 사용자) — 필터 칩용 */
    val folders: List<FolderWithCount> = emptyList(),
    /** 현재 선택된 필터 폴더 ID (null = 전체) */
    val selectedFilterFolderId: String? = null,
    /** 표시할 노트 목록 (필터 적용됨) */
    val notes: List<NoteWithCapture> = emptyList(),
    /** 로딩 상태 */
    val isLoading: Boolean = false,
    /** 폴더 생성 다이얼로그 표시 */
    val showCreateFolderDialog: Boolean = false,
    /** 폴더 이름 변경 다이얼로그 대상 */
    val renamingFolder: Folder? = null,
    /** 에러 메시지 */
    val errorMessage: String? = null,
    /** 프리미엄 구독 여부 (AI 재구성 버튼 표시 제어) */
    val isPremium: Boolean = false,
    /** 노트 보기 유형 */
    val viewType: NoteViewType = NoteViewType.LIST,
    /** 확장된 노트 ID (단일 확장 정책) */
    val expandedNoteId: String? = null,
    /** 공유 텍스트 (ShareSheet 트리거) */
    val shareText: String? = null
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
    val createdAt: Long,
    val body: String? = null,
    val folderId: String? = null,
    val folderName: String? = null,
    /** 노트 서브 분류 (INBOX, IDEA, BOOKMARK, USER_FOLDER) */
    val noteSubType: String? = null
)

/**
 * 노트 화면 이벤트
 */
sealed interface NotesEvent {
    /** 폴더 필터 선택 (null = 전체) */
    data class SelectFilter(val folderId: String?) : NotesEvent
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
    /** 노트 확장/축소 토글 */
    data class ToggleNoteExpand(val noteId: String) : NotesEvent
    /** 노트 삭제 */
    data class DeleteNote(val captureId: String) : NotesEvent
    /** 노트 공유 텍스트 생성 완료 후 소비 */
    data object DismissShareText : NotesEvent
    /** 노트 폴더 이동 */
    data class MoveNoteToFolder(val noteId: String, val folderId: String) : NotesEvent
}
