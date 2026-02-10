package com.example.kairos_mobile.domain.model

/** Inbox 자동 분류 결과 */
data class InboxClassifyResult(
    val assignments: List<InboxAssignment>,
    val newFolders: List<NewFolderSuggestion>
)

/** 개별 캡처 분류 할당 */
data class InboxAssignment(
    val captureId: String,
    val targetFolderId: String,
    val targetFolderName: String,
    val newNoteSubType: String
)

/** 새 폴더 생성 제안 */
data class NewFolderSuggestion(
    val name: String,
    val type: String
)
