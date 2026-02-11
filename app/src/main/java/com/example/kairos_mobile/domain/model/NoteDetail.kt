package com.example.kairos_mobile.domain.model

/**
 * 노트 상세 도메인 모델
 * 노트 + 캡처 정보를 포함한 상세 조회용
 */
data class NoteDetail(
    val noteId: String,
    val captureId: String,
    val aiTitle: String?,
    val originalText: String,
    val body: String?,
    val classifiedType: ClassifiedType,
    val noteSubType: NoteSubType?,
    val folderId: String?,
    val imageUri: String?,
    val tags: List<String> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long
)
