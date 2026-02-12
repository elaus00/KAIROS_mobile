package com.flit.app.domain.model

/** AI 노트 그룹 */
data class NoteGroup(
    val folderName: String,
    val folderType: String,
    val captureIds: List<String>
)
