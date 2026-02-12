package com.flit.app.presentation.notes.detail

import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.NoteDetail

/**
 * 노트 상세 화면 UI 상태
 */
data class NoteDetailUiState(
    val isLoading: Boolean = true,
    val noteDetail: NoteDetail? = null,
    val editedTitle: String = "",
    val editedBody: String = "",
    val selectedFolderId: String? = null,
    val hasChanges: Boolean = false,
    val error: String? = null,
    val showOriginalText: Boolean = false,
    val folders: List<Folder> = emptyList(),
    /** 공유용 텍스트 (한 번 소비 후 null 초기화) */
    val shareText: String? = null,
    /** 삭제 처리 중 (softDelete 완료, Snackbar 표시 중) */
    val isDeleted: Boolean = false,
    /** 삭제 후 네비게이션 이벤트 (한 번 소비 후 false) */
    val shouldNavigateBack: Boolean = false
)
