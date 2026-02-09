package com.example.kairos_mobile.presentation.notes.detail

import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.NoteDetail

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
    val isSaving: Boolean = false,
    val error: String? = null,
    val showOriginalText: Boolean = false,
    val folders: List<Folder> = emptyList(),
    /** 공유용 텍스트 (한 번 소비 후 null 초기화) */
    val shareText: String? = null
)
