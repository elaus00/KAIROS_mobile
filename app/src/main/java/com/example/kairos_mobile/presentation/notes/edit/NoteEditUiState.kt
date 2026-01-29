package com.example.kairos_mobile.presentation.notes.edit

import com.example.kairos_mobile.domain.model.NoteFolder

/**
 * NoteEditScreen UI 상태 (PRD v4.0)
 */
data class NoteEditUiState(
    // 노트 ID (새 노트인 경우 null)
    val noteId: String? = null,

    // 제목
    val title: String = "",

    // 내용
    val content: String = "",

    // 폴더
    val folder: NoteFolder = NoteFolder.INBOX,

    // 태그
    val tags: List<String> = emptyList(),

    // 새 태그 입력
    val newTagInput: String = "",

    // 로딩 상태
    val isLoading: Boolean = false,

    // 저장 중 상태
    val isSaving: Boolean = false,

    // 저장 성공
    val saveSuccess: Boolean = false,

    // 에러 메시지
    val errorMessage: String? = null
) {
    /**
     * 새 노트인지 확인
     */
    val isNewNote: Boolean get() = noteId == null

    /**
     * 저장 가능 여부 (제목 필수)
     */
    val canSave: Boolean get() = title.isNotBlank()
}

/**
 * NoteEdit 화면 이벤트
 */
sealed interface NoteEditEvent {
    data class UpdateTitle(val title: String) : NoteEditEvent
    data class UpdateContent(val content: String) : NoteEditEvent
    data class UpdateFolder(val folder: NoteFolder) : NoteEditEvent
    data class UpdateNewTagInput(val input: String) : NoteEditEvent
    data object AddTag : NoteEditEvent
    data class RemoveTag(val tag: String) : NoteEditEvent
    data object Save : NoteEditEvent
    data object Delete : NoteEditEvent
    data object DismissError : NoteEditEvent
}
