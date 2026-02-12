package com.flit.app.domain.model

/**
 * 노트 목록 표시용 캡처 프리뷰
 */
data class NoteWithCapturePreview(
    val noteId: String,
    val captureId: String,
    val aiTitle: String?,
    val originalText: String,
    val createdAt: Long,
    val body: String? = null,
    val folderId: String? = null,
    val noteSubType: String? = null
)
