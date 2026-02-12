package com.flit.app.domain.model

/** AI 노트 분석 입력 데이터 */
data class NoteAiInput(
    val captureId: String,
    val aiTitle: String,
    val tags: List<String>,
    val noteSubType: String?,
    val folderId: String?
)
